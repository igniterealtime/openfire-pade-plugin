package org.jivesoftware.openfire.plugin.ofmeet;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.plugin.spark.Bookmark;
import org.jivesoftware.openfire.plugin.spark.BookmarkManager;
import org.jivesoftware.openfire.security.SecurityAuditManager;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.EmailService;
import org.jivesoftware.util.JiveGlobals;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import java.util.*;
import java.util.regex.Pattern;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Meeting planner, which periodically evaluates planned meetings.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
// TODO it seems odd to have both a Scheduler and a Timer, as their functionality is pretty similar. Can they be combined?
// BAO modifications - just replace entire class

public class MeetingPlanner implements Job
{
    private static final Logger Log = LoggerFactory.getLogger( MeetingPlanner.class );
    private static Scheduler scheduler = null;

    private Timer timer = null;

    /**
     * Initialize the planner,
     *
     * @throws Exception On any problem.
     */
    protected synchronized void initialize() throws Exception
    {
        Log.debug( "Initializing Meeting Planner..." );

        if ( timer != null || scheduler != null )
        {
            Log.warn( "Another Meeting planner appears to have been initialized earlier! Unexpected behavior might be the result of this new initialization!" );
        }

        long interval = 900000;

        try {
            interval = Long.parseLong(JiveGlobals.getProperty("ofmeet.planner.check.interval", "900000"));

        } catch (Exception e) {}

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override public void run()
            {
                if (XMPPServer.getInstance().getPluginManager().getPlugin("bookmarks") == null)
                {
                    Log.debug( "Skipping the periodic execution, as the 'bookmarks' plugin is not loaded." );

                } else
                    processMeetingPlanner();
            }

        }, 0, interval);

        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            List<String> properties = JiveGlobals.getPropertyNames();
            List<String> deletes = new ArrayList<String>();

            for (String propertyName : properties) {

                if (propertyName.indexOf("ofmeet.cron.") == 0)
                {
                    Log.info("Loading quartz cron job " + propertyName);

                    String trigger = JiveGlobals.getProperty(propertyName);
                    String key = propertyName.substring(12);
                    Bookmark bookmark = BookmarkManager.getBookmark(Long.parseLong(key));

                    String error = processMeetingCron(bookmark, trigger, null);

                    if (error != null)
                    {
                        deletes.add(propertyName);
                    }
                }
            }

            for (String propertyName : deletes)
            {
                Log.warn("Removing quartz cron job " + propertyName);
                JiveGlobals.deleteProperty(propertyName);
            }

        } catch (SchedulerException se) {
            Log.error("Quartz Scheduler", se);
        }
        Log.trace( "Successfully initialized Meeting Planner." );
    }

    /**
     * Destroying the wrapped component. After this call, the wrapped component can be re-initialized.
     *
     * @throws Exception On any problem.
     */
    protected synchronized void destroy() throws Exception
    {
        Log.debug( "Destroying Meeting Planner..." );

        if ( timer == null )
        {
            Log.warn( "Unable to destroy the Meeting Planner, as none appears to be running!" );
        }
        else
        {
            timer.cancel();
            timer = null;
            Log.trace( "Successfully destroyed Meeting Planner." );
        }

        if ( scheduler != null )
        {
            scheduler.shutdown();
            scheduler = null;
        }

        Log.trace( "Destroyed Meeting Planner." );
    }

    public static void processMeetingPlanner() 
    {
        Log.debug("OfMeet Plugin - processMeetingPlanner");
       try {
			final Collection<Bookmark> bookmarks = BookmarkManager.getBookmarks();

			for (Bookmark bookmark : bookmarks)
			{
				String json = bookmark.getProperty("calendar");

				if (json != null)
				{
					bookmark.setProperty("lock", "true");

					JSONArray calendar = new JSONArray( json );
					boolean done = false;

					for(int i = 0; i < calendar.length(); i++)
					{
						try {
							JSONObject meeting = calendar.getJSONObject( i );
							boolean processed = meeting.getBoolean("processed");

							if (processed) continue;

							long startLong = meeting.getLong("startTime");
							long overlap = 300000;  // default 5 mins

							try {
								overlap = Long.parseLong(JiveGlobals.getProperty("ofmeet.planner.overlap.interval", "300000"));

							} catch (Exception e) {}

							Date rightNow = new Date(System.currentTimeMillis());
							Date actionDate = new Date(startLong + overlap);
							Date warnDate = new Date(startLong - overlap);

							Log.debug("OfMeet Plugin - scanning meeting now " + rightNow + " action " + actionDate + " warn " + warnDate + "\n" + meeting );

							if(rightNow.after(warnDate) && rightNow.before(actionDate))
							{
								processMeetingTrigger(meeting, bookmark);

								meeting.put("processed", true);
								done = true;
							}
						} catch (Exception e) {
							Log.error("processMeetingPlanner", e);
						}
					}

					if (done)
					{
						json = calendar.toString();
						bookmark.setProperty("calendar", json);

						Log.debug("OfMeet Plugin - processed meeting\n" + json);
					}

					bookmark.setProperty("lock", "false");
				}
			}
		} catch (Exception e) {
			Log.error("processMeetingPlanner", e);
		}			
    }

    public static void processMeetingTrigger(JSONObject meeting, Bookmark bookmark)
    {
        String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
        String rootUrl = JiveGlobals.getProperty("ofmeet.root.url.secure", "https://" + hostname + ":" + JiveGlobals.getProperty("httpbind.port.secure", "7443"));
        String url = null;

        try {
			JSONObject json = new JSONObject(JiveGlobals.getProperty("pade.branding.ofmeetUrl", "{\"value\":\"" + rootUrl + "/ofmeet/\"}"));			
            url = json.getString("value") + bookmark.getValue().split("@")[0];
        } catch (Exception e) {
            Log.error("bookmark ignored, url missing or bad " + bookmark.getValue());
            return;
        }

        for (String user : bookmark.getUsers())
        {
            processMeeting(meeting, user, url);
        }

        for (String groupName : bookmark.getGroups())
        {
            try {
                Group group = GroupManager.getInstance().getGroup(groupName);

                for (JID memberJID : group.getMembers())
                {
                    processMeeting(meeting, memberJID.getNode(), url);
                }

            } catch (GroupNotFoundException e) { }
        }
    }

    public static void processMeeting(JSONObject meeting, String username, String videourl)
    {
        Log.info("OfMeet Plugin - processMeeting " + username + " " + meeting);

        try {
            User user = null;   // assume username is email address
            String email = username;
            String name = username;

            if (username.indexOf("@") == -1)
            {
                try {
                    UserManager userManager = XMPPServer.getInstance().getUserManager();
                    user = userManager.getUser(username);
                    name = user.getName();
                    email = user.getEmail();
                } catch (Exception e) {
                    email = null;
                }
            }

            if (email != null)
            {
                Date start = new Date(meeting.getLong("startTime"));
                Date end = new Date(meeting.getLong("endTime"));
                String description = meeting.getString("description");
                String title = meeting.getString("title");
                String room = meeting.getString("room");
                String audiourl = videourl + "#config.startWithVideoMuted=true";
                String template = JiveGlobals.getProperty("ofmeet.email.template", "Dear [name],\n\nYou have an online meeting from [start] to [end]\n\n[description]\n\nTo join, please click\n[videourl]\nFor audio only with no webcan, please click\n[audiourl]\n\nAdministrator - [domain]");

                HashMap variables = new HashMap<String, String>();
                String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

                variables.put("name", name);
                variables.put("email", email);
                variables.put("start", start.toString());
                variables.put("end", end.toString());
                variables.put("description", description);
                variables.put("title", title);
                variables.put("room", room);
                variables.put("videourl", videourl);
                variables.put("audiourl", audiourl);
                variables.put("domain", domain);

                sendEmail(name, email, title, replaceTokens(template, variables), null);

                if (user != null && SessionManager.getInstance().getSessions(username).size() > 0)
                {
                    // send invitation to user session as chat message with url

                    org.xmpp.packet.Message message = new org.xmpp.packet.Message();
                    message.setFrom(domain);
                    message.setSubject(title);
                    message.setTo(username + "@" + domain);
                    message.setBody(videourl);

                    SessionManager.getInstance().userBroadcast(username, message);
                }
                SecurityAuditManager.getInstance().logEvent(username, "sent email - " + title, description);
            }

        }
        catch (Exception e) {
            Log.error("processMeeting error", e);
        }
    }

    public static void sendEmail(String toName, String toAddress, String subject, String body, String htmlBody)
    {
        try {
            String fromAddress = "no_reply@" + JiveGlobals.getProperty("ofmeet.email.domain", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
            String fromName = JiveGlobals.getProperty("ofmeet.email.fromname", "Pade Meetings");

            Log.debug( "sendEmail " + toAddress + " " + subject + "\n " + body + "\n " + htmlBody);
            EmailService.getInstance().sendMessage(toName, toAddress, fromName, fromAddress, subject, body, htmlBody);
        }
        catch (Exception e) {
            Log.error(e.toString());
        }

    }

    public static String replaceTokens(String text, Map<String, String> replacements)
    {
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find())
        {
            String replacement = replacements.get(matcher.group(1));

            if (replacement != null)
            {
                matcher.appendReplacement(buffer, "");
                buffer.append(replacement);
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        Log.info( "Quartz Execute Job....");
        try {
            String key = jobExecutionContext.getJobDetail().getKey().toString();
            Bookmark bookmark = (Bookmark) jobExecutionContext.getScheduler().getContext().get(key);
            long now = System.currentTimeMillis();

            JSONObject meeting = new JSONObject();
            meeting.put("startTime", now);
            meeting.put("endTime", now + 3600000);
            meeting.put("description", bookmark.getName());
            meeting.put("title", bookmark.getName());
            meeting.put("room", bookmark.getValue().split("@")[0]);

            processMeetingTrigger(meeting, bookmark);
        }
        catch (Throwable e) {
            Log.error("Failed to execute quartz job...", e);
        }
    }

    public static String processMeetingCron(Bookmark bookmark, String newTrigger, String oldTrigger)
    {
        String key = "ofmeet-" + bookmark.getValue();
        String response = null;

        Log.info( "processMeetingCron " + key + " " + oldTrigger + " " + newTrigger);

        if (oldTrigger != null && !"".equals(oldTrigger))
        {
            bookmark.deleteProperty("quartz");

            try {
                scheduler.deleteJob(JobKey.jobKey(key, key));
                JiveGlobals.deleteProperty("ofmeet.cron." + bookmark.getBookmarkID());
            }
            catch (Throwable e) {
                Log.error("Failed to execute quartz job...", e);
            }
        }

        if (newTrigger != null && !"".equals(newTrigger.trim()) && !"delete".equals(newTrigger) && !"remove".equals(newTrigger))
        {
            bookmark.setProperty("quartz", newTrigger);

            try {
                JobDetail jobDetail = newJob(MeetingPlanner.class).withIdentity(key, key).build();
                CronTrigger conTrigger = newTrigger().withIdentity(key, key).withSchedule(cronSchedule(newTrigger)).build();
                scheduler.getContext().put(jobDetail.getKey().toString(), bookmark);
                scheduler.scheduleJob(jobDetail, conTrigger);

                JiveGlobals.setProperty("ofmeet.cron." + bookmark.getBookmarkID(), newTrigger);
            }
            catch (Throwable e) {
                Log.error("Failed to execute quartz job...", e);
                response = e.toString();
            }
        }
        return response;
    }
}
