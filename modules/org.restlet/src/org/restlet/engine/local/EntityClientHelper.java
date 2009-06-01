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

import java.util.Collection;
import java.util.Iterator;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * Connector to the local entities. That connector supports the content
 * negotiation feature (i.e. for GET and HEAD methods) and implements the
 * response to GET/HEAD methods.
 * 
 * @author Thierry Boileau
 */
public abstract class EntityClientHelper extends LocalClientHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public EntityClientHelper(Client client) {
        super(client);
    }

    /**
     * Generate a Reference for a variant name (which is URL decoded) and handle
     * the translation between the incoming requested path (which is URL
     * encoded).
     * 
     * @param scheme
     *            The scheme of the requested resource.
     * @param encodedParentDirPath
     *            The encoded path of the parent dir of the requested resource.
     * @param encodedEntityName
     *            The encoded name of the requested resource.
     * @param decodedVariantName
     *            The decoded name of a returned resource.
     * @return A new Reference.
     */
    public Reference createReference(String scheme,
            String encodedParentDirPath, String encodedEntityName,
            String decodedVariantName) {
        Reference result = new Reference(scheme
                + "://"
                + encodedParentDirPath
                + "/"
                + getReencodedVariantEntityName(encodedEntityName,
                        decodedVariantName));
        return result;
    }

    /**
     * Returns a local entity for the given path.
     * 
     * @param path
     *            The path of the entity.
     * @return A local entity for the given path.
     */
    public abstract Entity getEntity(String path);

    /**
     * Percent-encodes the given percent-decoded variant name of a resource
     * whose percent-encoded name is given. Tries to match the longest common
     * part of both encoded entity name and decoded variant name.
     * 
     * @param encodedEntityName
     *            the percent-encoded name of the initial resource
     * @param decodedVariantEntityName
     *            the percent-decoded entity name of a variant of the initial
     *            resource.
     * @return The variant percent-encoded entity name.
     */
    protected String getReencodedVariantEntityName(String encodedEntityName,
            String decodedVariantEntityName) {
        int i = 0;
        int j = 0;
        boolean stop = false;
        char[] encodeds = encodedEntityName.toCharArray();
        char[] decodeds = decodedVariantEntityName.toCharArray();

        for (i = 0; (i < decodeds.length) && (j < encodeds.length) && !stop; i++) {
            char decodedChar = decodeds[i];
            char encodedChar = encodeds[j];

            if (encodedChar == '%') {
                String dec = Reference.decode(encodedEntityName.substring(j,
                        j + 3));
                if (decodedChar == dec.charAt(0)) {
                    j += 3;
                } else {
                    stop = true;
                }
            } else if (decodedChar == encodedChar) {
                j++;
            } else {
                String dec = Reference.decode(encodedEntityName.substring(j,
                        j + 1));
                if (decodedChar == dec.charAt(0)) {
                    j++;
                } else {
                    stop = true;
                }
            }
        }

        if (stop) {
            return encodedEntityName.substring(0, j)
                    + decodedVariantEntityName.substring(i - 1);
        }

        if (j == encodedEntityName.length()) {
            return encodedEntityName.substring(0, j)
                    + decodedVariantEntityName.substring(i);
        }

        return encodedEntityName.substring(0, j - 1);
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
        String path = request.getResourceRef().getPath();

        // As the path may be percent-encoded, it has to be percent-decoded.
        // Then, all generated uris must be encoded.
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
        if (Method.GET.equals(request.getMethod())
                || Method.HEAD.equals(request.getMethod())) {
            handleEntityGet(request, response, path, getEntity(decodedPath),
                    metadataService);
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
        }
    }

    /**
     * Handles a GET call.
     * 
     * @param request
     *            The request to answer.
     * @param response
     *            The response to update.
     * @param path
     *            The encoded path of the requested entity.
     * @param entity
     *            The requested entity (normal or directory).
     * @param metadataService
     *            The metadata service.
     */
    protected void handleEntityGet(Request request, Response response,
            String path, Entity entity, final MetadataService metadataService) {
        Representation output = null;

        // Get variants for a resource
        boolean found = false;
        final Iterator<Preference<MediaType>> iterator = request
                .getClientInfo().getAcceptedMediaTypes().iterator();
        while (iterator.hasNext() && !found) {
            final Preference<MediaType> pref = iterator.next();
            found = pref.getMetadata().equals(MediaType.TEXT_URI_LIST);
        }

        if (found) {
            // Try to list all variants of this resource
            // 1- set up base name as the longest part of the name without known
            // extensions (beginning from the left)
            final String baseName = entity.getBaseName(metadataService);

            // 2- looking for resources with the same base name
            Entity parent = entity.getParent();

            if (parent != null) {
                final Collection<Entity> entities = parent.getChildren();

                if (entities != null) {
                    final ReferenceList rl = new ReferenceList(entities.size());
                    final String scheme = request.getResourceRef().getScheme();
                    final String encodedParentDirectoryURI = path.substring(0,
                            path.lastIndexOf("/"));
                    final String encodedEntityName = path.substring(path
                            .lastIndexOf("/") + 1);

                    for (final Entity entry : entities) {
                        if (baseName.equals(entry.getBaseName(metadataService))) {
                            rl.add(createReference(scheme,
                                    encodedParentDirectoryURI,
                                    encodedEntityName, entry.getName()));
                        }
                    }

                    output = rl.getTextRepresentation();
                }
            }
        } else {
            if (entity.exists()) {
                if (entity.isDirectory()) {
                    // Return the directory listing
                    final Collection<Entity> children = entity.getChildren();
                    final ReferenceList rl = new ReferenceList(children.size());
                    String directoryUri = request.getResourceRef().toString();

                    // Ensures that the directory URI ends with a slash
                    if (!directoryUri.endsWith("/")) {
                        directoryUri += "/";
                    }

                    for (final Entity entry : children) {
                        if (entry.isDirectory()) {
                            rl.add(directoryUri
                                    + Reference.encode(entry.getName()) + "/");
                        } else {
                            rl.add(directoryUri
                                    + Reference.encode(entry.getName()));
                        }
                    }

                    output = rl.getTextRepresentation();
                } else {
                    // Return the file content
                    output = entity.getRepresentation(metadataService
                            .getDefaultMediaType(), getTimeToLive());
                    output.setIdentifier(request.getResourceRef());
                    updateMetadata(metadataService, entity.getName(), output);
                }
            } else {
                // We look for the possible variant which has the same
                // extensions in a distinct order.

                Entity uniqueVariant = null;

                // 1- set up base name as the longest part of the name without
                // known extensions (beginning from the left)
                final String baseName = entity.getBaseName(metadataService);
                final Collection<String> extensions = entity
                        .getExtensions(metadataService);

                // 2- loooking for resources with the same base name
                Entity parent = entity.getParent();
                if (parent != null) {
                    final Collection<Entity> files = parent.getChildren();

                    if (files != null) {
                        for (final Entity entry : files) {
                            if (baseName.equals(entry
                                    .getBaseName(metadataService))) {
                                final Collection<String> entryExtensions = entry
                                        .getExtensions(metadataService);
                                if (entryExtensions.containsAll(extensions)
                                        && extensions
                                                .containsAll(entryExtensions)) {
                                    // The right representation has been found.
                                    uniqueVariant = entry;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (uniqueVariant != null) {
                    // Return the file content
                    output = uniqueVariant.getRepresentation(metadataService
                            .getDefaultMediaType(), getTimeToLive());
                    output.setIdentifier(request.getResourceRef());
                    updateMetadata(metadataService, entity.getName(), output);
                }
            }
        }

        if (output == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        } else {
            output.setIdentifier(request.getResourceRef());
            response.setEntity(output);
            response.setStatus(Status.SUCCESS_OK);
        }
    }
}
