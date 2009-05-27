/**
 * Copyright 2005-2009 Noelios Technologies.
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
import java.util.Collection;
import java.util.zip.ZipFile;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * ZIP and JAR client connector. Only works for archives available as local
 * files.
 * 
 * Handles GET, HEAD and PUT request on resources referenced as :
 * zip:file://<file path>
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class ZipClientHelper extends LocalClientHelper {

    public ZipClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.ZIP);
        getProtocols().add(Protocol.JAR);
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
        // Ensure that all ".." and "." are normalized into the path
        // to prevent unauthorized access to user directories.
        request.getResourceRef().normalize();
        String path = request.getResourceRef().getHierarchicalPart();

        // As the path may be percent-encoded, it has to be percent-decoded.
        // Then, all generated URIs must be encoded.
        final String decodedPath = Reference.decode(path);
        final MetadataService metadataService = getMetadataService(request);

        // Finally, actually handle the call
        handleEntity(request, response, path, decodedPath, metadataService);
    }

    /**
     * Handles a call for a local entity. By default, only GET and HEAD methods
     * are implemented.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param path
     *            The entity path.
     * @param decodedPath
     *            The URL decoded entity path.
     * @param metadataService
     *            The metadataService.
     */
    protected void handleEntity(Request request, Response response,
            String path, final String decodedPath,
            final MetadataService metadataService) {
        int spi = decodedPath.indexOf("!/");
        String fileUri;
        String entryName;
        if (spi != -1) {
            fileUri = decodedPath.substring(0, spi);
            entryName = decodedPath.substring(spi + 2);
        } else {
            fileUri = decodedPath;
            entryName = "";
        }

        LocalReference fileRef = new LocalReference(fileUri);
        if (Protocol.FILE.equals(fileRef.getSchemeProtocol())) {
            final File file = fileRef.getFile();
            if (Method.GET.equals(request.getMethod())
                    || Method.HEAD.equals(request.getMethod())) {
                handleGet(request, response, file, entryName, metadataService);
            } else {
                response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                response.getAllowedMethods().add(Method.GET);
                response.getAllowedMethods().add(Method.HEAD);
                response.getAllowedMethods().add(Method.PUT);
            }
        } else {
            response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED,
                    "Only works on local files.");
        }
    }

    /**
     * Handles a GET call.
     * 
     * @param request
     *            The request to answer.
     * @param response
     *            The response to update.
     * @param file
     *            The Zip archive file.
     * @param entryName
     *            The Zip archive entry name.
     * @param metadataService
     *            The metadata service.
     */
    protected void handleGet(Request request, Response response, File file,
            String entryName, final MetadataService metadataService) {

        if (file.exists()) {
            ZipFile zipFile;

            try {
                zipFile = new ZipFile(file);
            } catch (Exception e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return;
            }

            Entity entity = new ZipEntryEntity(zipFile, entryName);
            if (entity.exists()) {
                final Representation output;

                if (entity.isDirectory()) {
                    // Return the directory listing
                    final Collection<Entity> children = entity.getChildren();
                    final ReferenceList rl = new ReferenceList(children.size());
                    String fileUri = LocalReference.createFileReference(file)
                            .toString();
                    String scheme = request.getResourceRef().getScheme();
                    String baseUri = scheme + ":" + fileUri + "!/";

                    for (final Entity entry : children) {
                        rl.add(baseUri + entry.getName());
                    }

                    output = rl.getTextRepresentation();

                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        // Do something ???
                    }
                } else {
                    // Return the file content
                    output = entity.getRepresentation(metadataService
                            .getDefaultMediaType(), getTimeToLive());
                    output.setIdentifier(request.getResourceRef());
                    updateMetadata(metadataService, entity.getName(), output);
                }
                response.setStatus(Status.SUCCESS_OK);
                response.setEntity(output);
                return;
            } else {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }
        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }
}
