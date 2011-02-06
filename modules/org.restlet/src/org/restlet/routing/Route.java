/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.routing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Filter scoring the affinity of calls with the attached Restlet. The score is
 * used by an associated Router in order to determine the most appropriate
 * Restlet for a given call. The routing is based on a reference template. It
 * also supports the extraction of some attributes from a call.<br>
 * <br>
 * Multiple extractions can be defined, based on the query string of the
 * resource reference, on the request form (ex: posted from a browser) or on
 * cookies.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see org.restlet.routing.Template
 * @author Jerome Louvel
 * @deprecated Use {@link TemplateRoute} instead.
 */
@Deprecated
public class Route extends TemplateRoute {
    /** Internal class holding extraction information. */
    private static final class ExtractInfo {
        /** Target attribute name. */
        protected volatile String attribute;

        /** Indicates how to handle repeating values. */
        protected volatile boolean first;

        /** Name of the parameter to look for. */
        protected volatile String parameter;

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

    /** Internal class holding validation information. */
    private static final class ValidateInfo {
        /** Name of the attribute to look for. */
        protected volatile String attribute;

        /** Format of the attribute value, using Regex pattern syntax. */
        protected volatile String format;

        /** Indicates if the attribute presence is required. */
        protected volatile boolean required;

        /**
         * Constructor.
         * 
         * @param attribute
         *            Name of the attribute to look for.
         * @param required
         *            Indicates if the attribute presence is required.
         * @param format
         *            Format of the attribute value, using Regex pattern syntax.
         */
        public ValidateInfo(String attribute, boolean required, String format) {
            this.attribute = attribute;
            this.required = required;
            this.format = format;
        }
    }

    /** The list of cookies to extract. */
    private volatile List<ExtractInfo> cookieExtracts;

    /** The list of request entity parameters to extract. */
    private volatile List<ExtractInfo> entityExtracts;

    /** The list of query parameters to extract. */
    private volatile List<ExtractInfo> queryExtracts;

    /** The list of attribute validations. */
    private volatile List<ValidateInfo> validations;

    /**
     * Constructor behaving as a simple extractor filter.
     * 
     * @param next
     *            The next Restlet.
     */
    public Route(Restlet next) {
        this(null, (Template) null, next);
    }

    /**
     * Constructor. The URIs will be matched agains the template using the
     * {@link Template#MODE_STARTS_WITH} matching mode. This can be changed by
     * getting the template and calling {@link Template#setMatchingMode(int)}
     * with {@link Template#MODE_EQUALS} for exact matching.
     * 
     * @param router
     *            The parent router.
     * @param uriTemplate
     *            The URI template.
     * @param next
     *            The next Restlet.
     */
    public Route(Router router, String uriTemplate, Restlet next) {
        this(router, new Template(uriTemplate, Template.MODE_STARTS_WITH,
                Variable.TYPE_URI_SEGMENT, "", true, false), next);
    }

    /**
     * Constructor.
     * 
     * @param router
     *            The parent router.
     * @param template
     *            The URI template.
     * @param next
     *            The next Restlet.
     */
    public Route(Router router, Template template, Restlet next) {
        super(router, template, next);
    }

