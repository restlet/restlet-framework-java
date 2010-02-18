/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.odata;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Relation;
import org.restlet.ext.odata.internal.EntryContentHandler;
import org.restlet.ext.odata.internal.FeedContentHandler;
import org.restlet.ext.odata.internal.FeedParser;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Template;
import org.restlet.routing.Variable;
import org.restlet.util.Series;

/**
 * Specific query to a Data Service, represents a particular HTTP request to a
 * data service. This Java class is more or less equivalent to the WCF
 * DataServiceQuery class.
 * 
 * @author Jerome Louvel
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/system.data.services.client.dataservicequery.aspx"></a>
 * @param <T>
 */
public class Query<T> implements Iterable<T> {

    /**
     * Iterator that transparently supports sever-side paging.
     * 
     * @author Thierry Boileau
     * 
     * @param <T>
     */
    private class EntryIterator<E> implements Iterator<E> {

        /** The class of the listed objects. */
        private Class<?> entityClass;

        /** The inner iterator. */
        private Iterator<E> iterator;

        /** The reference to the next page. */
        private Reference nextPage;

        /** The underlying service. */
        private Service service;

        /**
         * Constructor.
         * 
         * @param service
         * @param iterator
         * @param nextPageRef
         * @param entityClass
         */
        public EntryIterator(Service service, Iterator<E> iterator,
                Reference nextPageRef, Class<?> entityClass) {
            super();
            this.iterator = iterator;
            this.nextPage = nextPageRef;
            this.service = service;
            this.entityClass = entityClass;
        }

        @SuppressWarnings("unchecked")
        public boolean hasNext() {
            boolean result = false;

            if (iterator != null) {
                result = iterator.hasNext();
            }

            if (!result && nextPage != null) {
                // Get the next page.
                Query<E> query = service.createQuery(nextPage.toString(),
                        (Class<E>) entityClass);
                iterator = query.iterator();
                if (iterator != null) {
                    result = iterator.hasNext();
                }
                // Get the next page
                nextPage = query.getNextPage();
            }

            return result;
        }

        public E next() {
            E result = null;
            if (iterator != null) {
                if (iterator.hasNext()) {
                    result = iterator.next();
                }
            }
            return result;
        }

        public void remove() {
            if (iterator != null) {
                iterator.remove();
            }
        }
    }

    // Defines the type of the current query. It has an impact on how to parse
    // the result.
    /** Type of query: complex type or property. */
    public static final int TYPE_COMPLEX_TYPE_OR_PROPERTY = 3;

    /** Type of query: property. */
    public static final int TYPE_COMPLEX_TYPE_PROPERTY = 4;

    /** Type of query: property bis?? */
    public static final int TYPE_COMPLEX_TYPE_PROPERTY5 = 5;

    /** Type of query: entity. */
    public static final int TYPE_ENTITY = 2;

    /** Type of query: entity set. */
    public static final int TYPE_ENTITY_SET = 1;

    /** Type of query: links. */
    public static final int TYPE_LINKS = 7;

    /** Type of query: property value. */
    public static final int TYPE_PROPERTY_VALUE = 6;

    /** Type of query: unknown. */
    public static final int TYPE_UNKNOWN = 0;

    /** The number of entities. */
    protected int count;

    /** Class of the entity targeted by this query. */
    private Class<?> entityClass;

    /** Has the query been executed? */
    private boolean executed;

    /** The atom feed object that wraps the data. */
    private Feed feed;

    /** Is the inline asked for? */
    private boolean inlineCount;

    /** Internal logger. */
    private Logger logger;

    /** The reference to the next page (used in server-paging mode). */
    private Reference nextPage;

    /** The query string. */
    private String query;

    /** The parent client service. */
    private Service service;

    /** The path of the targeted entity relatively to the data service URI. */
    private String subpath;

    /**
     * Constructor.
     * 
     * @param service
     *            The data service requested by the query.
     * @param subpath
     *            The path of the targeted entity relatively to the data service
     *            URI.
     * @param entityClass
     *            The class of the target entity.
     */
    public Query(Service service, String subpath, Class<T> entityClass) {
        this.service = service;
        Reference ref = new Reference(subpath);
        if (ref.isAbsolute()) {
            this.subpath = ref.getRelativeRef(service.getServiceRef())
                    .toString(true, true);
        } else {
            this.subpath = subpath;
        }

        this.executed = false;
        this.entityClass = entityClass;
        this.count = -1;
    }

