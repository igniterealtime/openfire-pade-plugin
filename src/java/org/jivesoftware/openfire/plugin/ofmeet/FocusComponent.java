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
import java.util.*;

public class FocusComponent extends AbstractComponent
{
    private static final Logger Log = LoggerFactory.getLogger(FocusComponent.class);
	private HashMap<String, IQ> requests = new HashMap<>();
	
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

    @Override protected void handleIQError(IQ iq)
    {
		handleResult(iq);	
	}
	
    @Override protected void handleIQResult(IQ iq)
    {
		handleResult(iq);	
	}
	
    @Override public IQ handleIQSet(IQ iq)
    {
		return handleSet(iq);
	}

    @Override public IQ handleIQGet(IQ iq)
    {
		return handleSet(iq);
	}		
	
    private void handleResult(IQ iq)
    {	
        Log.debug("handleResult got \n"+ iq.toString());	
		
        try {	
			JID to = iq.getTo();
			String from = to.getResource();

			IQ iq1 = iq.createCopy();			
			iq1.setTo(from);
			iq1.setFrom("focus." + XMPPServer.getInstance().getServerInfo().getXMPPDomain());
			requests.put(from, iq1);
        }
        catch(Exception e) {
            Log.error("handleResult", e);
        }		
	}		

    private IQ handleSet(IQ iq)
    {
        Log.debug("handleSet got \n"+ iq.toString());
			
        try {				
			String from = iq.getFrom().toString();
			String to = "focus@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain() + "/focus";
			
			for (ClientSession sess : SessionManager.getInstance().getSessions("focus") )
			{
				to = sess.getAddress().toString();	
			}
			IQ iq1 = iq.createCopy();		
			iq1.setTo(to);
			iq1.setFrom("focus." + XMPPServer.getInstance().getServerInfo().getXMPPDomain() + "/" + from);
			XMPPServer.getInstance().getIQRouter().route( iq1 );
			Log.debug("handleSet forwarded \n"+ iq1.toString());	
			
			while (!requests.containsKey(from)) Thread.sleep(1000);
			IQ iq2 = requests.remove(from);
			Log.debug("handleSet sent \n"+ iq2.toString());			
			return iq2;
        }
        catch(Exception e) {
            Log.error("handleSet", e);
        }				
		
        return null;
    }	
}
