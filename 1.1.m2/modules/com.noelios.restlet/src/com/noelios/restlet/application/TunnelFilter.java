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

package com.noelios.restlet.application;

import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Filter tunnelling browser calls into full REST calls. The request method can
 * be changed (via POST requests only) as well as the accepted media types,
 * languages, encodings and character sets.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TunnelFilter extends Filter {
    /** The application. */
    private volatile Application application;

    /**
     * Constructor.
     * 
     * @param application
     *                The parent application.
     */
    public TunnelFilter(Application application) {
        super(application.getContext());
        this.application = application;
    }

    /**
     * Allows filtering before its handling by the target Restlet. Does nothing
     * by default.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @return The continuation status.
     */
    @Override
    public int beforeHandle(Request request, Response response) {
        Form query = request.getResourceRef().getQueryAsForm(null);
        boolean queryModified = false;

        // Tunnels the extracted attributes into the proper call objects.
        if (getApplication().getTunnelService().isMethodTunnel()
                && request.getMethod().equals(Method.POST)) {
            String methodName = query.getFirstValue(getApplication()
                    .getTunnelService().getMethodParameter());

            if (methodName != null) {
                request.setMethod(Method.valueOf(methodName));

                // The parameter is removed from the query
                query.removeFirst(getApplication().getTunnelService()
                        .getMethodParameter());
                queryModified = true;
            }
        }

        if (request.getMethod().equals(Method.GET)
                && getApplication().getTunnelService().isPreferencesTunnel()) {
            // Extract the header values
            String acceptCharset = query.getFirstValue(getApplication()
                    .getTunnelService().getCharacterSetParameter());
            String acceptEncoding = query.getFirstValue(getApplication()
                    .getTunnelService().getEncodingParameter());
            String acceptLanguage = query.getFirstValue(getApplication()
                    .getTunnelService().getLanguageParameter());
            String acceptMediaType = query.getFirstValue(getApplication()
                    .getTunnelService().getMediaTypeParameter());

            // Parse the headers and update the call preferences
            Metadata metadata = null;
            if (acceptCharset != null) {
                metadata = getApplication().getMetadataService().getMetadata(
                        acceptCharset);

                if (metadata instanceof CharacterSet) {
                    request.getClientInfo().getAcceptedCharacterSets().clear();
                    request.getClientInfo().getAcceptedCharacterSets().add(
                            new Preference<CharacterSet>(
                                    (CharacterSet) metadata));

                    // The parameter is removed from the query
                    query.removeFirst(getApplication().getTunnelService()
                            .getCharacterSetParameter());
                    queryModified = true;
                }
            }

            if (acceptEncoding != null) {
                metadata = getApplication().getMetadataService().getMetadata(
                        acceptEncoding);

                if (metadata instanceof Encoding) {
                    request.getClientInfo().getAcceptedEncodings().clear();
                    request.getClientInfo().getAcceptedEncodings().add(
                            new Preference<Encoding>((Encoding) metadata));

                    // The parameter is removed from the query
                    query.removeFirst(getApplication().getTunnelService()
                            .getEncodingParameter());
                    queryModified = true;
                }
            }

            if (acceptLanguage != null) {
                metadata = getApplication().getMetadataService().getMetadata(
                        acceptLanguage);

                if (metadata instanceof Language) {
                    request.getClientInfo().getAcceptedLanguages().clear();
                    request.getClientInfo().getAcceptedLanguages().add(
                            new Preference<Language>((Language) metadata));

                    // The parameter is removed from the query
                    query.removeFirst(getApplication().getTunnelService()
                            .getLanguageParameter());
                    queryModified = true;
                }
            }

            if (acceptMediaType != null) {
                metadata = getApplication().getMetadataService().getMetadata(
                        acceptMediaType);

                if (metadata instanceof MediaType) {
                    request.getClientInfo().getAcceptedMediaTypes().clear();
                    request.getClientInfo().getAcceptedMediaTypes().add(
                            new Preference<MediaType>((MediaType) metadata));

                    // The parameter is removed from the query
                    query.removeFirst(getApplication().getTunnelService()
                            .getMediaTypeParameter());
                    queryModified = true;
                }
            }
        }

        // Update the query if it has been modified
        if (queryModified) {
            request.getResourceRef().setQuery(query.getQueryString(null));
        }

        return CONTINUE;
    }

    /**
     * Returns the application.
     * 
     * @return The application.
     */
    public Application getApplication() {
        return this.application;
    }

}
