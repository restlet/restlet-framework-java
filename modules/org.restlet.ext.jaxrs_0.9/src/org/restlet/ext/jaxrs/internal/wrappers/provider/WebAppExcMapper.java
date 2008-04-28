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
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.restlet.ext.jaxrs.internal.exceptions.NotAcceptableWebAppException;
import org.restlet.ext.jaxrs.internal.exceptions.UnsupportedMediaTypeWebAppException;

/**
 * The exception mapper for {@link WebApplicationException}s.
 * 
 * @author Stephan Koops
 */
public class WebAppExcMapper implements
        ExceptionMapper<WebApplicationException> {

    @Context
    private ExtensionBackwardMapping extBackwMapping;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    /**
     * Adds the extensions for the given {@link Variant}.
     * 
     * @param uriBuilder
     *                the UriBuilder to add the extensions.
     * @param variant
     *                the Variant to add the extensions for.
     * @return true, if the extensions where added, or false, if the extension
     *         for the media type is not available.
     */
    private boolean addExtensions(UriBuilder uriBuilder, Variant variant) {
        String mediaTypeExt = null;
        String languageExt = null;
        String encodingExt = null;
        if (variant.getMediaType() != null) {
            mediaTypeExt = extBackwMapping.getByMediaType(variant
                    .getMediaType());
            if (mediaTypeExt == null)
                return false;
        }
        if (variant.getLanguage() != null) {
            languageExt = extBackwMapping.getByLanguage(variant.getLanguage());
            if (languageExt == null)
                languageExt = variant.getLanguage();
        }
        if (variant.getEncoding() != null) {
            encodingExt = extBackwMapping.getByEncoding(variant.getEncoding());
            if (encodingExt == null)
                encodingExt = variant.getEncoding();
        }
        if (languageExt != null)
            uriBuilder.extension(languageExt);
        if (mediaTypeExt != null)
            uriBuilder.extension(mediaTypeExt);
        if (encodingExt != null)
            uriBuilder.extension(encodingExt);
        return true;
    }

    /**
     * @return the allowed variants for an unsupported media type exception, or
     *         null if they could not be found.
     */
    private Collection<Variant> getAcceptedVariants(WebApplicationException wae) {
        if (wae instanceof UnsupportedMediaTypeWebAppException) {
            return ((UnsupportedMediaTypeWebAppException) wae).getAccepted();
        }
        return null;
    }

    /**
     * @return the allowed variants for an unsupported media type exception, or
     *         null if they could not be found.
     */
    private Collection<Variant> getSupportedVariants(WebApplicationException wae) {
        if (wae instanceof NotAcceptableWebAppException) {
            return ((NotAcceptableWebAppException) wae).getSupported();
        }
        return null;
    }

    /**
     * Creates an entity with a list of links to the accepted variants.
     */
    private Response giveOtherVariant(Collection<Variant> acceptedVariants,
            Response response) {
        if (acceptedVariants != null && acceptedVariants.isEmpty())
            acceptedVariants = null;
        ResponseBuilder rb = Response.fromResponse(response);
        // REQUEST HttpHeaders.getAccepted*: explicit define "not null"
        StringBuilder stb = new StringBuilder();

        // NICE speed optimization possible by using a Reader or InputStream,
        // which returns the values of String[] or better byte[][]
        stb.append("The given resource variant is not supported.");

        if (acceptedVariants != null) {
            stb.append("Please use one of the following:\n");
            stb.append("\n");
            for (Variant variant : acceptedVariants) {
                stb.append("* ");
                stb.append(variant);
                stb.append("\n");
            }
        }
        rb.entity(stb);
        rb.type(MediaType.TEXT_PLAIN_TYPE);
        return rb.build();
    }

    /**
     * Creates an entity with a list of links to the supported variants.
     * 
     * @param supportedVariants
     *                the supported variants
     * @param response
     *                the Response to add the entity to.
     * @return a Response with a list of the given variants as entity. If the
     *         supportedVariants is null, the given {@link Response} is
     *         returned.
     */
    private Response requestOtherVariants(
            Collection<Variant> supportedVariants, Response response) {
        if (supportedVariants != null && supportedVariants.isEmpty())
            supportedVariants = null;
        ResponseBuilder rb = Response.fromResponse(response);
        // REQUEST HttpHeaders.getAccepted*: explicit define "not null"
        boolean xhtml = false;
        boolean html = httpHeaders.getAcceptableMediaTypes().contains(
                MediaType.TEXT_HTML_TYPE);
        if (!html) {
            xhtml = httpHeaders.getAcceptableMediaTypes().contains(
                    MediaType.APPLICATION_XHTML_XML_TYPE);
            html = xhtml;
        }
        StringBuilder stb = new StringBuilder();

        // NICE speed optimization possible by using a Reader or InputStream,
        // which returns the values of String[] or better byte[][]
        if (html) {
            stb.append("<html><head>\n");
            stb.append("<title>The requested variant is not available</title>");
            stb.append("\n</head>\n<body>\n<p>\n");
        }
        stb.append("The requested variant is not available.");

        if (supportedVariants != null) {
            stb.append(" Try one of the following:\n");
            if (html)
                stb.append("</p><ul>");
            stb.append("\n");
            for (Variant variant : supportedVariants) {
                UriBuilder uriBuilder = uriInfo.getPlatonicRequestUriBuilder();
                boolean added = addExtensions(uriBuilder, variant);
                if (!added)
                    continue;
                String uri = uriBuilder.build().toString();
                if (html)
                    stb.append("<li><a href=\"");
                else
                    stb.append("* ");
                stb.append(uri);
                if (html) {
                    stb.append("\">");
                    stb.append(uri);
                    stb.append("</a></li>");
                }
                stb.append("\n");
            }
            if (html)
                stb.append("</ul>");
        }

        if (html)
            stb.append("</body></html>");
        rb.entity(stb);
        if (xhtml)
            rb.type(MediaType.APPLICATION_XHTML_XML_TYPE);
        else if (html)
            rb.type(MediaType.TEXT_HTML_TYPE);
        else
            rb.type(MediaType.TEXT_PLAIN_TYPE);
        return rb.build();
    }

    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
     */
    public Response toResponse(WebApplicationException wae) {
        Response response = wae.getResponse();
        if (response == null)
            return null;
        if (response.getEntity() != null)
            return response;
        if (response.getStatus() == NOT_ACCEPTABLE.getStatusCode())
            return requestOtherVariants(getSupportedVariants(wae), response);
        if (response.getStatus() == UNSUPPORTED_MEDIA_TYPE.getStatusCode())
            return giveOtherVariant(getAcceptedVariants(wae), response);
        return response;
    }
}