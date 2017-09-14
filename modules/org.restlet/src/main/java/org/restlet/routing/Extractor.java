/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.routing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Filter extracting attributes from a call. Multiple extractions can be
 * defined, based on the query string of the resource reference, on the request
 * form (ex: posted from a browser) or on cookies.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class Extractor extends Filter {

    /** Internal class holding extraction information. */
    private static final class ExtractInfo {
        /** Target attribute name. */
        protected String attribute;

        /** Indicates how to handle repeating values. */
        protected boolean first;

        /** Name of the parameter to look for. */
        protected String parameter;

        /**
         * Constructor.
         * 
         * @param attribute
         *            Target attribute name.
         * @param parameter
         *            Name of the parameter to look for.
         * @param first
         *            Indicates how to handle repeating values.
         */
        public ExtractInfo(String attribute, String parameter, boolean first) {
            this.attribute = attribute;
            this.parameter = parameter;
            this.first = first;
        }
    }

    /** The list of cookies to extract. */
    private volatile List<ExtractInfo> cookieExtracts;

    /** The list of request entity parameters to extract. */
    private volatile List<ExtractInfo> entityExtracts;

    /** The list of query parameters to extract. */
    private volatile List<ExtractInfo> queryExtracts;

    /**
     * Constructor.
     */
    public Extractor() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public Extractor(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet.
     */
    public Extractor(Context context, Restlet next) {
        super(context, next);
    }

    /**
     * Allows filtering before its handling by the target Restlet. By default it
     * extracts the attributes from form parameters (query, cookies, entity) and
     * finally puts them in the request's attributes (
     * {@link Request#getAttributes()}).
     * 
     * @param request
     *            The request to filter.
     * @param response
     *            The response to filter.
     * @return The continuation status.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        // Extract the query parameters
        if (!getQueryExtracts().isEmpty()) {
            Form form = request.getResourceRef().getQueryAsForm();

            if (form != null) {
                for (ExtractInfo ei : getQueryExtracts()) {
                    if (ei.first) {
                        String value = form.getFirstValue(ei.parameter);

                        if (value != null) {
                            request.getAttributes().put(ei.attribute, value);
                        }
                    } else {
                        request.getAttributes().put(ei.attribute,
                                form.subList(ei.parameter));
                    }
                }
            }
        }

        // Extract the request entity parameters
        if (!getEntityExtracts().isEmpty()) {
            Representation entity = request.getEntity();
            if (entity != null) {
                Form form = new Form(entity);

                for (ExtractInfo ei : getEntityExtracts()) {
                    if (ei.first) {
                        String value = form.getFirstValue(ei.parameter);

                        if (value != null) {
                            request.getAttributes().put(ei.attribute, value);
                        }
                    } else {
                        request.getAttributes().put(ei.attribute,
                                form.subList(ei.parameter));
                    }
                }
            }
        }

        // Extract the cookie parameters
        if (!getCookieExtracts().isEmpty()) {
            Series<Cookie> cookies = request.getCookies();

            if (cookies != null) {
                for (ExtractInfo ei : getCookieExtracts()) {
                    if (ei.first) {
                        String value = cookies.getFirstValue(ei.parameter);

                        if (value != null) {
                            request.getAttributes().put(ei.attribute, value);
                        }
                    } else {
                        request.getAttributes().put(ei.attribute,
                                cookies.subList(ei.parameter));
                    }
                }
            }
        }

        return CONTINUE;
    }

    /**
     * Extracts an attribute from the request cookies.
     * 
     * @param attribute
     *            The name of the request attribute to set.
     * @param cookieName
     *            The name of the cookies to extract.
     * @param first
     *            Indicates if only the first cookie should be set. Otherwise as
     *            a List instance might be set in the attribute value.
     */
    public void extractFromCookie(String attribute, String cookieName,
            boolean first) {
        getCookieExtracts().add(new ExtractInfo(attribute, cookieName, first));
    }

    /**
     * Extracts an attribute from the request entity form.
     * 
     * @param attribute
     *            The name of the request attribute to set.
     * @param parameter
     *            The name of the entity form parameter to extract.
     * @param first
     *            Indicates if only the first cookie should be set. Otherwise as
     *            a List instance might be set in the attribute value.
     */
    public void extractFromEntity(String attribute, String parameter,
            boolean first) {
        getEntityExtracts().add(new ExtractInfo(attribute, parameter, first));
    }

    /**
     * Extracts an attribute from the query string of the resource reference.
     * 
     * @param attribute
     *            The name of the request attribute to set.
     * @param parameter
     *            The name of the query string parameter to extract.
     * @param first
     *            Indicates if only the first cookie should be set. Otherwise as
     *            a List instance might be set in the attribute value.
     */
    public void extractFromQuery(String attribute, String parameter,
            boolean first) {
        getQueryExtracts().add(new ExtractInfo(attribute, parameter, first));
    }

    /**
     * Returns the list of query extracts.
     * 
     * @return The list of query extracts.
     */
    private List<ExtractInfo> getCookieExtracts() {
        // Lazy initialization with double-check.
        List<ExtractInfo> ce = this.cookieExtracts;
        if (ce == null) {
            synchronized (this) {
                ce = this.cookieExtracts;
                if (ce == null) {
                    this.cookieExtracts = ce = new CopyOnWriteArrayList<ExtractInfo>();
                }
            }
        }
        return ce;
    }

    /**
     * Returns the list of query extracts.
     * 
     * @return The list of query extracts.
     */
    private List<ExtractInfo> getEntityExtracts() {
        // Lazy initialization with double-check.
        List<ExtractInfo> ee = this.entityExtracts;
        if (ee == null) {
            synchronized (this) {
                ee = this.entityExtracts;
                if (ee == null) {
                    this.entityExtracts = ee = new CopyOnWriteArrayList<ExtractInfo>();
                }
            }
        }
        return ee;
    }

    /**
     * Returns the list of query extracts.
     * 
     * @return The list of query extracts.
     */
    private List<ExtractInfo> getQueryExtracts() {
        // Lazy initialization with double-check.
        List<ExtractInfo> qe = this.queryExtracts;
        if (qe == null) {
            synchronized (this) {
                qe = this.queryExtracts;
                if (qe == null) {
                    this.queryExtracts = qe = new CopyOnWriteArrayList<ExtractInfo>();
                }
            }
        }
        return qe;
    }

}
