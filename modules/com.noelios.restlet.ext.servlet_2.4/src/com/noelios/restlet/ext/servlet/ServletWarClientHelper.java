/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.servlet;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.service.MetadataService;

import com.noelios.restlet.local.WarClientHelper;

/**
 * Local client connector based on a Servlet context (JEE Web application
 * context).
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServletWarClientHelper extends WarClientHelper {
    /** The Servlet context to use. */
    private ServletContext servletContext;

    /**
     * Constructor.
     * 
     * @param client
     *                The client to help.
     * @param servletContext
     *                The Servlet context
     */
    public ServletWarClientHelper(Client client, ServletContext servletContext) {
        super(client);
        this.servletContext = servletContext;
    }

    /**
     * Returns the Servlet context.
     * 
     * @return The Servlet context.
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleWar(Request request, Response response) {
        if (request.getMethod().equals(Method.GET)
                || request.getMethod().equals(Method.HEAD)) {
            String basePath = request.getResourceRef().getPath();
            int lastSlashIndex = basePath.lastIndexOf('/');
            String entry = (lastSlashIndex == -1) ? basePath : basePath
                    .substring(lastSlashIndex + 1);
            Representation output = null;

            if (basePath.endsWith("/")) {
                // Return the directory listing
                Set<String> entries = getServletContext().getResourcePaths(
                        basePath);
                // Directory listing may be null.
                if (entries != null) {
                    ReferenceList rl = new ReferenceList(entries.size());
                    rl.setIdentifier(request.getResourceRef());

                    for (Iterator<String> iter = entries.iterator(); iter
                            .hasNext();) {
                        entry = iter.next();
                        rl.add(new Reference(basePath
                                + entry.substring(basePath.length())));
                    }

                    output = rl.getTextRepresentation();
                }
            } else {
                // Return the entry content
                MetadataService metadataService = getMetadataService(request);
                InputStream ris = getServletContext().getResourceAsStream(
                        basePath);
                if (ris != null) {
                    output = new InputRepresentation(ris, metadataService
                            .getDefaultMediaType());
                    output.setIdentifier(request.getResourceRef());
                    updateMetadata(metadataService, entry, output);

                    // See if the Servlet context specified a particular Mime
                    // Type
                    String mediaType = getServletContext()
                            .getMimeType(basePath);

                    if (mediaType != null) {
                        output.setMediaType(new MediaType(mediaType));
                    }
                }
            }

            response.setEntity(output);
            response.setStatus(Status.SUCCESS_OK);
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
        }
    }

}
