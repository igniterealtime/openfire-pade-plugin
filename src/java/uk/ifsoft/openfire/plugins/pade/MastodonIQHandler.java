package uk.ifsoft.openfire.plugins.pade;

import org.dom4j.Element;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import java.net.*;
import javax.net.ssl.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import net.sf.json.*;

/**
 * custom IQ handler for Mastodon SFU requests and responses
 */
public class MastodonIQHandler extends IQHandler implements SessionEventListener, ServerFeaturesProvider
{
    private final static Logger Log = LoggerFactory.getLogger( MastodonIQHandler.class );	
	
	public void startHandler() {
		SessionEventDispatcher.addListener(this);													
	}

	public void stopHandler() {
		SessionEventDispatcher.removeListener(this);	
	}
	
    public MastodonIQHandler() {
        super("Mastodon IQ Handler");
    }

    @Override
    public IQ handleIQ(IQ iq)
    {
		HttpsURLConnection con = null;
		BufferedReader in = null;
		DataOutputStream out = null;
		
		if (iq.getType() == IQ.Type.set || iq.getType() == IQ.Type.get) {
			IQ reply = IQ.createResultIQ(iq);

			try {
				Log.debug("C2S request \n" + iq.toString());
				final String from = iq.getFrom().toBareJID();				
				final Element element = iq.getChildElement();
				final String endpoint = element.attribute("endpoint").getStringValue();							
				final Element json = element.element("json");	
				String accessToken = element.attribute("token").getStringValue();
				
				URL url = new URL(endpoint);
				con = (HttpsURLConnection) url.openConnection();
				
				if (!removeNull(accessToken).equals("")) {				
					con.setRequestProperty("authorization", "Bearer " + accessToken);
				}
				
				if (iq.getType() == IQ.Type.set) 
				{				
					if (json != null) {
						con.setRequestMethod("POST");
						con.setRequestProperty("content-type", "application/json");
						con.setDoOutput(true);
						out = new DataOutputStream(con.getOutputStream());
						out.writeBytes(json.getText());
						out.flush();
						out.close();					
					}
				}
				else {
					con.setRequestMethod("GET");					
				}					

				int status = con.getResponseCode();
				in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
				String inputLine;
				StringBuffer content = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				con.disconnect();
				
				String text = element.getText();
				Log.info("C2S response " + status + "\n" + content.toString());
				
				Element mastodon = reply.setChildElement("s2c", "urn:xmpp:mastodon:0");
				Element payload = mastodon.addElement("json", "urn:xmpp:json:0");
				payload.setText(content.toString());				

				return reply;

			} catch(Exception e) {
				try {
					if (in != null) in.close();
					if (out != null) out.close();
					if (con != null) con.disconnect();
				} catch (Exception ioe) {}
				
				Log.error("Mastodon handleIQ", e);
				reply.setError(new PacketError(PacketError.Condition.not_allowed, PacketError.Type.modify, e.toString()));
				return reply;
			}
		}
		return null;
    }		

    @Override
    public IQHandlerInfo getInfo()
    {
        return new IQHandlerInfo("c2s", "urn:xmpp:mastodon:0");
    }
	
    @Override
    public Iterator<String> getFeatures()
    {
        final ArrayList<String> features = new ArrayList<>();
        features.add( "urn:xmpp:mastodon:0" );
        return features.iterator();
    }	

    private String removeNull(String s)
    {
        if (s == null) {
            return "";
        }

        return s.trim();
    }	
	
    //-------------------------------------------------------
    //
    //      session management
    //
    //-------------------------------------------------------

    public void anonymousSessionCreated(Session session)
    {
        Log.debug("MastodonIQHandler -  anonymousSessionCreated "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void anonymousSessionDestroyed(Session session)
    {
        Log.debug("MastodonIQHandler -  anonymousSessionDestroyed "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void resourceBound(Session session)
    {
        Log.debug("MastodonIQHandler -  resourceBound "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void sessionCreated(Session session)
    {
        Log.debug("MastodonIQHandler -  sessionCreated "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }

    public void sessionDestroyed(Session session)
    {
        Log.debug("MastodonIQHandler -  sessionDestroyed "+ session.getAddress().toString() + "\n" + ((ClientSession) session).getPresence().toXML());
    }	
}
