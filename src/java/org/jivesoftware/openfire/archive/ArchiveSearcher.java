/*
 * Copyright (C) 2008 Jive Software. All rights reserved.
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

package org.jivesoftware.openfire.archive;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.jivesoftware.database.CachedPreparedStatement;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.util.JiveGlobals;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

/**
 * Searches archived conversations. If conversation archiving is not enabled,
 * this class does nothing. Searches may or may not include keyword searching. When
 * keywords are used, the search is executed against the Lucene index. When keywords
 * are not used, the search is database driven (e.g., "get all conversations between
 * two users over the past year").
 *
 * @see ArchiveIndexer
 * @author Matt Tucker
 */
public class ArchiveSearcher implements Startable {

    private static final Logger Log = LoggerFactory.getLogger(ArchiveSearch.class);

    private Directory directory;
    private IndexSearcher searcher;

    /**
     * Constructs a new archive searcher.
     *
     */
    public ArchiveSearcher() {

    }

    public void start() {

    }

    public void stop() {

    }

    /**
     * Searches the archive using the specified search. The {@link ArchiveSearch} class
     * is used to encapsulate all information about a search.
     *
     * @param search the search.
     * @return a Collection of conversations that match the search query.
     */
    public Collection<Conversation> search(ArchiveSearch search) {
        // If the search has a query string it will be driven by Lucene. Otherwise
        if (search.getQueryString() != null) {
            return luceneSearch(search);
        }
        else {
            return databaseSearch(search);
        }
    }

