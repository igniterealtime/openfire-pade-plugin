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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests that verify the functionality as implemented in {@link BookmarkInterceptor}
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class BookmarkInterceptorTest
{
    /**
     * Verifies that {@link BookmarkInterceptor#findStorageElementInPrivateXmlStorage(Element)} finds a storage element in proper XEP-0049-formatted element.
     */
    @Test
    public void testFindStorageElementInPrivateXmlStorageHappyFlow() throws Exception
    {
        // Setup fixture.
        final Element element = DocumentHelper.parseText(
            "<query xmlns=\"jabber:iq:private\">\n" +
                "  <storage xmlns='storage:bookmarks'>\n" +
                "    <conference name='Council of Oberon'\n" +
                "                autojoin='true'\n" +
                "                jid='council@conference.underhill.org'>\n" +
                "      <nick>Puck</nick>\n" +
                "    </conference>\n" +
                "  </storage>\n" +
                "</query>" ).getRootElement();

        // Execute system under test.
        final Element result = BookmarkInterceptor.findStorageElementInPrivateXmlStorage( element );

        // Verify results.
        assertNotNull( result );
    }

    /**
     * Verifies that {@link BookmarkInterceptor#findStorageElementInPrivateXmlStorage(Element)} gracefully fails when no XEP-0048 storage element is defined
     * in the input that is otherwise a proper XEP-0049-formatted element.
     */
    @Test
    public void testFindStorageElementInPrivateXmlStorageNoXEP0048() throws Exception
    {
        // Setup fixture.
        final Element element = DocumentHelper.parseText(
            "<query xmlns=\"jabber:iq:private\">\n" +
                "  <exodus xmlns=\"exodus:prefs\">\n" +
                "    <defaultnick>Hamlet</defaultnick>\n" +
                "  </exodus>\n" +
                "</query>" ).getRootElement();

        // Execute system under test.
        final Element result = BookmarkInterceptor.findStorageElementInPrivateXmlStorage( element );

        // Verify results.
        assertNull( result );
    }

    /**
     * Verifies that {@link BookmarkInterceptor#findStorageElementInPrivateXmlStorage(Element)} gracefully fails when the input element is something else than
     * a XEP-0049-formatted element.
     */
    @Test
    public void testFindStorageElementInPrivateXmlStorageNoXEP0049() throws Exception
    {
        // Setup fixture.
        final Element element = DocumentHelper.parseText(
            "<foo xmlns=\"bar\"/>" ).getRootElement();

        // Execute system under test.
        final Element result = BookmarkInterceptor.findStorageElementInPrivateXmlStorage( element );

        // Verify results.
        assertNull( result );
    }

    /**
     * Verifies that {@link BookmarkInterceptor#findStorageElementInPubsub(Element)} finds a storage element in a proper Pubsub event notification element.
     */
    @Test
    public void testFindStorageElementInPubsubHappyFlowEventNotification() throws Exception
    {
        // Setup fixture.
        final Element element = DocumentHelper.parseText(
            "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <items node='storage:bookmarks'>\n" +
                "      <item id='current'>\n" +
                "        <storage xmlns='storage:bookmarks'>\n" +
                "          <conference name='The Play&apos;s the Thing'\n" +
                "                      autojoin='true'\n" +
                "                      jid='theplay@conference.shakespeare.lit'>\n" +
                "            <nick>JC</nick>\n" +
                "          </conference>\n" +
                "        </storage>\n" +
                "      </item>\n" +
                "    </items>\n" +
                "  </event>" ).getRootElement();

        // Execute system under test.
        final Element result = BookmarkInterceptor.findStorageElementInPubsub( element );

        // Verify results.
        assertNotNull( result );
    }

    /**
     * Verifies that {@link BookmarkInterceptor#findStorageElementInPubsub(Element)} finds a storage element in a proper Pubsub item retrieval element.
     */
    @Test
    public void testFindStorageElementInPubsubHappyFlowItemRetrieval() throws Exception
    {
        // Setup fixture.
        final Element element = DocumentHelper.parseText(
            "  <pubsub xmlns='http://jabber.org/protocol/pubsub'>\n" +
                "    <items node='storage:bookmarks'>\n" +
                "      <item id='current'>\n" +
                "        <storage xmlns='storage:bookmarks'>\n" +
                "          <conference name='The Play&apos;s the Thing'\n" +
                "                      autojoin='true'\n" +
                "                      jid='theplay@conference.shakespeare.lit'>\n" +
                "            <nick>JC</nick>\n" +
                "          </conference>\n" +
                "        </storage>\n" +
                "      </item>\n" +
                "    </items>\n" +
                "  </pubsub>" ).getRootElement();

        // Execute system under test.
        final Element result = BookmarkInterceptor.findStorageElementInPubsub( element );

        // Verify results.
        assertNotNull( result );
    }
    /**
     * Verifies that {@link BookmarkInterceptor#findStorageElementInPubsub(Element)} gracefully fails when no XEP-0048 storage element is defined
     * in the input that is otherwise a proper pubsub element.
     */
    @Test
    public void testFindStorageElementInPubsubNoXEP0048() throws Exception
    {
        // Setup fixture.
        final Element element = DocumentHelper.parseText(
            "  <event xmlns='http://jabber.org/protocol/pubsub#event'>\n" +
                "    <items node='http://jabber.org/protocol/tune'>\n" +
                "      <item>\n" +
                "        <tune xmlns='http://jabber.org/protocol/tune'>\n" +
                "          <artist>Gerald Finzi</artist>\n" +
                "          <length>255</length>\n" +
                "          <source>Music for \"Love's Labors Lost\" (Suite for small orchestra)</source>\n" +
                "          <title>Introduction (Allegro vigoroso)</title>\n" +
                "          <track>1</track>\n" +
                "        </tune>\n" +
                "      </item>\n" +
                "    </items>\n" +
                "  </event>" ).getRootElement();

        // Execute system under test.
        final Element result = BookmarkInterceptor.findStorageElementInPubsub( element );

        // Verify results.
        assertNull( result );
    }

    /**
     * Verifies that {@link BookmarkInterceptor#findStorageElementInPubsub(Element)} gracefully fails when the input element is something else than
     * a XEP-0223-formatted element.
     */
    @Test
    public void testFindStorageElementInPubsubNoXEP0049() throws Exception
    {
        // Setup fixture.
        final Element element = DocumentHelper.parseText(
            "<foo xmlns=\"bar\"/>" ).getRootElement();

        // Execute system under test.
        final Element result = BookmarkInterceptor.findStorageElementInPubsub( element );

        // Verify results.
        assertNull( result );
    }
}
