package org.jivesoftware.openfire.plugin.ofmeet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.disco.*;
import org.jivesoftware.openfire.interceptor.*;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.session.*;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Version;

import org.dom4j.*;
import org.xmpp.forms.*;
import org.xmpp.forms.FormField.*;
import org.xmpp.packet.*;
import org.json.JSONObject;

public class LobbyMuc implements ServerIdentitiesProvider, ServerFeaturesProvider, PacketInterceptor
{
    private static final Logger Log = LoggerFactory.getLogger(LobbyMuc.class);

    private static final String MUC_NS = "http://jabber.org/protocol/muc";
    private static final String DISCO_INFO_NS = "http://jabber.org/protocol/disco#info";
    private static final String DISPLAY_NAME_REQUIRED_FEATURE = "http://jitsi.org/protocol/lobbyrooms#displayname_required";
    private static final String MUC_NAME = "conference";
    private static final String LOBBY_NAME = "lobby";
    private static final String LOBBY_DESC = "Lobby Chatrooms";
    private static final String LOBBY_IDENTITY_TYPE = "lobbyrooms";
    private static final String NOTIFY_JSON_MESSAGE_TYPE = "lobby-notify";
    private static final String NOTIFY_LOBBY_ENABLED = "LOBBY-ENABLED";
    private static final String NOTIFY_LOBBY_ACCESS_GRANTED = "LOBBY-ACCESS-GRANTED";
    private static final String NOTIFY_LOBBY_ACCESS_DENIED = "LOBBY-ACCESS-DENIED";
    private static final String MAIN_MUC = JiveGlobals.getProperty( "ofmeet.main.muc", MUC_NAME + "." + XMPPServer.getInstance().getServerInfo().getXMPPDomain());
    private static final String LOBBY_MUC = JiveGlobals.getProperty( "ofmeet.lobby.muc", LOBBY_NAME + "." + XMPPServer.getInstance().getServerInfo().getXMPPDomain());

    private MultiUserChatService mucService;
    private MultiUserChatService lobbyService;

    protected void initialize() throws Exception
    {
         if (!XMPPServer.getInstance().getMultiUserChatManager().isServiceRegistered(LOBBY_NAME)) {
            lobbyService = XMPPServer.getInstance().getMultiUserChatManager().createMultiUserChatService(LOBBY_NAME, LOBBY_DESC, false);
            lobbyService.addExtraIdentity("component", LOBBY_MUC, LOBBY_IDENTITY_TYPE);
            lobbyService.addExtraFeature(DISPLAY_NAME_REQUIRED_FEATURE);
        }

        lobbyService = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(LOBBY_NAME);
        mucService = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(MUC_NAME);

        XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeaturesProvider((ServerFeaturesProvider) this);
        XMPPServer.getInstance().getIQDiscoInfoHandler().addServerIdentitiesProvider((ServerIdentitiesProvider) this);

       InterceptorManager.getInstance().addInterceptor(this);
    }

    protected void destroy() throws Exception
    {
        XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerIdentitiesProvider((ServerIdentitiesProvider) this);
        XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerFeature(DISPLAY_NAME_REQUIRED_FEATURE);
        InterceptorManager.getInstance().removeInterceptor(this);
    }

    @Override public Iterator<Element> getIdentities() {
        Element identity = DocumentHelper.createElement("identity");
        identity.addAttribute("category", "component");
        identity.addAttribute("name", LOBBY_MUC);
        identity.addAttribute("type", LOBBY_IDENTITY_TYPE);
        return Collections.singleton(identity).iterator();
    }

    @Override public Iterator<String> getFeatures() {
        ArrayList<String> features = new ArrayList<>();
        features.add(DISPLAY_NAME_REQUIRED_FEATURE);
        return features.iterator();
    }

    private void broadcast_json_msg(JID to, JID from, JSONObject jsonMsg)
    {
        jsonMsg.put("type", NOTIFY_JSON_MESSAGE_TYPE);
        Message message = new Message();
        message.setFrom(from);
        message.setTo(to);
        message.setType(Message.Type.groupchat);
        Element json = message.addChildElement("json-message", "http://jitsi.org/jitmeet");
        json.setText(jsonMsg.toString());
        XMPPServer.getInstance().getRoutingTable().routePacket(to, message, true);
    }