    /**
     * Searches the Lucene index for all archived conversations using the specified search.
     *
     * @param search the search.
     * @return the collection of conversations that match the search.
     */
    private Collection<Conversation> luceneSearch(ArchiveSearch search) {
        Log.debug( "Executing new Lucene search for query string {}", search.getQueryString() );
        try {
            File searchDir = new File(JiveGlobals.getHomeDirectory() + File.separator + MonitoringConstants.NAME + File.separator + "search");

            if (!searchDir.exists())
            {
                Log.error("Search folder missing " + searchDir);
                return Collections.emptySet();
            }
            try {
                directory = FSDirectory.open(searchDir.toPath());
            }
            catch (IOException ioe) {
                Log.error(ioe.getMessage(), ioe);
                return Collections.emptySet();
            }

            if (searcher == null) {
                DirectoryReader reader = DirectoryReader.open(directory);
                searcher = new IndexSearcher(reader);
            }
            // See if the searcher needs to be closed due to the index being updated.
            final DirectoryReader replacement = DirectoryReader.openIfChanged((DirectoryReader) searcher.getIndexReader());
            if ( replacement != null )
            {
                Log.debug("Returning new Index Searcher (as index was updated)");
                searcher.getIndexReader().close();
                searcher = new IndexSearcher(replacement);
            }

            final StandardAnalyzer analyzer = new StandardAnalyzer();

            // Create the query based on the search terms.
            Query query = new QueryParser("text", analyzer).parse(search.getQueryString());

            // See if the user wants to sort on something other than relevance. If so, we need
            // to tell Lucene to do sorting. Default to a null sort so that it has no
            // effect if sorting hasn't been selected.
            Sort sort = null;
            if (search.getSortField() != ArchiveSearch.SortField.relevance) {
                if (search.getSortField() == ArchiveSearch.SortField.date) {
                    sort = new Sort(new SortField("date", SortField.Type.STRING, search.getSortOrder() == ArchiveSearch.SortOrder.descending));
                    Log.debug( "... applying sort: {}", sort );
                }
            }

            // See if we need to filter on date. Default to a null filter so that it has
            // no effect if date filtering hasn't been selected.
            if (search.getDateRangeMin() != null || search.getDateRangeMax() != null) {
                String min = null;
                if (search.getDateRangeMin() != null) {
                    min = DateTools.dateToString(search.getDateRangeMin(), DateTools.Resolution.DAY);
                }
                String max = null;
                if (search.getDateRangeMax() != null) {
                    max = DateTools.dateToString(search.getDateRangeMax(), DateTools.Resolution.DAY);
                }

                if (max != null || min != null) {
                    // ENT-271: don't include upper or lower bound if these elements are null
                    final TermRangeQuery dateRangeQuery = TermRangeQuery.newStringRange("date", min, max, min != null, max != null);
                    Log.debug( "... limiting to range: {}", dateRangeQuery );
                    query = new BooleanQuery.Builder()
                        .add(query, BooleanClause.Occur.MUST)
                        .add(dateRangeQuery, BooleanClause.Occur.MUST)
                        .build();
                }
            }

            // See if we need to match external conversations. This will only be true
            // when less than two conversation participants are specified and external
            // wildcard matching is enabled.
            Collection<JID> participants = search.getParticipants();
            if (search.getParticipants().size() < 2 && search.isExternalWildcardMode()) {
                TermQuery externalQuery = new TermQuery(new Term("external", "true"));
                Log.debug( "... enabling 'external' wildcard matching: {}", true );

                // Add this query to the existing query.
                query = new BooleanQuery.Builder()
                    .add(query, BooleanClause.Occur.MUST)
                    .add(externalQuery, BooleanClause.Occur.MUST)
                    .build();
            }

            // See if we need to restrict the search to certain users.
            if (!participants.isEmpty()) {
                if (participants.size() == 1) {
                    JID jid = participants.iterator().next().asBareJID();
                    Query participantQuery = new QueryParser("jid", analyzer).parse(jid.toString());
                    Log.debug( "... restricting to participant: {}", jid );

                    // Add this query to the existing query.
                    query = new BooleanQuery.Builder()
                        .add(query, BooleanClause.Occur.MUST)
                        .add(participantQuery, BooleanClause.Occur.MUST)
                        .build();
                }
                // Otherwise there are two participants.
                else {
                    Iterator<JID> iter = participants.iterator();
                    String participant1 = iter.next().toBareJID();
                    String participant2 = iter.next().toBareJID();
                    if ( iter.hasNext() ) {
                        Log.warn( "More participants available in search than are used!" );
                    }

                    Log.debug( "... restricting to participants: {} and {}", participant1, participant2 );
                    final BooleanQuery participantQuery = new BooleanQuery.Builder()
                        .add(new QueryParser("jid", analyzer).parse(participant1), BooleanClause.Occur.MUST)
                        .add(new QueryParser("jid", analyzer).parse(participant2), BooleanClause.Occur.MUST)
                        .build();

                    // Add this query to the existing query.
                    query = new BooleanQuery.Builder()
                        .add(query, BooleanClause.Occur.MUST)
                        .add(participantQuery, BooleanClause.Occur.MUST)
                        .build();
                }
            }

            int startIndex = search.getStartIndex();
            int endIndex = startIndex + search.getNumResults() - 1;

            if (((endIndex - startIndex) + 1) <= 0) {
                Log.debug( "... end index of query ({}) is positioned is not larger then the start index ({}). Returning empty result.", endIndex, startIndex );
                return Collections.emptyList();
            }

            TopDocs hits;
            if ( sort != null ) {
                hits = searcher.search(query, endIndex + 1, sort);
            } else {
                hits = searcher.search(query, endIndex + 1 );
            }

            return new LuceneQueryResults(searcher, hits, startIndex, endIndex);
        }
        catch (ParseException pe) {
            Log.error(pe.getMessage(), pe);
            return Collections.emptySet();
        }
        catch (IOException ioe) {
            Log.error(ioe.getMessage(), ioe);
            return Collections.emptySet();
        }
    }

