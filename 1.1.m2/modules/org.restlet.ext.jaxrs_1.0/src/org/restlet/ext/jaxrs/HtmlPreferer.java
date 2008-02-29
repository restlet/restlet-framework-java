/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * <p>
 * Some Browser (e.g. Internet Explorer 7.0 and Firefox 2.0) sends as accepted
 * media type XML with a higher quality than html. The consequence is, that a
 * HTTP server sends XML instead of HTML, if it could produce XML. To avoid
 * this, you can use this filter.
 * </p>
 * <p>
 * This Filter will return HTML before XML, if both is available. The check is
 * implemented in method {@link #shouldChangeToPrefereHtml(Request)}.
 * <br/>Other requests are not effected.
 * </p>
 * 
 * @author Stephan Koops
 */
public class HtmlPreferer extends Filter {

    /**
     * Creates a new {@link HtmlPreferer}. You should use constructor
     * {@link #HtmlPreferer(Context)} or {@link #HtmlPreferer(Context, Restlet)}.
     */
    public HtmlPreferer() {
        super();
    }

    /**
     * Creates a new {@link HtmlPreferer}. You can give also the next by using
     * constructor {@link #HtmlPreferer(Context, Restlet)}.
     * 
     * @param context
     *                the context from the parent
     */
    public HtmlPreferer(Context context) {
        super(context);
    }

    /**
     * Creates a new {@link HtmlPreferer}.
     * 
     * @param context
     *                the context from the parent
     * @param next
     *                the {@link Restlet} to call after filtering.
     */
    public HtmlPreferer(Context context, Restlet next) {
        super(context, next);
    }

    /**
     * Allows filtering before processing by the next Restlet.
     * 
     * @param request
     *                The request to filter.
     * @param response
     *                The response to update.
     * @return The continuation status, see
     *         {@link Filter#beforeHandle(Request, Response)}
     * @see Filter#beforeHandle(Request, Response)
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (shouldChangeToPrefereHtml(request))
            prefereHtml(request);
        return super.beforeHandle(request, response);
    }

    /**
     * This method checks, if HTML should be prefered for the given request. The
     * check may be overridden or complemented by override that method.
     * 
     * @param request
     *                the request to check.
     * @return true, if the {@link Request} should be altered, or false if not.
     */
    protected boolean shouldChangeToPrefereHtml(Request request) {
        float htmlQuality = Float.MAX_VALUE;
        float xmlQuality = Float.MIN_VALUE;
        for (Preference<MediaType> acc : request.getClientInfo()
                .getAcceptedMediaTypes()) {
            MediaType accMediaType = acc.getMetadata();
            if (accMediaType.equals(MediaType.APPLICATION_XHTML_XML, true)
                    || accMediaType.equals(MediaType.TEXT_HTML, true))
                htmlQuality = acc.getQuality();
            if (accMediaType.equals(MediaType.APPLICATION_XML, true)
                    || accMediaType.equals(MediaType.TEXT_XML, true))
                xmlQuality = acc.getQuality();
        }
        return htmlQuality <= xmlQuality;
    }

    /**
     * Alters the request, that HTML is prefered before XML.
     * 
     * @param request the request to alter.
     */
    protected void prefereHtml(Request request) {
        float xmlQuality = Float.MIN_VALUE;
        for (Preference<MediaType> acc : request.getClientInfo()
                .getAcceptedMediaTypes()) {
            MediaType accMediaType = acc.getMetadata();
            if (accMediaType.equals(MediaType.APPLICATION_XML, true)
                    || accMediaType.equals(MediaType.TEXT_XML, true)) {
                xmlQuality = acc.getQuality();
                if (xmlQuality >= 1f) {
                    xmlQuality = 0.999f;
                    acc.setQuality(xmlQuality);
                }
            }
        }
        float htmlQuality = xmlQuality + 0.001f;
        for (Preference<MediaType> acc : request.getClientInfo()
                .getAcceptedMediaTypes()) {
            MediaType accMediaType = acc.getMetadata();
            if (accMediaType.equals(MediaType.APPLICATION_XHTML_XML, true)
                    || accMediaType.equals(MediaType.TEXT_HTML, true))
                acc.setQuality(htmlQuality);
        }
    }
}