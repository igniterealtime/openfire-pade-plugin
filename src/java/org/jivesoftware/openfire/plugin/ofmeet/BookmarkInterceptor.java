/*
 * Copyright (C) 2018 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.ofmeet;

import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.net.URL;

/**
 * Intercepts Bookmarks requests and adds a result to the response that exposes the location in which the 'Meet' web application is running.
 * <p>
 * This implementation borrows heavily from Openfire's org.jivesoftware.openfire.plugin.spark.BookmarkInterceptor.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class BookmarkInterceptor implements PacketInterceptor
{
    public static final String BOOKMARK_NAME_PROPERTYNAME = "ofmeet.bookmarks.webclient.name";
    public static final String BOOKMARK_NAME_DEFAULT = "Video conferencing web client";
    private OfMeetPlugin ofMeetPlugin;

    public BookmarkInterceptor( OfMeetPlugin ofMeetPlugin )
    {
        this.ofMeetPlugin = ofMeetPlugin;
    }

    @Override
    public void interceptPacket( Packet packet,
                                 Session session,
                                 boolean incoming,
                                 boolean processed ) throws PacketRejectedException
    {
        // Interested only in pre-processed, outgoing stanzas.
        if ( processed || incoming )
        {
            return;
        }

        if ( packet == null || session == null )
        {
            return;
        }
		
        // Only interested in stanzas that are either:
        // - from the server itself.
        // - sent 'on behalf of' the user that is the recipient of the stanza ('from' matches session address).
        if ( packet.getFrom() != null && session.getAddress() != null
            && !packet.getFrom().asBareJID().equals( session.getAddress().asBareJID() )
            && !packet.getFrom().toBareJID().equals( XMPPServer.getInstance().getServerInfo().getXMPPDomain() ) )
        {
            return;
        }

        final Element storageElement = findStorageElement( packet );

        if ( storageElement == null )
        {
            return;
        }

        final URL url = ofMeetPlugin.getWebappURL();
        if ( url == null )
        {
            return;
        }

        addBookmark( storageElement, url );
    }

    /**
     * XEP-0048 'Bookmarks' describes a storage element that contains the list of bookmarks that we intend to
     * add to in this method. Such a storage element can be transmitted in a number of different ways, including
     * XEP-0049 "Private XML Storage" and XEP-0223 "Persistent Storage of Private Data via PubSub".
     *
     * @param packet The packet in which to search for a 'storage' element (cannot be null).
     * @return The storage element, or null when no such element was found.
     */
    static Element findStorageElement( final Packet packet )
    {
        if ( packet instanceof IQ )
        {
            final IQ iq = (IQ) packet;
            final Element childElement = iq.getChildElement();
            if ( childElement == null || iq.getType() != IQ.Type.result )
            {
                return null;
            }

            switch ( childElement.getNamespaceURI() )
            {
                // A "Private XML Storage (XEP-0049) Bookmarks" result stanza.
                case "jabber:iq:private":
                    return findStorageElementInPrivateXmlStorage( childElement );

                // a "Persistent Storage of Private Data via PubSub (XEP-0048 / XEP-0223)" Bookmarks result.
                case "http://jabber.org/protocol/pubsub":
                    return findStorageElementInPubsub( childElement );

                default:
                    return null;
            }
        }

        if ( packet instanceof Message )
        {
            final Message message = (Message) packet;

            // Check for a "Persistent Storage of Private Data via PubSub (XEP-0048 / XEP-0223)" Bookmarks event notification.
            return findStorageElementInPubsub( message.getChildElement( "event", "http://jabber.org/protocol/pubsub#event" ) );
        }

        return null;
    }

    /**
     * Find and returns a 'storage' element as defined in XEP-0048 'Bookmarks' from a XML element that is expected to be a child element as defined in
     * XEP-0049 "Private XML Storage"
     *
     * @param privateXmlStorageElement a child element (can be null).
     * @return The XEP-0048-defined 'storage' element that was found in the child element, or null.
     */
    static Element findStorageElementInPrivateXmlStorage( final Element privateXmlStorageElement )
    {
        if ( privateXmlStorageElement == null )
        {
            return null;
        }

        return privateXmlStorageElement.element( QName.get( "storage", "storage:bookmarks" ) );
    }

    /**
     * Find and returns a 'storage' element as defined in XEP-0048 'Bookmarks' from a XML element that is expected to be a child element as defined in
     * XEP-0223 "Persistent Storage of Private Data via PubSub"
     *
     * @param pubsubElement a child element (can be null).
     * @return The XEP-0048-defined 'storage' element that was found in the child element, or null.
     */
    static Element findStorageElementInPubsub( final Element pubsubElement )
    {
        if ( pubsubElement == null )
        {
            return null;
        }

        final Element itemsElement = pubsubElement.element( "items" );
        if ( itemsElement == null || !"storage:bookmarks".equals( itemsElement.attributeValue( "node" ) ) )
        {
            return null;
        }

        final Element itemElement = itemsElement.element( "item" );
        if ( itemElement == null )
        {
            return null;
        }

        return itemElement.element( QName.get( "storage", "storage:bookmarks" ) );
    }

    /**
     * Adds an additional bookmark element to a parent element that contains bookmarks.
     *
     * @param element The parent element, containing zero or more bookmarks (cannot be null).
     * @param url     The URL for which a bookmark is to be added (cannot be null).
     */
    static void addBookmark( final Element element, final URL url )
    {
        final Element urlBookmarkElement = element.addElement( "url" );
        urlBookmarkElement.addAttribute( "name", JiveGlobals.getProperty( BOOKMARK_NAME_PROPERTYNAME, BOOKMARK_NAME_DEFAULT ) );
        urlBookmarkElement.addAttribute( "url", url.toExternalForm() );

        appendSharedElement( urlBookmarkElement );
    }

    /**
     * Adds the shared namespace element to indicate to clients that this bookmark is a shared bookmark.
     *
     * @param bookmarkElement the bookmark to add the shared element to (cannot be null).
     */
    static void appendSharedElement( final Element bookmarkElement )
    {
        bookmarkElement.addElement( "shared_bookmark", "http://jivesoftware.com/jeps/bookmarks" );
    }
}
