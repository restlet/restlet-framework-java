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

package org.restlet.ext.jaxrs.internal.provider;

import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.restlet.Application;
import org.restlet.ext.jaxrs.ExtendedUriBuilder;
import org.restlet.ext.jaxrs.ExtendedUriInfo;
import org.restlet.ext.jaxrs.internal.exceptions.NotAcceptableWebAppException;
import org.restlet.ext.jaxrs.internal.exceptions.UnsupportedMediaTypeWebAppException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

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
    private ExtendedUriInfo uriInfo;

    /**
     * Adds the extensions for the given {@link Variant}.
     * 
     * @param uriBuilder
     *            the UriBuilder to add the extensions.
     * @param variant
     *            the Variant to add the extensions for.
     * @return true, if the extensions where added, or false, if the extension
     *         for the media type is not available.
     */
    private boolean addExtensions(ExtendedUriBuilder uriBuilder, Variant variant) {
        uriBuilder.equals(null);
        String mediaTypeExt = null;
        String languageExt = null;
        String encodingExt = null;
        if (variant.getMediaType() != null) {
            mediaTypeExt = this.extBackwMapping.getByMediaType(variant
                    .getMediaType());
            if (mediaTypeExt == null) {
                return false;
            }
        }
        if (variant.getLanguage() != null) {
            languageExt = this.extBackwMapping.getByLanguage(variant
                    .getLanguage());
            if (languageExt == null) {
                languageExt = Converter.toLanguageString(variant.getLanguage());
            }
        }
        if (variant.getEncoding() != null) {
            encodingExt = this.extBackwMapping.getByEncoding(variant
                    .getEncoding());
            if (encodingExt == null) {
                encodingExt = variant.getEncoding();
            }
        }
        if (languageExt != null) {
            uriBuilder.extensionLanguage(languageExt);
        }
        if (mediaTypeExt != null) {
            uriBuilder.extensionMedia(mediaTypeExt);
        }
        if (encodingExt != null) {
            uriBuilder.queryParam(Application.getCurrent().getTunnelService()
                    .getEncodingParameter(), encodingExt);
        }
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
        if ((acceptedVariants != null) && acceptedVariants.isEmpty()) {
            acceptedVariants = null;
        }
        final ResponseBuilder rb = Response.fromResponse(response);
        final StringBuilder stb = new StringBuilder();

        // NICE speed optimization possible by using a Reader or InputStream,
        // which returns the values of String[] or better byte[][]
        stb.append("The given resource variant is not supported.");

        if (acceptedVariants != null) {
            stb.append("Please use one of the following:\n");
            stb.append("\n");
            for (final Variant variant : acceptedVariants) {
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
     *            the supported variants
     * @param response
     *            the Response to add the entity to.
     * @return a Response with a list of the given variants as entity. If the
     *         supportedVariants is null, the given {@link Response} is
     *         returned.
     */
    private Response requestOtherVariants(
            Collection<Variant> supportedVariants, Response response) {
        if ((supportedVariants != null) && supportedVariants.isEmpty()) {
            supportedVariants = null;
        }
        final ResponseBuilder rb = Response.fromResponse(response);
        boolean xhtml = false;
        boolean html = this.httpHeaders.getAcceptableMediaTypes().contains(
                MediaType.TEXT_HTML_TYPE);
        if (!html) {
            xhtml = this.httpHeaders.getAcceptableMediaTypes().contains(
                    MediaType.APPLICATION_XHTML_XML_TYPE);
            html = xhtml;
        }
        final StringBuilder stb = new StringBuilder();

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
            if (html) {
                stb.append("</p><ul>");
            }
            stb.append("\n");
            for (final Variant variant : supportedVariants) {
                final ExtendedUriBuilder uriBuilder = this.uriInfo
                        .getRequestUriBuilder();
                final boolean added = addExtensions(uriBuilder, variant);
                if (!added) {
                    continue;
                }
                final String uri = uriBuilder.build().toString();
                if (html) {
                    stb.append("<li><a href=\"");
                } else {
                    stb.append("* ");
                }
                stb.append(uri);
                if (html) {
                    stb.append("\">");
                    stb.append(uri);
                    stb.append("</a></li>");
                }
                stb.append("\n");
            }
            if (html) {
                stb.append("</ul>");
            }
        }

        if (html) {
            stb.append("</body></html>");
        }
        rb.entity(stb);
        if (xhtml) {
            rb.type(MediaType.APPLICATION_XHTML_XML_TYPE);
        } else if (html) {
            rb.type(MediaType.TEXT_HTML_TYPE);
        } else {
            rb.type(MediaType.TEXT_PLAIN_TYPE);
        }
        return rb.build();
    }

    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
     */
    public Response toResponse(WebApplicationException wae) {
        final Response response = wae.getResponse();
        if (response == null) {
            return null;
        }
        if (response.getEntity() != null) {
            return response;
        }
        if (response.getStatus() == NOT_ACCEPTABLE.getStatusCode()) {
            return requestOtherVariants(getSupportedVariants(wae), response);
        }
        if (response.getStatus() == UNSUPPORTED_MEDIA_TYPE.getStatusCode()) {
            return giveOtherVariant(getAcceptedVariants(wae), response);
        }
        return response;
    }
}