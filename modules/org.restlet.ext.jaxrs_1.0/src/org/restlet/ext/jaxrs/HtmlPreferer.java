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

import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * <p>
 * Some browsers sends as accepted media type XML with a higher quality than
 * HTML. The Internet Explorer 7.0 and Firefox 2.0 have this behavior, for
 * example. The consequence is, that a HTTP server sends XML instead of HTML, if
 * it could produce XML. To avoid this, you can use this filter.<br>
 * It is fully independent of the JAX-RS extension, so you can use it
 * everywhere, were you can use any {@link Filter}.
 * </p>
 * <p>
 * This Filter will increase the qualities for HTML media types (text/html and
 * application/xhtml+xml) higher than both XML types (text/xml and
 * application/xml), if at least one of both is available in a request. <br>
 * Responses are not changed.
 * </p>
 * <p>
 * Some informations for developer:<br>
 * <ul>
 * <li>The check, if the Request should change is implemented in method
 * {@link #shouldChangeToPrefereHtml(Request)}. You may alter the test by
 * subclass this Filter and override that method.</li>
 * <li>The Request change is implemented in method
 * {@link #prefereHtml(Request)}. You may alter the changing process by
 * subclass this Filter and override that method.</li>
 * </ul>
 * </p>
 * 
 * @author Stephan Koops
 */
public class HtmlPreferer extends Filter {

    private static final int MT_PREF_APP_XHTML = 1;

    private static final int MT_PREF_APP_XML = 3;

    private static final int MT_PREF_TEXT_HTML = 0;

    private static final int MT_PREF_TEXT_XML = 2;

    /**
     * The preferences for the HTML and XML datatypes are stored in the request
     * attributes, available with this key. This pair will be removed after
     * finish the changes.
     */
    private static final String MT_QUALITY_ARRAY = "org.restlet.HtmlPreferer.qualities";

    /**
     * Creates a new {@link HtmlPreferer}. You should use constructor
     * {@link #HtmlPreferer(Context)} or {@link #HtmlPreferer(Context, Restlet)}.
     */
    @Deprecated
    public HtmlPreferer() {
        super();
    }

    /**
     * Creates a new {@link HtmlPreferer}. You can give also the next restlet
     * by using constructor {@link #HtmlPreferer(Context, Restlet)}.
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
        request.getAttributes().remove(MT_QUALITY_ARRAY);
        return super.beforeHandle(request, response);
    }

    /**
     * Returns the quality of accepted media type application/xhtml+xml, or
     * null, if not present in the given request.
     * 
     * @param request
     * @return the quality of accepted media type application/xhtml+xml, or
     *         null, if not present in the given request.
     * @see #getHtmlMinQuality(Request)
     */
    protected Float getAppXhtmlQuality(Request request) {
        return getHtmlXmlMtQualities(request)[MT_PREF_APP_XHTML];
    }

    /**
     * Returns the quality of accepted media type app/xml, or null, if not
     * present in the given request.
     * 
     * @param request
     * @return the quality of accepted media type app/xml, or null, if not
     *         present in the given request.
     * @see #getXmlMaxQuality(Request)
     */
    protected Float getAppXmlQuality(Request request) {
        return getHtmlXmlMtQualities(request)[MT_PREF_APP_XML];
    }

    /**
     * Returns the lowest quality of the HTML types (text/html and
     * application/xhtml+xml), or null, if not available.
     * 
     * @param request
     * @return the lowest quality of the HTML types (text/html and
     *         application/xhtml+xml), or null, if not available.
     * @see #getTextHtmlQuality(Request)
     * @see #getAppXhtmlQuality(Request)
     */
    protected Float getHtmlMinQuality(Request request) {
        Float xhtmlQuality = getAppXhtmlQuality(request);
        Float htmlQuality = getTextHtmlQuality(request);
        if (xhtmlQuality == null)
            return htmlQuality;
        if (htmlQuality == null)
            return xhtmlQuality;
        return Math.min(xhtmlQuality, htmlQuality);
    }

    @SuppressWarnings("unchecked")
    private Float[] getHtmlXmlMtQualities(Request request) {
        Float[] htmlXmlQualities;
        Map<String, Object> attributes = request.getAttributes();
        htmlXmlQualities = (Float[]) attributes.get(MT_QUALITY_ARRAY);
        if (htmlXmlQualities == null) {
            htmlXmlQualities = new Float[4];
            List<Preference<MediaType>> acceptedMediaTypes = request
                    .getClientInfo().getAcceptedMediaTypes();
            for (Preference<MediaType> accPref : acceptedMediaTypes) {
                MediaType accMediaType = accPref.getMetadata();
                if (accMediaType.equals(MediaType.TEXT_HTML, true))
                    htmlXmlQualities[MT_PREF_TEXT_HTML] = accPref.getQuality();
                if (accMediaType.equals(MediaType.TEXT_XML, true))
                    htmlXmlQualities[MT_PREF_TEXT_XML] = accPref.getQuality();
                if (accMediaType.equals(MediaType.APPLICATION_XHTML_XML, true))
                    htmlXmlQualities[MT_PREF_APP_XHTML] = accPref.getQuality();
                if (accMediaType.equals(MediaType.APPLICATION_XML, true))
                    htmlXmlQualities[MT_PREF_APP_XML] = accPref.getQuality();
            }
            attributes.put(MT_QUALITY_ARRAY, htmlXmlQualities);
        }
        return htmlXmlQualities;
    }

    /**
     * Returns the quality of accepted media type text/html, or null, if not
     * present in the given request.
     * 
     * @param request
     * @return the quality of accepted media type text/html, or null, if not
     *         present in the given request.
     * @see #getHtmlMinQuality(Request)
     */
    protected Float getTextHtmlQuality(Request request) {
        return getHtmlXmlMtQualities(request)[MT_PREF_TEXT_HTML];
    }

    /**
     * Returns the quality of accepted media type text/xml, or null, if not
     * present in the given request.
     * 
     * @param request
     * @return the quality of accepted media type text/xml, or null, if not
     *         present in the given request.
     * @see #getXmlMaxQuality(Request)
     */
    protected Float getTextXmlQuality(Request request) {
        return getHtmlXmlMtQualities(request)[MT_PREF_TEXT_XML];
    }

    /**
     * Returns the highest quality of the XML types (text/xml and
     * application/xml), or null, if not available.
     * 
     * @param request
     * @return the highest quality of the XML types (text/xml and
     *         application/xml), or null, if not available.
     * @see #getAppXmlQuality(Request)
     * @see #getTextXmlQuality(Request)
     */
    protected Float getXmlMaxQuality(Request request) {
        Float appXmlQuality = getAppXmlQuality(request);
        Float textXmlQuality = getTextXmlQuality(request);
        if (appXmlQuality == null)
            return textXmlQuality;
        if (textXmlQuality == null)
            return appXmlQuality;
        return Math.max(appXmlQuality, textXmlQuality);
    }

    /**
     * Alters the request, that HTML is prefered before XML.
     * 
     * @param request
     *                the request to alter.
     */
    protected void prefereHtml(Request request) {
        Float xmlQualityO = getXmlMaxQuality(request);
        if (xmlQualityO == null)
            return;
        float xmlQuality = xmlQualityO;
        float htmlMinQuality;
        if (xmlQuality < 1) {
            htmlMinQuality = xmlQuality + 0.001f;
        } else {
            lowerWithQuality(request, xmlQuality);
            htmlMinQuality = 1;
        }
        htmlPrefsMin(request, htmlMinQuality);
    }

    /**
     * Lowers all accepted media type Preferences with the given quality to
     * 0.001 (littlest accuracy for HTML qualities). Also lowers recursive the
     * preferences with the goal quality.
     * 
     * @param request
     * @param quality
     */
    private void lowerWithQuality(Request request, float quality) {
        List<Preference<MediaType>> acceptedMediaTypes = request
                .getClientInfo().getAcceptedMediaTypes();
        float goalQuality = quality - 0.001f;
        boolean alreadyAvailable = false;
        for (Preference<MediaType> accPref : acceptedMediaTypes) {
            if (accPref.getQuality() == goalQuality) {
                alreadyAvailable = true;
                break;
            }
        }
        if (alreadyAvailable)
            lowerWithQuality(request, goalQuality);
        for (Preference<MediaType> accPref : acceptedMediaTypes) {
            if (accPref.getQuality() == quality)
                accPref.setQuality(goalQuality);
        }
    }

    /**
     * sets the quality of the preferences of the HTML media types (text/html
     * and app/xhtml) at least to the given quality.
     * 
     * @param request
     * @param htmlQuality
     */
    private void htmlPrefsMin(Request request, float htmlQuality) {
        for (Preference<MediaType> accPref : request.getClientInfo()
                .getAcceptedMediaTypes()) {
            MediaType accMediaType = accPref.getMetadata();
            if (accMediaType.equals(MediaType.APPLICATION_XHTML_XML, true)
                    || accMediaType.equals(MediaType.TEXT_HTML, true)) {
                if (accPref.getQuality() < htmlQuality)
                    accPref.setQuality(htmlQuality);
            }
        }
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
        Float htmlQuality = getHtmlMinQuality(request);
        Float xmlQuality = getXmlMaxQuality(request);
        if (htmlQuality == null || xmlQuality == null)
            return false;
        return xmlQuality >= htmlQuality;
    }
}