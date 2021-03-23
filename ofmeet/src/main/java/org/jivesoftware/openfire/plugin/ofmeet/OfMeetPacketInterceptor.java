package org.jivesoftware.openfire.plugin.ofmeet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.*;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.session.*;

import org.dom4j.*;
import org.xmpp.packet.*;

public class OfMeetPacketInterceptor implements PacketInterceptor {
    private static final Logger Log = LoggerFactory.getLogger(LobbyMuc.class);

    protected void initialize() throws Exception {
        InterceptorManager.getInstance().addInterceptor(this);
    }

    protected void destroy() throws Exception {
        InterceptorManager.getInstance().removeInterceptor(this);
    }

    protected void interceptIQ(IQ iq, Session session, boolean incoming, boolean processed)
            throws PacketRejectedException {
    }

    protected void interceptPresence(Presence presence, Session session, boolean incoming, boolean processed)
            throws PacketRejectedException {
        // Add self-presence code (110) to kick presence
        if (presence.getType() == Presence.Type.unavailable) {
            Element childElement = presence.getChildElement("x", "http://jabber.org/protocol/muc#user");
            if (childElement != null) {
                Element status = childElement.element("status");
                if (status != null && status.attribute("code").getStringValue().equals("307")) {
                    try {
                        JID kicked = new JID(childElement.element("item").attribute("jid").getStringValue());
                        if (presence.getTo().compareTo(kicked) == 0) {
                            childElement.addElement("status").addAttribute("code", "110");

                            // Remove the user from the allowed list
                            IQ iq = new IQ(IQ.Type.set);
                            Element frag = iq.setChildElement("query", "http://jabber.org/protocol/muc#admin");
                            Element item = frag.addElement("item");
                            item.addAttribute("affiliation", "none");
                            item.addAttribute("jid", kicked.toFullJID());

                            JID roomJID = presence.getFrom();
                            MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager()
                                    .getMultiUserChatService(roomJID).getChatRoom(roomJID.getNode());

                            // Send the IQ packet that will modify the room's configuration
                            room.getIQAdminHandler().handleIQ(iq, room.getRole());
                        }
                    } catch (Exception e) {
                        Log.error("kick failure", e);
                    }
                }
            }
        }
    }

    protected void interceptMessage(Message message, Session session, boolean incoming, boolean processed)
            throws PacketRejectedException {
    }

    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
            throws PacketRejectedException {
        if (packet instanceof IQ) {
            interceptIQ((IQ) packet, session, incoming, processed);
        } else if (packet instanceof Presence) {
            interceptPresence((Presence) packet, session, incoming, processed);
        } else if (packet instanceof Message) {
            interceptMessage((Message) packet, session, incoming, processed);
        }
    }
}