    /**
     * Searches the database for all archived conversations using the specified search.
     *
     * @param search the search.
     * @return the collection of conversations that match the search.
     */
    private Collection<Conversation> databaseSearch(ArchiveSearch search) {
        CachedPreparedStatement cachedPstmt = new CachedPreparedStatement();

        // Build the SQL
        StringBuilder query = new StringBuilder(160);
        query.append("SELECT DISTINCT ofConversation.conversationID");

        Collection<JID> participants = search.getParticipants();
        boolean filterParticipants = !participants.isEmpty();
        boolean filterDate = search.getDateRangeMin() != null || search.getDateRangeMax() != null;
        boolean filterTimestamp = search.getIncludeTimestamp() != null;
        boolean filterRoom = search.getRoom() != null;

        // SELECT -- need to add value that we sort on. We always sort on date since that's
        // the only valid current option for non-keyword searches.
        query.append(", ofConversation.startDate");

        // FROM -- values (in addition to jiveThread)
        query.append(" FROM ofConversation");
        if (filterParticipants) {
            for (int i=0; i < participants.size(); i++) {
                query.append(", ofConParticipant participant").append(i);
            }
        }

        // WHERE BLOCK
        boolean whereSet = false;
        // See if we need to match against external conversations.
        if (search.isExternalWildcardMode() && search.getParticipants().size() != 2) {
            query.append(" WHERE isExternal=?");
            cachedPstmt.addInt(1);
            whereSet = true;
        }
        // Participants
        if (filterParticipants) {
            Iterator<JID> iter = participants.iterator();
            for (int i=0; i < participants.size(); i++) {
                if (!whereSet) {
                    query.append(" WHERE");
                    whereSet = true;
                }
                else {
                    query.append(" AND");
                }
                query.append(" ofConversation.conversationID=participant").append(i).append(".conversationID");
                query.append(" AND ");
                query.append("participant").append(i).append(".bareJID=?");
                String partJID = iter.next().toString();
                cachedPstmt.addString(partJID);
            }
        }

        // Creation date range
        if (filterDate) {
            if (search.getDateRangeMin() != null) {
                if (!whereSet) {
                    query.append(" WHERE");
                    whereSet = true;
                }
                else {
                    query.append(" AND");
                }
                query.append(" ofConversation.startDate >= ?");
                cachedPstmt.addLong(search.getDateRangeMin().getTime());
            }
            if (search.getDateRangeMax() != null) {
                if (!whereSet) {
                    query.append(" WHERE");
                    whereSet = true;
                }
                else {
                    query.append(" AND");
                }
                query.append(" ofConversation.startDate <= ?");
                cachedPstmt.addLong(search.getDateRangeMax().getTime());
            }
        }

        // Check if conversations have to happen at a given point in time
        if (filterTimestamp) {
            if (!whereSet) {
                query.append(" WHERE");
                whereSet = true;
            }
            else {
                query.append(" AND");
            }
            query.append(" ofConversation.startDate <= ?");
            cachedPstmt.addLong(search.getIncludeTimestamp().getTime());

            query.append(" AND");
            query.append(" ofConversation.lastActivity >= ?");
            cachedPstmt.addLong(search.getIncludeTimestamp().getTime());
        }

        // Filter by room
        if (filterRoom) {
            if (!whereSet) {
                query.append(" WHERE");
                whereSet = true;
            }
            else {
                query.append(" AND");
            }
            query.append(" ofConversation.room = ?");
            cachedPstmt.addString(search.getRoom().toString());
        }

        // ORDER BY
        query.append(" ORDER BY ofConversation.startDate");
        if (search.getSortOrder() == ArchiveSearch.SortOrder.descending) {
            query.append(" DESC");
        }
        else {
            query.append(" ASC");
        }

        int startIndex = search.getStartIndex();
        int numResults = search.getNumResults();
        if (numResults != ArchiveSearch.NULL_INT) {
            // MySQL optimization: use the LIMIT command to tell the database how many
            // rows we need returned. The syntax is LIMIT [offset],[rows]
            if (DbConnectionManager.getDatabaseType() == DbConnectionManager.DatabaseType.mysql) {
                query.append(" LIMIT ").append(startIndex).append(",").append(numResults);
            }
            // PostgreSQL optimization: use the LIMIT command to tell the database how many
            // rows we need returned. The syntax is LIMIT [rows] OFFSET [offset]
            else if (DbConnectionManager.getDatabaseType() == DbConnectionManager.DatabaseType.postgresql) {
                query.append(" LIMIT ").append(numResults).append(" OFFSET ").append(startIndex);
            }
        }

        // Set the database query string.
        cachedPstmt.setSQL(query.toString());

        List<Long> conversationIDs = new ArrayList<Long>();

        // Get all matching conversations from the database.
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = DbConnectionManager.createScrollablePreparedStatement(con, cachedPstmt.getSQL());
            cachedPstmt.setParams(pstmt);
            // Set the maximum number of rows to end at the end of this block.
            // A MySQL optimization using the LIMIT command is part of the SQL.
            // Therefore, we can skip this call on MySQL.
            if (DbConnectionManager.getDatabaseType() != DbConnectionManager.DatabaseType.mysql
                && DbConnectionManager.getDatabaseType() != DbConnectionManager.DatabaseType.postgresql)
            {
                DbConnectionManager.setMaxRows(pstmt, startIndex+numResults);
            }
            ResultSet rs = pstmt.executeQuery();
            // Position the cursor right before the first row that we're insterested in.
            // A MySQL and Postgres optimization using the LIMIT command is part of the SQL.
            // Therefore, we can skip this call on MySQL or Postgres.
            if (DbConnectionManager.getDatabaseType() != DbConnectionManager.DatabaseType.mysql
                && DbConnectionManager.getDatabaseType() != DbConnectionManager.DatabaseType.postgresql)
            {
                DbConnectionManager.scrollResultSet(rs, startIndex);
            }
            // Keep reading results until the result set is exhausted or
            // we come to the end of the block.
            int count = 0;
            while (rs.next() && count < numResults) {
                conversationIDs.add(rs.getLong(1));
                count++;
            }
            rs.close();
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
        return new DatabaseQueryResults(conversationIDs);
    }

