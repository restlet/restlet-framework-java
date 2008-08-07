/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.local;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.service.MetadataService;

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
	@Override
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
        MetadataService metadataService = getMetadataService(request);

        if (request.getMethod().equals(Method.GET)
                || request.getMethod().equals(Method.HEAD)) {
            String path = request.getResourceRef().getPath();

            // Prepare a classloader URI, removing the leading slash
            if ((path != null) && path.startsWith("/"))
                path = path.substring(1);
            // As the path may be percent-encoded, it has to be percent-decoded.
            URL url = classLoader.getResource(Reference.decode(path));

            // The ClassLoader returns a directory listing in some cases.
            // As this listing is partial, it is of little value in the context
            // of the CLAP client, so we have to ignore them.
            if (url != null) {
                if (url.getProtocol().equals("file")) {
                    File file = new File(url.getFile());
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
                    output.setIdentifier(request.getResourceRef());

                    // Update the metadata based on file extensions
                    String name = path.substring(path.lastIndexOf('/') + 1);
                    updateMetadata(metadataService, name, output);

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

}
