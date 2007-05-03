/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.local;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;

/**
 * Connector to the class loaders.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ClapClientHelper extends LocalClientHelper {
    /**
     * Constructor. Note that the common list of metadata associations based on
     * extensions is added, see the addCommonExtensions() method.
     * 
     * @param client
     *            The client to help.
     */
    public ClapClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.CLAP);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        String scheme = request.getResourceRef().getScheme();

        // Ensure that all ".." and "." are normalized into the path
        // to preven unauthorized access to user directories.
        request.getResourceRef().normalize();

        if (scheme.equalsIgnoreCase(Protocol.CLAP.getSchemeName())) {
            LocalReference cr = new LocalReference(request.getResourceRef());

            if (cr.getClapAuthorityType() == LocalReference.CLAP_CLASS) {
                handleClassLoader(request, response, getClass()
                        .getClassLoader());
            } else if (cr.getClapAuthorityType() == LocalReference.CLAP_SYSTEM) {
                handleClassLoader(request, response, ClassLoader
                        .getSystemClassLoader());
            } else if (cr.getClapAuthorityType() == LocalReference.CLAP_THREAD) {
                handleClassLoader(request, response, Thread.currentThread()
                        .getContextClassLoader());
            }
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only CLAP is supported.");
        }
    }

    /**
     * Handles a call with a given class loader.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void handleClassLoader(Request request, Response response,
            ClassLoader classLoader) {
        if (request.getMethod().equals(Method.GET)
                || request.getMethod().equals(Method.HEAD)) {
            URL url = classLoader.getResource(request.getResourceRef()
                    .getPath());

            if (url != null) {
                try {
                    response.setEntity(new InputRepresentation(
                            url.openStream(), null));
                    response.setStatus(Status.SUCCESS_OK);
                } catch (IOException ioe) {
                    getLogger().log(Level.WARNING,
                            "Unable to open the representation's input stream",
                            ioe);
                    response.setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            } else {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
        }
    }

}
