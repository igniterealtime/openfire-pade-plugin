package org.jivesoftware.openfire.plugin.ofmeet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.session.ClientSession;

import org.xmpp.component.Component;
import org.xmpp.component.AbstractComponent;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import org.dom4j.Element;
import org.xmpp.packet.*;
import java.util.List;

public class FocusComponent extends AbstractComponent
{
    private static final Logger Log = LoggerFactory.getLogger(FocusComponent.class);

    public FocusComponent()
    {

    }
	
    @Override public String getDescription()
    {
        return "Focus Component";
    }
	
    @Override public String getName()
    {
        return "focus";
    }	
	
    @Override protected void handleIQResult(IQ iq)
    {
        Log.debug("handleIQResult \n"+ iq.toString());	
		
        try {		
			JID to = iq.getTo();
			iq.setTo(to.getResource());
			iq.setFrom("focus." + XMPPServer.getInstance().getServerInfo().getXMPPDomain());
			XMPPServer.getInstance().getIQRouter().route( iq );	
        }
        catch(Exception e) {
            Log.error("handleIQResult", e);
        }		
	}		

    @Override public IQ handleIQSet(IQ iq)
    {
        Log.debug("handleIQSet \n"+ iq.toString());
		IQ iq1 = IQ.createResultIQ(iq);
			
        try {	
			iq1.setType(org.xmpp.packet.IQ.Type.result);
			iq1.setChildElement(iq.getChildElement().createCopy());
			
			JID from = iq.getFrom();
			String to = "focus@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() + "/focus";
			
			for (ClientSession sess : SessionManager.getInstance().getSessions("focus") )
			{
				to = sess.getAddress().toString();	
			}			
			iq.setTo(to);
			iq.setFrom("focus." + XMPPServer.getInstance().getServerInfo().getXMPPDomain() + "/" + from);
			XMPPServer.getInstance().getIQRouter().route( iq );			
        }
        catch(Exception e) {
            Log.error("handleIQSet", e);
        }				
		
        return null;
    }	
}