    private void notify_configuration_change(JID to, String from)
    {
        Message message = new Message();
        message.setFrom(from);
        message.setTo(to);
        message.setType(Message.Type.groupchat);
        Element x = message.addChildElement("x", "http://jabber.org/protocol/muc#user");
        x.addElement("status").addAttribute("code", "104");
        XMPPServer.getInstance().getRoutingTable().routePacket(to, message, true);
    }

    private void notify_lobby_enabled(JID to, JID from, boolean value)
    {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put("event", NOTIFY_LOBBY_ENABLED);
        jsonMsg.put("value", value);
        broadcast_json_msg(to, from, jsonMsg);
        notify_configuration_change(from, to.toBareJID());
   }

    private void notify_lobby_access(JID room, JID from, String to, boolean granted)
    {
        JSONObject jsonMsg = new JSONObject();
        jsonMsg.put("value", to);
        jsonMsg.put("name", to);

        if (granted) {
            jsonMsg.put("event", NOTIFY_LOBBY_ACCESS_GRANTED);
        } else {
            jsonMsg.put("event", NOTIFY_LOBBY_ACCESS_DENIED);
        }
        broadcast_json_msg(room, from, jsonMsg);
    }

    private boolean isMembersOnly(Element childElement)
    {
        boolean result = false;
        Element formElement = childElement.element(QName.get("x", "jabber:x:data"));

        if (formElement != null) {
            DataForm completedForm = new DataForm(formElement);
            FormField field = completedForm.getField("muc#roomconfig_membersonly");

            if (field != null) {
                final String value = field.getFirstValue();
                if ( "1".equals( value ) || "true".equals( value ) ) result = true;
            }
        }
        return result;
    }

