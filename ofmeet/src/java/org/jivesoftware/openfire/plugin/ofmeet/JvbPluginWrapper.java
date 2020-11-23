package org.jivesoftware.openfire.plugin.ofmeet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jitsi.videobridge.openfire.*;

import de.mxro.process.*;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.JiveGlobals;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.*;
import java.util.*;
import java.net.*;
import org.jitsi.util.OSUtils;
import java.util.Properties;

import de.mxro.process.*;
import org.jivesoftware.util.JiveGlobals;
import org.igniterealtime.openfire.plugin.ofmeet.config.OFMeetConfig;

import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import net.sf.json.JSONObject;

/**
 * A wrapper object for the Jitsi Videobridge Openfire plugin.
 *
 * This wrapper can be used to instantiate/initialize and tearing down an instance of that plugin. An instance of this
 * class is re-usable.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JvbPluginWrapper implements ProcessListener
{
    private static final Logger Log = LoggerFactory.getLogger(JvbPluginWrapper.class);
    public static JvbPluginWrapper self;

    private PluginImpl jitsiPlugin;
    private XProcess jvbThread = null;


    /**
     * Initialize the wrapped component.
     *
     * @throws Exception On any problem.
     */
    public synchronized void initialize(final PluginManager manager, final File pluginDirectory) throws Exception
    {
        Log.debug( "Initializing Jitsi Videobridge..." );
        JvbPluginWrapper self = this;

        jitsiPlugin = new PluginImpl();
        jitsiPlugin.initializePlugin( manager, pluginDirectory );

        final OFMeetConfig config = new OFMeetConfig();
        ensureJvbUser(config);

        final String jvbHomePath = pluginDirectory.getPath() + File.separator + "classes" + File.separator + "jvb";
        final String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        final String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
        final String main_muc = JiveGlobals.getProperty( "ofmeet.main.muc", "conference." + domain);
        final String username =  config.getJvbName();
        final String password = config.getJvbPassword();
        final String ipAddress = getIpAddress();

        List<String> lines = Arrays.asList(
            "videobridge {",
            "    health {",
            "        interval=300 seconds",
            "    }",
            "",
            "    stats {",
            "        # Enable broadcasting stats/presence in a MUC",
            "        enabled = true",
            "        transports = [",
            "            { type = \"muc\" }",
            "        ]",
            "    }",
            "",
            "    websockets {",
            "      enabled = true",
            "      domain = \"" + domain + "\"",
            "      tls = true",
            "    }",
            "",
            "    apis {",
            "        xmpp-client {",
            "               configs {",
            "                # Connect to the first XMPP server",
            "                shard {",
            "                    hostname= \"" + hostname + "\"",
            "                    domain = \"" + domain + "\"",
            "                    username = \"" + username + "\"",
            "                    password = \"" + password + "\"",
            "                    muc_jids = \"ofmeet@" + main_muc + "\"",
            "                    muc_nickname = \"" + username + "\"",
            "                    disable_certificate_verification = true",
            "                }",
            "               }",
            "        }",
            "    }",
            "",
            "    ice {",
            "        tcp {",
            "            enabled = " + (RuntimeConfiguration.isTcpEnabled() ? "true" : "false"),
            "            port = \"" + RuntimeConfiguration.getTcpPort() + "\"",
            "            mapped-port = \"" + RuntimeConfiguration.getTcpMappedPort() +"\"",
            "        }",
            "        udp {",
            "            port = \"" + JiveGlobals.getProperty( PluginImpl.SINGLE_PORT_NUMBER_PROPERTY_NAME, "10000" ) + "\"",
            "            local-address = " + JiveGlobals.getProperty( PluginImpl.MANUAL_HARVESTER_LOCAL_PROPERTY_NAME, ipAddress),
            "            public-address = " + JiveGlobals.getProperty( PluginImpl.MANUAL_HARVESTER_PUBLIC_PROPERTY_NAME, ipAddress),
            "        }",
            "    }",
            "}"
        );

        Path configFile = Paths.get(jvbHomePath + File.separator + "application.conf");
        try
        {
            Files.write(configFile, lines, Charset.forName("UTF-8"));
        } catch (Exception e) {
            Log.error("createConfigFile error", e);
        }

        String javaHome = System.getProperty("java.home");
        String javaExec = javaHome + File.separator + "bin" + File.separator + "java";

        if(OSUtils.IS_WINDOWS64)
        {
            javaExec = javaExec + ".exe";
        }

         makeFileExecutable(javaExec);
         String cmdLine = javaExec + " -Dconfig.file=" + configFile + " -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=. -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=. -Djava.util.logging.config.file=./logging.properties -Djdk.tls.ephemeralDHKeySize=2048 -cp " + jvbHomePath + "/jitsi-videobridge.jar" + File.pathSeparator + jvbHomePath + "/jitsi-videobridge-2.1-SNAPSHOT-jar-with-dependencies.jar org.jitsi.videobridge.MainKt  --apis=rest";
         jvbThread = Spawn.startProcess(cmdLine, new File(jvbHomePath), this);

        Log.info( "Successfully initialized Jitsi Videobridge.\n" + cmdLine );
    }

    public String getIpAddress()
    {
        String ourHostname = XMPPServer.getInstance().getServerInfo().getHostname();
        String ourIpAddress = "127.0.0.1";

        try {
            ourIpAddress = InetAddress.getByName(ourHostname).getHostAddress();
        } catch (Exception e) {

        }

        return ourIpAddress;
    }

    public JSONObject getConferenceStats()
    {
        Log.info( "getConferenceStats" );

        String server = getIpAddress();

        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://" + server + ":8080/colibri/stats");
            get.setHeader("Content-Type", "application/json");
            HttpResponse response2 = client.execute(get);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response2.getEntity().getContent()));

            String line;
            String json = "";

            while ((line = rd.readLine()) != null) {
                json = json + line;
            }
            return new JSONObject(json).getJSONObject("data");

        } catch (Exception e) {
            return new JSONObject("{\"data\": {\"current_timestamp\":0, \"total_conference_seconds\":0, \"total_participants\":0, \"total_failed_conferencestotal_failed_conferences\":0, \"total_conferences_created\":0, \"total_conferences_completed\":0, \"conferences\":0, \"participants\":0, \"largest_conference\":0, \"p2p_conferences\":0}}");
        }

    }

    public synchronized void destroy() throws Exception
    {
        //Log.info(getConferenceStats().toString());
        Log.debug( "Destroying jvb process." );

        if (jvbThread != null) jvbThread.destory();
        if (jitsiPlugin != null ) jitsiPlugin.destroyPlugin();

        Log.debug( "Successfully destroyed jvb process." );
    }

    public void onOutputLine(final String line)
    {
        Log.info("onOutputLine " + line);
    }

    public void onProcessQuit(int code)
    {
        Log.info("onProcessQuit " + code);
    }

    public void onOutputClosed() {
        Log.error("onOutputClosed");
    }

    public void onErrorLine(final String line)
    {
        Log.info(line);
    }

    public void onError(final Throwable t)
    {
        Log.error("Thread error", t);
    }

    private void ensureJvbUser(OFMeetConfig config)
    {
        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        final String username =  config.getJvbName();

        if ( !userManager.isRegisteredUser( username ) )
        {
            Log.info( "No pre-existing 'jvb' user detected. Generating one." );
            String password = config.getJvbPassword();

            if ( password == null || password.isEmpty() )
            {
                password = StringUtils.randomString( 40 );
            }

            try
            {
                userManager.createUser( username, password, "JVB User (generated)", null);
                config.setJvbPassword( password );
            }
            catch ( Exception e )
            {
                Log.error( "Unable to provision a 'jvb' user.", e );
            }
        }
    }
    private void makeFileExecutable(String path)
    {
        File file = new File(path);
        file.setReadable(true, true);
        file.setWritable(true, true);
        file.setExecutable(true, true);
    }
}