    /**
     * Creates a new Query<T> with the query parameter set in the URI generated
     * by the returned query.
     * 
     * @param name
     *            The string value that contains the name of the query string
     *            option to add.
     * @param value
     *            The value of the query string option.
     * @return A new Query<T> with the query parameter set in the URI generated
     *         by the returned query.
     */
    @SuppressWarnings("unchecked")
    public Query<T> addParameter(String name, String value) {
        Query<T> result = new Query<T>(this.getService(), this.getSubpath(),
                (Class<T>) this.entityClass);
        if (getQuery() == null || "".equals(getQuery())) {
            result.setQuery(name + "=" + value);
        } else {
            result.setQuery(getQuery() + "&" + name + "=" + value);
        }

        return result;
    }

    /**
     * Creates a new Query<T> with the query parameter set in the URI generated
     * by the returned query.
     * 
     * @param params
     *            the set of name/value pairs to add to the query string
     * @return A new Query<T> with the query parameter set in the URI generated
     *         by the returned query.
     */
    @SuppressWarnings("unchecked")
    public Query<T> addParameters(Series<Parameter> params) {
        Query<T> result = new Query<T>(this.getService(), this.getSubpath(),
                (Class<T>) this.entityClass);
        StringBuilder builder = new StringBuilder();
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                Parameter param = params.get(i);
                if (i == 0) {
                    builder.append(param.getName());
                    builder.append("=");
                    builder.append(param.getValue());
                }
            }
        }
        if (getQuery() == null || "".equals(getQuery())) {
            result.setQuery(builder.toString());
        } else {
            result.setQuery(getQuery() + "&" + builder.toString());
        }

        return result;
    }

    /**
     * Returns the complete target URI reference for this query. It is composed
     * of the data service base URI, the subpath and the query string.
     * 
     * @return The complete target URI reference.
     */
    protected String createTargetUri() {
        String service = getService().getServiceRef().toString();
        StringBuilder result = new StringBuilder(service);
        String subpath = (getSubpath() == null) ? "" : getSubpath();
        if (service.endsWith("/")) {
            if (subpath.startsWith("/")) {
                result.append(subpath.substring(1));
            } else {
                result.append(subpath);
            }
        } else {
            if (subpath.startsWith("/")) {
                result.append(subpath);
            } else {
                result.append("/").append(subpath);
            }
        }
        if (getQuery() != null) {
            result.append("?").append(getQuery());
        }

        return result.toString();
    }

    /**
     * Executes the query.
     * 
     * @throws Exception
     */
    public void execute() throws Exception {
        if (!isExecuted()) {
            String targetUri = createTargetUri();

            ClientResource resource = new ClientResource(targetUri);
            resource.setChallengeResponse(service.getCredentials());

            Representation result = new StringRepresentation(resource.get(
                    MediaType.APPLICATION_ATOM).getText());
            getLogger().fine(result.getText());
            service.setLatestRequest(resource.getRequest());
            service.setLatestResponse(resource.getResponse());

            if (resource.getStatus().isSuccess()) {
                // Guess the type of query based on the URI structure
                switch (guessType(targetUri)) {
                case TYPE_ENTITY_SET:
                    FeedContentHandler feedContentHandler = new FeedContentHandler();
                    EntryContentHandler<T> entryContentHandler = new EntryContentHandler<T>(
                            (Metadata) service.getMetadata(), this.entityClass);
                    setFeed(new Feed(result, feedContentHandler,
                            entryContentHandler));
                    this.count = feedContentHandler.getCount();
                    break;
                case TYPE_ENTITY:
                    Feed feed = new Feed();
                    feed.getEntries().add(new Entry(result));
                    setFeed(feed);
                    break;
                case TYPE_UNKNOWN:
                    // Guess the type of query based on the returned
                    // representation
                    Representation rep = new StringRepresentation(result
                            .getText());
                    String string = rep.getText().substring(0,
                            Math.min(100, rep.getText().length()));
                    if (string.contains("<feed")) {
                        setFeed(new Feed(rep));
                    } else if (string.contains("<entry")) {
                        Feed f = new Feed();
                        f.getEntries().add(new Entry(result));
                        setFeed(f);
                    }
                default:
                    // Can only guess entity and entity set, a priori.
                    // TODO May we go a step further by analyzing the metadata
                    // of the data services?
                    // Do we support only those two types?
                    // Another way is to guess from the result representation.
                    // Sometimes, it returns a set, an entity, or a an XML
                    // representation of a property.
                    break;
                }
            }

            setExecuted(true);
        }
    }

    /**
     * Creates a new Query<T> with the $expand option set in the URI generated
     * by the returned query.
     * 
     * @param path
     *            A string value that contains the requesting URI.
     * @return A new Query<T> with the $expand option set in the URI generated
     *         by the returned query.
     */
    public Query<T> expand(String path) {
        return addParameter("$expand", path);
    }

    /**
     * Creates a new Query<T> with the $filter option set in the URI generated
     * by the returned query.
     * 
     * @param predicate
     *            A string value that contains the predicate used to filter the
     *            data.
     * @return A new Query<T> with the $filter option set in the URI generated
     *         by the returned query.
     */
    public Query<T> filter(String predicate) {
        return addParameter("$filter", predicate);
    }

    /**
     * Returns the total number of elements in the entity set.
     * 
     * @return The total number of elements in the entity set.
     * @throws Exception
     */
    public int getCount() throws Exception {
        if (!isExecuted()) {
            if (inlineCount) {
                execute();
            } else {
                String targetUri = createTargetUri();

                if (guessType(targetUri) == TYPE_ENTITY) {
                    targetUri = targetUri.substring(0, targetUri
                            .lastIndexOf("("));
                }
                targetUri += "/$count";

                ClientResource resource = new ClientResource(targetUri);
                resource.setChallengeResponse(service.getCredentials());
                Representation result = resource.get();
                if (resource.getStatus().isSuccess()) {
                    count = Integer.parseInt(result.getText());
                }
            }
        }

        return count;
    }

    /**
     * Returns the atom feed object that wrap the data.
     * 
     * @return The atom feed object that wrap the data.
     */
    private Feed getFeed() {
        return feed;
    }

    /**
     * Returns the current logger.
     * 
     * @return The current logger.
     */
    private Logger getLogger() {
        if (logger == null) {
            logger = Context.getCurrentLogger();
        }
        return logger;
    }

    /**
     * Return the reference to the next page (used in server-paging mode).
     * 
     * @return The reference to the next page (used in server-paging mode).
     */
    private Reference getNextPage() {
        return nextPage;
    }

    /**
     * Returns the query string that may be completed by calls to
     * {@link Query#addParameter(String, String)} or
     * {@link Query#expand(String)}.
     * 
     * @return The query string.
     */
    private String getQuery() {
        return query;
    }

    /**
     * Returns the parent client service.
     * 
     * @return The parent client service.
     */
    public Service getService() {
        return service;
    }

    /**
     * Returns the path of the targeted entity relatively to the data service
     * URI.
     * 
     * @return The path of the targeted entity relatively to the data service
     *         URI.
     */
    public String getSubpath() {
        return subpath;
    }

    /**
     * Tries to deduce the type of the query based on the analysis of the target
     * URI, and return it.
     * 
     * @param targetUri
     *            The target URI to analyse.
     * @return The deduced type of query or {@link Query#TYPE_UNKNOWN} if it is
     *         unknown.
     */
    private int guessType(String targetUri) {
        // Remove the trailing query part
        String uri = targetUri;
        int index = targetUri.indexOf("?");
        if (index != -1) {
            uri = uri.substring(0, index);
        }

        // Let's detect the type of query
        int type = TYPE_UNKNOWN;

        // Can only match entitySet and entity from the the target URI
        String entitySet = "{service}.svc/{entitySet}";
        String entity = entitySet + "({keyPredicate})";

        Template t = new Template(entity, Template.MODE_EQUALS);
        t.getVariables().put("entitySet",
                new Variable(Variable.TYPE_ALL, "", true, false));
        t.getVariables().put("keyPredicate",
                new Variable(Variable.TYPE_ALL, "", true, false));

        if (t.match(uri) != -1) {
            return TYPE_ENTITY;
        }

        t.setPattern(entitySet);
        if (t.match(uri) != -1) {
            return TYPE_ENTITY_SET;
        }

        return type;
    }

    /**
     * Creates a new Query<T> with the $inlinecount option set in the URI
     * generated by the returned query.
     * 
     * @param inlineCount
     *            True if the total number of entities in the entity set must be
     *            returned.
     * @return A new Query<T> with the $inlinecount option set in the URI
     *         generated by the returned query.
     */
    public Query<T> inlineCount(boolean inlineCount) {
        Query<T> result = null;
        if (inlineCount) {
            result = addParameter("$inlinecount", "allpages");
        } else {
            result = addParameter("$inlinecount", "none");
        }
        result.inlineCount = inlineCount;

        return result;
    }

    /**
     * Returns true if the query has been executed.
     * 
     * @return true if the query has been executed.
     */
    private boolean isExecuted() {
        return executed;
    }

    /**
     * Returns an iterator over a set of elements of type T. It returns null if
     * the query does not retrieve elements.
     * 
     * @return an Iterator or null if the query does not retrieve elements.
     */
    public Iterator<T> iterator() {
        Iterator<T> result = null;

        try {
            execute();
            result = new FeedParser<T>(getFeed(), this.entityClass,
                    ((Metadata) getService().getMetadata())).parse();
            // Detect server-paging mode.
            nextPage = null;
            for (Link link : getFeed().getLinks()) {
                if (Relation.NEXT.equals(link.getRel())) {
                    nextPage = link.getHref();
                    break;
                }
            }
            if (nextPage != null) {
                result = new EntryIterator<T>(this.service, result, nextPage,
                        entityClass);
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Can't parse the content of " + createTargetUri(), e);
        }

        return result;
    }

    /**
     * Creates a new Query<T> with the $orderby option set in the URI generated
     * by the returned query.
     * 
     * @param criteria
     *            A string value that contains the criteria used to order the
     *            results.
     * @return A new Query<T> with the $orderby option set in the URI generated
     *         by the returned query.
     */
    public Query<T> orderby(String criteria) {
        return addParameter("$orderby", criteria);
    }

    /**
     * Creates a new Query<T> with the $select option set in the URI generated
     * by the returned query.
     * 
     * @param select
     *            A string value that contains the requesting URI.
     * @return A new Query<T> with the $select option set in the URI generated
     *         by the returned query.
     */
    public Query<T> select(String select) {
        return addParameter("$select", select);
    }

    /**
     * Indicates whether the query has been executed.
     * 
     * @param executed
     *            true if the query has been executed.
     */
    private void setExecuted(boolean executed) {
        this.executed = executed;
    }

    /**
     * Sets the atom feed object that wraps the data.
     * 
     * @param feed
     *            The atom feed object that wraps the data.
     */
    private void setFeed(Feed feed) {
        this.feed = feed;
    }

    /**
     * Sets the query string of the request.
     * 
     * @param query
     *            The query string of the request.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Creates a new Query<T> with the $skip option set in the URI generated by
     * the returned query.
     * 
     * @param rowsCount
     *            A number of rows to skip.
     * @return A new Query<T> with the $skip option set in the URI generated by
     *         the returned query.
     */
    public Query<T> skip(int rowsCount) {
        return addParameter("$skip", Integer.toString(rowsCount));
    }

    /**
     * Creates a new Query<T> with the $skiptoken option set in the URI
     * generated by the returned query.
     * 
     * @param token
     *            A string value that contains the requesting URI.
     * @return A new Query<T> with the $skiptoken option set in the URI
     *         generated by the returned query.
     */
    public Query<T> skiptoken(String token) {
        return addParameter("$skiptoken", token);
    }

    /**
     * Creates a new Query<T> with the $top option set in the URI generated by
     * the returned query.
     * 
     * @param rowsCount
     *            A number of rows used to limit the number of results.
     * @return A new Query<T> with the $top option set in the URI generated by
     *         the returned query.
     */
    public Query<T> top(int rowsCount) {
        return addParameter("$top", Integer.toString(rowsCount));
    }
}