    /**
     * Returns Hits from a database search against archived conversations as a Collection
     * of Conversation objects.
     */
    private class DatabaseQueryResults extends AbstractCollection<Conversation> {

        private List<Long> conversationIDs;

        /**
         * Constructs a new query results object.
         *
         * @param conversationIDs the list of conversation IDs.
         */
        public DatabaseQueryResults(List<Long> conversationIDs) {
            this.conversationIDs = conversationIDs;
        }

        @Override
        public Iterator<Conversation> iterator() {
            final Iterator<Long> convIterator = conversationIDs.iterator();
            return new Iterator<Conversation>() {

                private Conversation nextElement = null;

                public boolean hasNext() {
                    if (nextElement == null) {
                        nextElement = getNextElement();
                        if (nextElement == null) {
                            return false;
                        }
                    }
                    return true;
                }

                public Conversation next() {
                    Conversation element;
                    if (nextElement != null) {
                        element = nextElement;
                        nextElement = null;
                    }
                    else {
                        element = getNextElement();
                        if (element == null) {
                            throw new NoSuchElementException();
                        }
                    }
                    return element;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                private Conversation getNextElement() {
                    if (!convIterator.hasNext()) {
                        return null;
                    }
                    while (convIterator.hasNext()) {
                        try {
                            long conversationID = convIterator.next();
                            return new Conversation(conversationID);
                        }
                        catch (Exception e) {
                            Log.error(e.getMessage(), e);
                        }
                    }
                    return null;
                }
            };
        }

        @Override
        public int size() {
            return conversationIDs.size();
        }
    }

    /**
     * Returns Hits from a Lucene search against archived conversations as a Collection
     * of Conversation objects.
     */
    private class LuceneQueryResults extends AbstractCollection<Conversation> {

        private IndexSearcher searcher;
        private TopDocs hits;
        private int index;
        private int endIndex;

        /**
         * Constructs a new query results object.
         *
         * @param hits the search hits.
         * @param startIndex the starting index that results should be returned from.
         * @param endIndex the ending index that results should be returned to.
         */
        public LuceneQueryResults(IndexSearcher searcher, TopDocs hits, int startIndex, int endIndex) {
            this.searcher = searcher;
            this.hits = hits;
            this.index = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public Iterator<Conversation> iterator() {
            // Only use the range as specified.
            final ScoreDoc[] scoreDocs = Arrays.copyOfRange( hits.scoreDocs, index, Math.min( endIndex + 1, hits.scoreDocs.length) );
            final Iterator<ScoreDoc> hitsIterator = Arrays.asList(scoreDocs).iterator();

            return new Iterator<Conversation>() {

                private Conversation nextElement = null;

                public boolean hasNext() {
                    if (nextElement == null) {
                        nextElement = getNextElement();
                        if (nextElement == null) {
                            return false;
                        }
                    }
                    return true;
                }

                public Conversation next() {
                    Conversation element;
                    if (nextElement != null) {
                        element = nextElement;
                        nextElement = null;
                    }
                    else {
                        element = getNextElement();
                        if (element == null) {
                            throw new NoSuchElementException();
                        }
                    }
                    return element;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                private Conversation getNextElement() {
                    if (!hitsIterator.hasNext()) {
                        return null;
                    }
                    while (hitsIterator.hasNext()) {
                        try {
                            ScoreDoc hit = hitsIterator.next();
                            long conversationID = Long.parseLong(searcher.doc(hit.doc).get("conversationID"));
                            return new Conversation(conversationID);
                        }
                        catch (Exception e) {
                            Log.error(e.getMessage(), e);
                        }
                    }
                    return null;
                }
            };
        }

        @Override
        public int size() {
            // TODO the original implementation returned the size of all hits, not the size as delimitered by index and endIndex. Shouldn't that be returned instead?
            return (int) hits.totalHits.value;
        }
    }
}