    /**
     * Allows filtering before its handling by the target Restlet. By default it
     * parses the template variable, adjust the base reference, then extracts
     * the attributes from form parameters (query, cookies, entity) and finally
     * tries to validates the variables as indicated by the
     * {@link #validate(String, boolean, String)} method.
     * 
     * @param request
     *            The request to filter.
     * @param response
     *            The response to filter.
     * @return The continuation status.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        // 1 - Parse the template variables and adjust the base reference
        if (getTemplate() != null) {
            final String remainingPart = request.getResourceRef()
                    .getRemainingPart(false, isMatchingQuery());
            final int matchedLength = getTemplate().parse(remainingPart,
                    request);

            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().finer(
                        "Attempting to match this pattern: "
                                + getTemplate().getPattern() + " >> "
                                + matchedLength);
            }

            if (matchedLength != -1) {
                // Updates the context
                final String matchedPart = remainingPart.substring(0,
                        matchedLength);
                Reference baseRef = request.getResourceRef().getBaseRef();

                if (baseRef == null) {
                    if (matchedLength > 0) {
                        baseRef = new Reference(matchedPart);
                    }
                } else {
                    baseRef = new Reference(baseRef.toString(false, false)
                            + matchedPart);
                }

                request.getResourceRef().setBaseRef(baseRef);

                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().fine(
                            "New base URI: "
                                    + request.getResourceRef().getBaseRef());
                    getLogger().fine(
                            "New remaining part: "
                                    + request.getResourceRef()
                                            .getRemainingPart(false,
                                                    isMatchingQuery()));
                }

                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().fine(
                            "Delegating the call to the target Restlet");
                }
            } else {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }

        // 2 - Extract the attributes from form parameters (query, cookies,
        // entity).
        extractAttributes(request, response);

        // 3 - Validate the attributes extracted (or others)
        validateAttributes(request, response);

        return CONTINUE;
    }

    /**
     * Extracts the attributes value from the request.
     * 
     * @param request
     *            The request to process.
     * @param response
     *            The response to process.
     */
    private void extractAttributes(Request request, Response response) {
        // Extract the query parameters
        if (!getQueryExtracts().isEmpty()) {
            final Form form = request.getResourceRef().getQueryAsForm();

            if (form != null) {
                for (final ExtractInfo ei : getQueryExtracts()) {
                    if (ei.first) {
                        request.getAttributes().put(ei.attribute,
                                form.getFirstValue(ei.parameter));
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
                final Form form = new Form(entity);
                for (final ExtractInfo ei : getEntityExtracts()) {
                    if (ei.first) {
                        request.getAttributes().put(ei.attribute,
                                form.getFirstValue(ei.parameter));
                    } else {
                        request.getAttributes().put(ei.attribute,
                                form.subList(ei.parameter));
                    }
                }
            }
        }

        // Extract the cookie parameters
        if (!getCookieExtracts().isEmpty()) {
            final Series<Cookie> cookies = request.getCookies();

            if (cookies != null) {
                for (final ExtractInfo ei : getCookieExtracts()) {
                    if (ei.first) {
                        request.getAttributes().put(ei.attribute,
                                cookies.getFirstValue(ei.parameter));
                    } else {
                        request.getAttributes().put(ei.attribute,
                                cookies.subList(ei.parameter));
                    }
                }
            }
        }
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
     * @return The current Filter.
     */
    public Route extractCookie(String attribute, String cookieName,
            boolean first) {
        getCookieExtracts().add(new ExtractInfo(attribute, cookieName, first));
        return this;
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
     * @return The current Filter.
     */
    public Route extractEntity(String attribute, String parameter, boolean first) {
        getEntityExtracts().add(new ExtractInfo(attribute, parameter, first));
        return this;
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
     * @return The current Filter.
     */
    public Route extractQuery(String attribute, String parameter, boolean first) {
        getQueryExtracts().add(new ExtractInfo(attribute, parameter, first));
        return this;
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

    /**
     * Returns the list of attribute validations.
     * 
     * @return The list of attribute validations.
     */
    private List<ValidateInfo> getValidations() {
        // Lazy initialization with double-check.
        List<ValidateInfo> v = this.validations;
        if (v == null) {
            synchronized (this) {
                v = this.validations;
                if (v == null) {
                    this.validations = v = new CopyOnWriteArrayList<ValidateInfo>();
                }
            }
        }
        return v;
    }

    /**
     * Checks the request attributes for presence, format, etc. If the check
     * fails, then a response status CLIENT_ERROR_BAD_REQUEST is returned with
     * the proper status description.
     * 
     * @param attribute
     *            Name of the attribute to look for.
     * @param required
     *            Indicates if the attribute presence is required.
     * @param format
     *            Format of the attribute value, using Regex pattern syntax.
     */
    public void validate(String attribute, boolean required, String format) {
        getValidations().add(new ValidateInfo(attribute, required, format));
    }

    /**
     * Validates the attributes from the request.
     * 
     * @param request
     *            The request to process.
     * @param response
     *            The response to process.
     */
    private void validateAttributes(Request request, Response response) {
        if (this.validations != null) {
            for (final ValidateInfo validate : getValidations()) {
                if (validate.required
                        && !request.getAttributes().containsKey(
                                validate.attribute)) {
                    response
                            .setStatus(
                                    Status.CLIENT_ERROR_BAD_REQUEST,
                                    "Unable to find the \""
                                            + validate.attribute
                                            + "\" attribute in the request. Please check your request.");
                } else if (validate.format != null) {
                    final Object value = request.getAttributes().get(
                            validate.attribute);
                    if (value == null) {
                        response
                                .setStatus(
                                        Status.CLIENT_ERROR_BAD_REQUEST,
                                        "Unable to validate the \""
                                                + validate.attribute
                                                + "\" attribute with a null value. Please check your request.");
                    } else {
                        if (!Pattern.matches(validate.format, value.toString())) {
                            response
                                    .setStatus(
                                            Status.CLIENT_ERROR_BAD_REQUEST,
                                            "Unable to validate the value of the \""
                                                    + validate.attribute
                                                    + "\" attribute. The expected format is: "
                                                    + validate.format
                                                    + " (Java Regex). Please check your request.");
                        }
                    }
                }
            }
        }
    }
}
