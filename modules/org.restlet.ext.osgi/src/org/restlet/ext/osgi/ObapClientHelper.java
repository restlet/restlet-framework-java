/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.ext.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.osgi.framework.Bundle;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.local.Entity;
import org.restlet.engine.local.LocalClientHelper;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * Connector to the resources accessed via bundles.
 * 
 * @author Thierry Boileau
 */
public class ObapClientHelper extends LocalClientHelper {

    /** Map of registered bundles. */
    private final static Map<String, Bundle> BUNDLE_CACHE = new ConcurrentHashMap<String, Bundle>();

    /**
     * Registers the given bundle.
     * 
     * @param bundle
     *            The bundle to register.
     * 
     * @return True if the bundle was successfully registered.
     */
    public static boolean register(Bundle bundle) {
        boolean result = false;
        if (bundle != null && bundle.getSymbolicName() != null) {
            BUNDLE_CACHE.put(bundle.getSymbolicName(), bundle);
            result = true;
        }
        return result;
    }

    /**
     * Clears the registry of bundles.
     */
    public static void clear() {
        BUNDLE_CACHE.clear();
    }

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public ObapClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.OBAP);
    }

    /**
     * /** Handles a call with a given bundle.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param bundle
     *            The bundle to look for representations from.
     */
    protected void handleBundle(Request request, Response response,
            Bundle bundle) {
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

            // Get the URL to the bundle 'resource'
            if (bundle != null) {
                // As the path may be percent-encoded, it has to be
                // percent-decoded.
                url = bundle.getResource(Reference.decode(path));
            } else {
                getLogger()
                        .warning(
                                "Unable to get the resource. The selected bundle is null.");
            }

            // The Bundle returns a directory listing in some cases.
            // As this listing is partial, it is of little value in the context
            // of the OBAP client, so we have to ignore them.
            if (url != null) {
                if (url.getProtocol().equals("file")) {
                    File file = new File(url.getFile());
                    modificationDate = new Date(file.lastModified());

                    if (file.isDirectory()) {
                        url = null;
                    }
                } else if (url.getPath() != null && url.getPath().endsWith("/")) {
                    url = null;
                }
            }

            if (url != null) {
                try {
                    Representation output = new InputRepresentation(
                            url.openStream(),
                            metadataService.getDefaultMediaType());
                    output.setLocationRef(request.getResourceRef());
                    output.setModificationDate(modificationDate);

                    // Update the expiration date
                    long timeToLive = getTimeToLive();

                    if (timeToLive == 0) {
                        output.setExpirationDate(null);
                    } else if (timeToLive > 0) {
                        output.setExpirationDate(new Date(System
                                .currentTimeMillis() + (1000L * timeToLive)));
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

        if (scheme.equalsIgnoreCase(Protocol.OBAP.getSchemeName())) {
            Bundle bundle = BUNDLE_CACHE.get(request.getResourceRef()
                    .getAuthority());
            getLogger().info(
                    "Look for bundle "
                            + request.getResourceRef().getAuthority());
            handleBundle(request, response, bundle);
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only OBAP is supported.");
        }
    }

}
