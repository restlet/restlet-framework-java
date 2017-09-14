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

import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;

/**
 * Filter scoring the affinity of calls with the attached Restlet. The score is
 * used by an associated Router in order to determine the most appropriate
 * Restlet for a given call. The routing is based on a reference template.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see org.restlet.routing.Template
 * @author Jerome Louvel
 */
public class TemplateRoute extends Route {
    /**
     * Indicates whether the query part should be taken into account when
     * matching a reference with the template.
     */
    private volatile boolean matchingQuery;

    /** The reference template to match. */
    private volatile Template template;

    /**
     * Constructor behaving as a simple extractor filter.
     * 
     * @param next
     *            The next Restlet.
     */
    public TemplateRoute(Restlet next) {
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
    public TemplateRoute(Router router, String uriTemplate, Restlet next) {
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
    public TemplateRoute(Router router, Template template, Restlet next) {
        super(router, next);
        this.matchingQuery = (router == null) ? true : router
                .getDefaultMatchingQuery();
        this.template = template;
    }

    /**
     * Allows filtering before its handling by the target Restlet. By default it
     * parses the template variable, adjust the base reference of the target
     * resource's reference.
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
            String remainingPart = request.getResourceRef().getRemainingPart(
                    false, isMatchingQuery());
            int matchedLength = getTemplate().parse(remainingPart, request);

            if (matchedLength == 0) {
                if (request.isLoggable() && getLogger().isLoggable(Level.FINER)) {
                    getLogger().finer("No characters were matched");
                }
            } else if (matchedLength > 0) {
                if (request.isLoggable() && getLogger().isLoggable(Level.FINER)) {
                    getLogger().finer(
                            "" + matchedLength + " characters were matched");
                }

                // Updates the context
                String matchedPart = remainingPart.substring(0, matchedLength);
                Reference baseRef = request.getResourceRef().getBaseRef();

                if (baseRef == null) {
                    baseRef = new Reference(matchedPart);
                } else {
                    baseRef = new Reference(baseRef.toString(false, false)
                            + matchedPart);
                }

                request.getResourceRef().setBaseRef(baseRef);

                if (request.isLoggable()) {
                    if (getLogger().isLoggable(Level.FINE)) {
                        remainingPart = request.getResourceRef()
                                .getRemainingPart(false, isMatchingQuery());

                        if ((remainingPart != null)
                                && (!"".equals(remainingPart))) {
                            getLogger().fine(
                                    "New base URI: \""
                                            + request.getResourceRef()
                                                    .getBaseRef()
                                            + "\". New remaining part: \""
                                            + remainingPart + "\"");
                        } else {
                            getLogger().fine(
                                    "New base URI: \""
                                            + request.getResourceRef()
                                                    .getBaseRef()
                                            + "\". No remaining part to match");
                        }
                    }

                    if (getLogger().isLoggable(Level.FINER)) {
                        getLogger().finer(
                                "Delegating the call to the target Restlet");
                    }
                }
            } else {
                if (request.isLoggable() && getLogger().isLoggable(Level.FINE)) {
                    getLogger().fine(
                            "Unable to match this pattern: "
                                    + getTemplate().getPattern());
                }

                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }

        return CONTINUE;
    }

    /**
     * Returns the matching mode to use on the template when parsing a formatted
     * reference.
     * 
     * @return The matching mode to use.
     */
    public int getMatchingMode() {
        return getTemplate().getMatchingMode();
    }

    /**
     * Returns the reference template to match.
     * 
     * @return The reference template to match.
     */
    public Template getTemplate() {
        return this.template;
    }

    /**
     * Indicates whether the query part should be taken into account when
     * matching a reference with the template.
     * 
     * @return True if the query part of the reference should be taken into
     *         account, false otherwise.
     */
    public boolean isMatchingQuery() {
        return this.matchingQuery;
    }

    /**
     * Returns the score for a given call (between 0 and 1.0).
     * 
     * @param request
     *            The request to score.
     * @param response
     *            The response to score.
     * @return The score for a given call (between 0 and 1.0).
     */
    public float score(Request request, Response response) {
        float result = 0F;

        if ((getRouter() != null) && (request.getResourceRef() != null)
                && (getTemplate() != null)) {
            final String remainingPart = request.getResourceRef()
                    .getRemainingPart(false, isMatchingQuery());
            if (remainingPart != null) {
                final int matchedLength = getTemplate().match(remainingPart);

                if (matchedLength != -1) {
                    final float totalLength = remainingPart.length();

                    if (totalLength > 0.0F) {
                        result = getRouter().getRequiredScore()
                                + (1.0F - getRouter().getRequiredScore())
                                * (matchedLength / totalLength);
                    } else {
                        result = 1.0F;
                    }
                }
            }

            if (request.isLoggable() && getLogger().isLoggable(Level.FINER)) {
                getLogger().finer(
                        "Call score for the \"" + getTemplate().getPattern()
                                + "\" URI pattern: " + result);
            }
        }

        return result;
    }

    /**
     * Sets the matching mode to use on the template when parsing a formatted
     * reference.
     * 
     * @param matchingMode
     *            The matching mode to use.
     */
    public void setMatchingMode(int matchingMode) {
        getTemplate().setMatchingMode(matchingMode);
    }

    /**
     * Sets whether the matching should be done on the URI with or without query
     * string.
     * 
     * @param matchingQuery
     *            True if the matching should be done with the query string,
     *            false otherwise.
     */
    public void setMatchingQuery(boolean matchingQuery) {
        this.matchingQuery = matchingQuery;
    }

    /**
     * Sets the reference template to match.
     * 
     * @param template
     *            The reference template to match.
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    @Override
    public String toString() {
        return "\""
                + ((getTemplate() == null) ? super.toString() : getTemplate()
                        .getPattern()) + "\" -> "
                + ((getNext() == null) ? "null" : getNext().toString());
    }
}