    private boolean featureExists(Element element, String feature)
    {
        final Iterator<Element> features = element.elementIterator("feature");

        while (features.hasNext())
        {
            Element featureElement = features.next();
            String featureVar = featureElement.attributeValue("var");
            if (featureVar.equalsIgnoreCase(feature)) return true;
        }

        return false;
    }


    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
        if (!processed && lobbyService != null) {
            if (packet instanceof IQ) {
                interceptIQ((IQ) packet, session, incoming, processed);
            } else if (packet instanceof Presence) {
                interceptPresence((Presence) packet, session, incoming, processed);
            } else if (packet instanceof Message) {
                interceptMessage((Message) packet, session, incoming, processed);
            }
        }
    }

    protected void interceptIQ(IQ iq, Session session, boolean incoming, boolean processed)
            throws PacketRejectedException {
        Element childElement = iq.getChildElement();
        if (childElement != null) {
            String namespace = childElement.getNamespaceURI();
            if ("http://jabber.org/protocol/muc#owner".equals(namespace)) {
                if (isMembersOnly(childElement)) {
                    final String roomName = iq.getTo().getNode();
                    Log.debug("lobbyroom creating room " + roomName);

                    MUCRoom lobbyRoom, mucRoom;
                    try {
                        mucRoom = mucService.getChatRoom(roomName, iq.getFrom());
                        lobbyRoom = lobbyService.getChatRoom(roomName, iq.getFrom());
                        lobbyRoom.setPersistent(false);
                        lobbyRoom.setPublicRoom(true);
                        lobbyRoom.setPassword(mucRoom.getPassword());
                        lobbyRoom.unlock(lobbyRoom.getRole());
						lobbyService.syncChatRoom(lobbyRoom);							
                        notify_lobby_enabled(iq.getTo(), iq.getFrom(), true);
                    } catch (Exception e) {
                        Log.error("Cannot create MUC room", e);
                        return;
                    }
                } else {
                    notify_lobby_enabled(iq.getTo(), iq.getFrom(), false);
                }
            } else if ("http://jabber.org/protocol/disco#info".equals(namespace)) {
                if (featureExists(childElement, "muc_membersonly") && iq.getType() == IQ.Type.result && !incoming
                        && MAIN_MUC.equals(iq.getFrom().getDomain())) {
                    Element formElement = childElement.element(QName.get("x", "jabber:x:data"));

                    if (formElement != null) {
                        Log.debug("lobbyroom updating room " + iq.getFrom() + " config with muc#roominfo_lobbyroom");
                        DataForm form = new DataForm(formElement);
                        form.addField("muc#roominfo_lobbyroom", "Lobby room jid", FormField.Type.hidden)
                                .addValue(iq.getFrom().getNode() + "@" + LOBBY_MUC);
                    }
                } else if (iq.getType() == IQ.Type.get && LOBBY_MUC.equals(iq.getTo().toString())
                        && childElement.attribute("node") != null) {
                    if (childElement.attribute("node").getStringValue().equals(LOBBY_IDENTITY_TYPE)) {
                        Log.debug("lobbyroom remove node attribute from disco#info for " + LOBBY_MUC);
                        childElement.remove(childElement.attribute("node"));
                    }
                }
            }
        }
    }

    protected void interceptPresence(Presence presence, Session session, boolean incoming, boolean processed)
            throws PacketRejectedException {
        Element childElement;
        if (presence.getError() != null && presence.getError().getElement() != null
                && presence.getType() == Presence.Type.error && !incoming) {
            childElement = presence.getChildElement("x", "http://jabber.org/protocol/muc");
            if (childElement != null && presence.getFrom() != null) {
                String lobbyRoom = presence.getFrom().getNode() + "@" + LOBBY_MUC;
                Log.debug("lobbyroom add room to disco#info for " + lobbyRoom);
                childElement.addElement("lobbyroom").setText(lobbyRoom);
            }
        } else if (presence.getType() == Presence.Type.unavailable) {
            childElement = presence.getChildElement("x", "http://jabber.org/protocol/muc#user");
            if (childElement != null) {
                Element status = childElement.element("status");
                if (status != null && status.attribute("code").getStringValue().equals("307")) {
                    // Processes in a normal room
                    try {
                        JID kicked = new JID(childElement.element("item").attribute("jid").getStringValue());
                        if (presence.getTo().compareTo(kicked) == 0) {
                            // Add self-presence code (110) to kick presence
                            childElement.addElement("status").addAttribute("code", "110");

                            // Remove the user from the allowed list                            
                            JID roomJID = presence.getFrom();
							
							if (roomJID != null && roomJID.getNode() != null) {
								MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomJID.getNode());
								List<Presence> addNonePresence = room.addNone(kicked, room.getRole());

								// Send a presence to other room members
								for (Presence p : addNonePresence) {
									room.send(p, room.getRole());
								}
							}
                        }
                    } catch (Exception e) {
                        Log.error("kick failure", e);
                    }

                    // Processes only in lobby
                    if (LOBBY_MUC.equals(presence.getFrom().getDomain())) {
                        Element nickElement = presence.getChildElement("nick", "http://jabber.org/protocol/nick");
                        if (nickElement != null) {
                            try {
                                Element actor = childElement.element("item").element("actor");
                                JID room = new JID(presence.getFrom().getNode() + "@" + MAIN_MUC);
                                JID from = new JID(actor.attribute("jid").getStringValue());
                                String invitee = presence.getFrom().toString();
                                Log.debug("lobbyroom participant refused for " + room + " by " + from);
                                notify_lobby_access(room, from, invitee, false);
                            } catch (Exception e) {
                                Log.error("loobyroom kick failure", e);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void interceptMessage(Message message, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
        Element childElement = message.getChildElement("x", "http://jabber.org/protocol/muc#user");

        if (childElement != null && MAIN_MUC.equals(message.getTo().getDomain())) {
            Element inviteElement = childElement.element("invite");

            if (inviteElement != null) {
                Log.debug("lobbyroom participant accepted for " + message.getTo() + " by " + message.getFrom());
                String invitee = inviteElement.attribute("to").getStringValue();
                notify_lobby_access(message.getTo(), message.getFrom(), invitee, true);
            }
        }
    }
}
/*
Test data
---------------

<presence to="7tv76q4hdp@localhost/7tv76q4hdp" from="soyu@lobby.localhost/40077f0f" type="unavailable">
<videomuted xmlns="http://jitsi.org/jitmeet/video">false</videomuted>
<c xmlns="http://jabber.org/protocol/caps" hash="sha-1" node="http://igniterealtime.org/ofmeet/jitsi-meet/" ver="cWj8xSCR2vP2KMorJrpHIw9Q/jA="></c>
<nick xmlns="http://jabber.org/protocol/nick">josie266789</nick>
<email>dele@4ng.net</email>
<x xmlns="http://jabber.org/protocol/muc#user">
    <item jid="7tv76q4hdp@localhost/7tv76q4hdp" affiliation="none" role="none">
        <reason>You have been kicked.</reason>
        <actor jid="ae18nkotau@localhost" nick="41a51c71"/>
    </item>
    <status code="307"/>
</x></presence>

<message id="fa76adcb-18de-49e7-9b04-53e12710f2d4:sendIQ" to="soyu@conference.localhost" from="ae18nkotau@localhost/ae18nkotau">
<x xmlns="http://jabber.org/protocol/muc#user">
<invite to="ahmpoqew97@localhost/ahmpoqew97"/>
</x></message>

<iq from="47ielcbnfj@localhost/47ielcbnfj" id="2b816cb8-e475-46cb-a9e9-c8a6cad4f313:sendIQ" to="lobby.localhost" type="get">
<query xmlns="http://jabber.org/protocol/disco#info" node="lobbyrooms"></query>
</iq>

<presence
    from='coven@chat.shakespeare.lit/thirdwitch'
    id='n13mt3l'
    to='hag66@shakespeare.lit/pda'
    type='error'>
  <x xmlns='http://jabber.org/protocol/muc'/>
  <error by='coven@chat.shakespeare.lit' type='auth'>
    <registration-required xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>
  </error>
</presence>

<presence to="9r21pr9sgx@localhost/9r21pr9sgx" from="heya@conference.localhost/36dd56cd" type="error">
<x xmlns="http://jabber.org/protocol/muc"></x>
<stats-id>Zola-Lsx</stats-id>
<c xmlns="http://jabber.org/protocol/caps" hash="sha-1" node="http://igniterealtime.org/ofmeet/jitsi-meet/" ver="cWj8xSCR2vP2KMorJrpHIw9Q/jA="></c>
<avatar-id>be1db8e2e41a7043d6ffc64a6c4aa5da</avatar-id><avatar-url>data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAABIklEQVRYR2N8cbjjP8MAAsZRB4yGwGgIjIbAaAiQEwJsAvIM/GreDExsvAzfX15i+HR7O9mFOVlFMY+8LQOnhCEDw/8/YIs/3trK8OvDQ7IcQZYDhI2SGZhY2Bl+fXzCwC6ixvDtyUmGLw8P08cBHGLaDHzKbgy/Pz9n+P7qMpz9/soK+jiAT9WTAeQImK9hoUFuNJAcBegWghzEKa5HdmIkyQGgxMclY87w49VVeMqH5Yh/f34yvD03l+RoIMkBgjoRDCALsYH/f38xfLq7C+w4UgDRDsDnU2whQ6wjiHYAPktgjiOnTCDaAaDEx8zOhzOYQdHDyi9DcplAlANgef/vz084ExoshH5/fMJASplAlAOIjU9y1I06YDQERkNgNAQAR8jmYb5KXxsAAAAASUVORK5CYII=</avatar-url>
<email>dele@4ng.net</email>
<nick xmlns="http://jabber.org/protocol/nick">josie2</nick>
<audiomuted xmlns="http://jitsi.org/jitmeet/audio">false</audiomuted>
<videoType xmlns="http://jitsi.org/jitmeet/video">camera</videoType>
<videomuted xmlns="http://jitsi.org/jitmeet/video">false</videomuted>
<error code="407" type="auth"><registration-required xmlns="urn:ietf:params:xml:ns:xmpp-stanzas"/>
</error></presence>

Overview
----------------

    Component "lobbyrooms.jitmeet.example.com" "muc"
        storage = "memory"
        muc_room_cache_size = 1000
        restrict_room_creation = true
        muc_room_locking = false
        muc_room_default_public_jids = true

    The lobby rooms muc component is a separate one, on which rooms can be created only by admins.

    When a moderator enables lobby feature, just changes the muc config to be set to members_only https://xmpp.org/extensions/xep-0045.html#enter-members
    Also a shared password can be used by adding it as muc#roomcroomconfig_lobbypassword.
    What happens in the prosody module is that when this members_only setting is detected a corresponding lobby room is created in the custom component, and if there is a shared password it is stored in the main room object, so we can compare it later.
    The jid of the created lobby room is set in the room object and is distributed through the muc config so other moderators can join it (muc#roominfo_lobbyroom).

    When a user tries to join the standard error for joining a members_only room is detected: https://xmpp.org/extensions/xep-0045.html#enter-members (we slightly modify that adding the lobby room jid to it) and the participant can join the lobby room by providing display name, and email which is optional or can provide a shared password to try to skip the lobby.

    The moderators see people joining the lobby room and can approve/deny. Approving is by inviting them to the room, using https://xmpp.org/extensions/xep-0045.html#invite-mediated
    Deny is just kicking the user from the lobby room.

    Another part which is in the backend is that the lobby room is filtering presences between participants and only moderators can see those.


Prosody module for lobby in Lua
--------------------------------

-- This module added under the main virtual host domain
-- It needs a lobby muc component
--
-- VirtualHost "jitmeet.example.com"
-- modules_enabled = {
--     "muc_lobby_rooms"
-- }
-- lobby_muc = "lobby.jitmeet.example.com"
-- main_muc = "conference.jitmeet.example.com"
--
-- Component "lobby.jitmeet.example.com" "muc"
--     storage = "memory"
--     muc_room_cache_size = 1000
--     restrict_room_creation = true
--     muc_room_locking = false
--     muc_room_default_public_jids = true
--
-- we use async to detect Prosody 0.10 and earlier
local have_async = pcall(require, 'util.async');

if not have_async then
    module:log('warn', 'Lobby rooms will not work with Prosody version 0.10 or less.');
    return;
end

local formdecode = require "util.http".formdecode;
local jid_split = require 'util.jid'.split;
local jid_bare = require 'util.jid'.bare;
local json = require 'util.json';
local filters = require 'util.filters';
local st = require 'util.stanza';
local MUC_NS = 'http://jabber.org/protocol/muc';
local DISCO_INFO_NS = 'http://jabber.org/protocol/disco#info';
local DISPLAY_NAME_REQUIRED_FEATURE = 'http://jitsi.org/protocol/lobbyrooms#displayname_required';
local LOBBY_IDENTITY_TYPE = 'lobbyrooms';
local NOTIFY_JSON_MESSAGE_TYPE = 'lobby-notify';
local NOTIFY_LOBBY_ENABLED = 'LOBBY-ENABLED';
local NOTIFY_LOBBY_ACCESS_GRANTED = 'LOBBY-ACCESS-GRANTED';
local NOTIFY_LOBBY_ACCESS_DENIED = 'LOBBY-ACCESS-DENIED';

local is_healthcheck_room = module:require 'util'.is_healthcheck_room;

local main_muc_component_config = module:get_option_string('main_muc');
if main_muc_component_config == nil then
    module:log('error', 'lobby not enabled missing main_muc config');
    return ;
end
local lobby_muc_component_config = module:get_option_string('lobby_muc');
if lobby_muc_component_config == nil then
    module:log('error', 'lobby not enabled missing lobby_muc config');
    return ;
end

local whitelist;
local check_display_name_required;
local function load_config()
    whitelist = module:get_option_set('muc_lobby_whitelist', {});
    check_display_name_required
        = module:get_option_boolean('muc_lobby_check_display_name_required', true);
end
load_config();

local lobby_muc_service;
local main_muc_service;

-- Checks whether there is status in the <x node
function check_status(muc_x, status)
    if not muc_x then
        return false;
    end

    for statusNode in muc_x:childtags('status') do
        if statusNode.attr.code == status then
            return true;
        end
    end

    return false;
end

function broadcast_json_msg(room, from, json_msg)
    json_msg.type = NOTIFY_JSON_MESSAGE_TYPE;

    local occupant = room:get_occupant_by_real_jid(from);
    if occupant then
        room:broadcast_message(
            st.message({ type = 'groupchat', from = occupant.nick })
              :tag('json-message', {xmlns='http://jitsi.org/jitmeet'})
              :text(json.encode(json_msg)):up());
    end
end

-- Sends a json message notifying for lobby enabled/disable
-- the message from is the actor that did the operation
function notify_lobby_enabled(room, actor, value)
    broadcast_json_msg(room, actor, {
        event = NOTIFY_LOBBY_ENABLED,
        value = value
    });
end

-- Sends a json message notifying that the jid was granted/denied access in lobby
-- the message from is the actor that did the operation
function notify_lobby_access(room, actor, jid, granted)
    local notify_json = {
        value = jid
    };
    if granted then
        notify_json.event = NOTIFY_LOBBY_ACCESS_GRANTED;
    else
        notify_json.event = NOTIFY_LOBBY_ACCESS_DENIED;
    end

    broadcast_json_msg(room, actor, notify_json);
end

function filter_stanza(stanza)
    if not stanza.attr or not stanza.attr.from or not main_muc_service then
        return stanza;
    end
    -- Allow self-presence (code=110)
    local node, from_domain = jid_split(stanza.attr.from);

    if from_domain == lobby_muc_component_config then
        if stanza.name == 'presence' then
            local muc_x = stanza:get_child('x', MUC_NS..'#user');

            if check_status(muc_x, '110') then
                return stanza;
            end

            -- check is an owner, only owners can receive the presence
            local room = main_muc_service.get_room_from_jid(jid_bare(node .. '@' .. main_muc_component_config));
            if room.get_affiliation(room, stanza.attr.to) == 'owner' then
                return stanza;
            end

            return nil;
        elseif stanza.name == 'iq' and stanza:get_child('query', DISCO_INFO_NS) then
            -- allow disco info from the lobby component
            return stanza;
        end

        return nil;
    else
        return stanza;
    end
end
function filter_session(session)
    if session.host and session.host == module.host then
        -- domain mapper is filtering on default priority 0, and we need it after that
        filters.add_filter(session, 'stanzas/out', filter_stanza, -1);
    end
end

-- process a host module directly if loaded or hooks to wait for its load
function process_host_module(name, callback)
    local function process_host(host)
        if host == name then
            callback(module:context(host), host);
        end
    end

    if prosody.hosts[name] == nil then
        module:log('debug', 'No host/component found, will wait for it: %s', name)

        -- when a host or component is added
        prosody.events.add_handler('host-activated', process_host);
    else
        process_host(name);
    end
end

-- operates on already loaded lobby muc module
function process_lobby_muc_loaded(lobby_muc, host_module)
    module:log('debug', 'Lobby muc loaded');
    lobby_muc_service = lobby_muc;

    -- enable filtering presences in the lobby muc rooms
    filters.add_filter_hook(filter_session);

    -- Advertise lobbyrooms support on main domain so client can pick up the address and use it
    module:add_identity('component', LOBBY_IDENTITY_TYPE, lobby_muc_component_config);

    -- Tag the disco#info response with a feature that display name is required
    -- when the conference name from the web request has a lobby enabled.
    host_module:hook('host-disco-info-node', function (event)
        local session, reply, node = event.origin, event.reply, event.node;
        if node == LOBBY_IDENTITY_TYPE
            and session.jitsi_web_query_room
            and main_muc_service
            and check_display_name_required then
            local room = main_muc_service.get_room_from_jid(
                jid_bare(session.jitsi_web_query_room .. '@' .. main_muc_component_config));
            if room and room._data.lobbyroom then
                reply:tag('feature', { var = DISPLAY_NAME_REQUIRED_FEATURE }):up();
            end
        end
        event.exists = true;
    end);

    local room_mt = lobby_muc_service.room_mt;
    -- we base affiliations (roles) in lobby muc component to be based on the roles in the main muc
    room_mt.get_affiliation = function(room, jid)
        if not room.main_room then
            module:log('error', 'No main room(%s) for %s!', room.jid, jid);
            return 'none';
        end

        -- moderators in main room are moderators here
        local role = room.main_room.get_affiliation(room.main_room, jid);
        if role then
            return role;
        end

        return 'none';
    end

    -- listens for kicks in lobby room, 307 is the status for kick according to xep-0045
    host_module:hook('muc-broadcast-presence', function (event)
        local actor, occupant, room, x = event.actor, event.occupant, event.room, event.x;
        if check_status(x, '307') then
            -- we need to notify in the main room
            notify_lobby_access(room.main_room, actor, occupant.nick, false);
        end
    end);
end

-- process or waits to process the lobby muc component
process_host_module(lobby_muc_component_config, function(host_module, host)
    -- lobby muc component created
    module:log('info', 'Lobby component loaded %s', host);

    local muc_module = prosody.hosts[host].modules.muc;
    if muc_module then
        process_lobby_muc_loaded(muc_module, host_module);
    else
        module:log('debug', 'Will wait for muc to be available');
        prosody.hosts[host].events.add_handler('module-loaded', function(event)
            if (event.module == 'muc') then
                process_lobby_muc_loaded(prosody.hosts[host].modules.muc, host_module);
            end
        end);
    end
end);

-- process or waits to process the main muc component
process_host_module(main_muc_component_config, function(host_module, host)
    main_muc_service = prosody.hosts[host].modules.muc;

    -- hooks when lobby is enabled to create its room, only done here or by admin
    host_module:hook('muc-config-submitted', function(event)
        local actor, room = event.actor, event.room;
        local members_only = event.fields['muc#roomconfig_membersonly'] and true or nil;
        if members_only then
            local node = jid_split(room.jid);

            local lobby_room_jid = node .. '@' .. lobby_muc_component_config;
            if not lobby_muc_service.get_room_from_jid(lobby_room_jid) then
                local new_room = lobby_muc_service.create_room(lobby_room_jid);
                new_room.main_room = room;
                room._data.lobbyroom = new_room;
                event.status_codes['104'] = true;
                notify_lobby_enabled(room, actor, true);
            end
        elseif room._data.lobbyroom then
            room._data.lobbyroom:destroy(room.jid, 'Lobby room closed.');
            room._data.lobbyroom = nil;
            notify_lobby_enabled(room, actor, false);
        end
    end);
    host_module:hook('muc-room-destroyed',function(event)
        local room = event.room;
        if room._data.lobbyroom then
            room._data.lobbyroom:destroy(nil, 'Lobby room closed.');
            room._data.lobbyroom = nil;
        end
    end);
    host_module:hook('muc-disco#info', function (event)
        local room = event.room;
        if (room._data.lobbyroom and room:get_members_only()) then
            table.insert(event.form, {
                name = 'muc#roominfo_lobbyroom';
                label = 'Lobby room jid';
                value = '';
            });
            event.formdata['muc#roominfo_lobbyroom'] = room._data.lobbyroom.jid;
        end
    end);

    host_module:hook('muc-occupant-pre-join', function (event)
        local room, stanza = event.room, event.stanza;

        if is_healthcheck_room(room.jid) or not room:get_members_only() then
            return;
        end

        local join = stanza:get_child('x', MUC_NS);
        if not join then
            return;
        end

        local invitee = event.stanza.attr.from;
        local invitee_bare_jid = jid_bare(invitee);
        local _, invitee_domain = jid_split(invitee);
        local whitelistJoin = false;

        -- whitelist participants
        if whitelist:contains(invitee_domain) or whitelist:contains(invitee_bare_jid) then
            whitelistJoin = true;
        end

        local password = join:get_child_text('password', MUC_NS);
        if password and room:get_password() and password == room:get_password() then
            whitelistJoin = true;
        end

        if whitelistJoin then
            local affiliation = room:get_affiliation(invitee);
            if not affiliation or affiliation == 0 then
                event.occupant.role = 'participant';
                room:set_affiliation(true, invitee_bare_jid, 'member');
                room:save();

                return;
            end
        end

        -- we want to add the custom lobbyroom field to fill in the lobby room jid
        local invitee = event.stanza.attr.from;
        local affiliation = room:get_affiliation(invitee);
        if not affiliation or affiliation == 'none' then
            local reply = st.error_reply(stanza, 'auth', 'registration-required'):up();
            reply.tags[1].attr.code = '407';
            reply:tag('x', {xmlns = MUC_NS}):up();
            reply:tag('lobbyroom'):text(room._data.lobbyroom.jid);
            event.origin.send(reply:tag('x', {xmlns = MUC_NS}));
            return true;
        end
    end, -4); -- the default hook on members_only module is on -5

    -- listens for invites for participants to join the main room
    host_module:hook('muc-invite', function(event)
        local room, stanza = event.room, event.stanza;
        local invitee = stanza.attr.to;
        local from = stanza:get_child('x', 'http://jabber.org/protocol/muc#user')
            :get_child('invite').attr.from;

        if room._data.lobbyroom then
            local occupant = room._data.lobbyroom:get_occupant_by_real_jid(invitee);
            if occupant then
                notify_lobby_access(room, from, occupant.nick, true);
            end
        end
    end);
end);

-- Extract 'room' param from URL when session is created
function update_session(event)
    local session = event.session;

    if session.jitsi_web_query_room then
        -- no need for an update
        return;
    end

    local query = event.request.url.query;
    if query ~= nil then
        local params = formdecode(query);
        -- The room name and optional prefix from the web query
        session.jitsi_web_query_room = params.room;
        session.jitsi_web_query_prefix = params.prefix or '';
    end
end

module:hook_global('bosh-session', update_session);
module:hook_global('websocket-session', update_session);
module:hook_global('config-reloaded', load_config);
*/
