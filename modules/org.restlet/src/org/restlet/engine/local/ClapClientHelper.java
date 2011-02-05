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

package org.restlet.engine.local;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * Connector to the resources accessed via class loaders. Note that if you use
 * the class authority for your CLAP URIs, you can provide a custom classloader
 * instead of the one of the connector. For this, your requests need to have a
 * "org.restlet.clap.classLoader" attribute set with the instance of your
 * classloader and use the {@link LocalReference#CLAP_CLASS} authority.
 * 
 * @author Jerome Louvel
 */
public class ClapClientHelper extends LocalClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public ClapClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.CLAP);
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
        MetadataService metadataService = getMetadataService();

        if (request.getMethod().equals(Method.GET)
                || request.getMethod().equals(Method.HEAD)) {
            String path = request.getResourceRef().getPath();
            URL url = null;
            Date modificationDate = null;

            // Prepare a classloader URI, removing the leading slash
            if ((path != null) && path.startsWith("/")) {
                path = path.substring(1);
            }

            // Get the URL to the classloader 'resource'
            if (classLoader != null) {
                // As the path may be percent-encoded, it has to be
                // percent-decoded.
                url = classLoader.getResource(Reference.decode(path));
            } else {
                getLogger()
                        .warning(
                                "Unable to get the resource. The selected classloader is null.");
            }

            // The ClassLoader returns a directory listing in some cases.
            // As this listing is partial, it is of little value in the context
            // of the CLAP client, so we have to ignore them.
            if (url != null) {
                if (url.getProtocol().equals("file")) {
                    File file = new File(url.getFile());
                    modificationDate = new Date(file.lastModified());

                    if (file.isDirectory()) {
                        url = null;
                    }
                }
            }

            if (url != null) {
                try {
                    Representation output = new InputRepresentation(url
                            .openStream(), metadataService
                            .getDefaultMediaType());
                    output.setLocationRef(request.getResourceRef());
                    output.setModificationDate(modificationDate);

                    // Update the expiration date
                    long timeToLive = getTimeToLive();

                    if (timeToLive == 0) {
                        output.setExpirationDate(null);
                    } else if (timeToLive > 0) {
                        output.setExpirationDate(new Date(System
                                .currentTimeMillis()
                                + (1000L * timeToLive)));
                    }

                    // Update the metadata based on file extensions
                    String name = path.substring(path.lastIndexOf('/') + 1);
                    Entity.updateMetadata(name, output, true,
                            getMetadataService());

                    // Update the response
                    response.setEntity(output);
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

    @Override
    protected void handleLocal(Request request, Response response,
            String decodedPath) {
        String scheme = request.getResourceRef().getScheme();

        if (scheme.equalsIgnoreCase(Protocol.CLAP.getSchemeName())) {
            LocalReference cr = new LocalReference(request.getResourceRef());
            ClassLoader classLoader = null;

            if ((cr.getClapAuthorityType() == LocalReference.CLAP_CLASS)
                    || (cr.getClapAuthorityType() == LocalReference.CLAP_DEFAULT)) {
                // Sometimes, a specific class loader needs to be used,
                // make sure that it can be provided as a request's attribute
                Object classLoaderAttribute = request.getAttributes().get(
                        "org.restlet.clap.classLoader");

                if (classLoaderAttribute != null) {
                    classLoader = (ClassLoader) classLoaderAttribute;
                } else {
                    // Old name to be deprecated
                    classLoaderAttribute = request.getAttributes().get(
                            "org.restlet.clap.classloader");

                    if (classLoaderAttribute != null) {
                        classLoader = (ClassLoader) classLoaderAttribute;
                    } else {
                        classLoader = getClass().getClassLoader();
                    }
                }
            } else if (cr.getClapAuthorityType() == LocalReference.CLAP_SYSTEM) {
                classLoader = ClassLoader.getSystemClassLoader();
            } else if (cr.getClapAuthorityType() == LocalReference.CLAP_THREAD) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }

            handleClassLoader(request, response, classLoader);
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only CLAP is supported.");
        }
    }
}
