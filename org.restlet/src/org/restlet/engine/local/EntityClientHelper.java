/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.engine.local;

import java.util.Collection;
import java.util.Iterator;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

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
     *            The encoded path of the parent directory of the requested
     *            resource.
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

        return encodedEntityName.substring(0, j);
    }

    /**
     * Handles a GET call.
     * 
     * @param request
     *            The request to answer.
     * @param response
     *            The response to update.
     * @param entity
     *            The requested entity (normal or directory).
     */
    protected void handleEntityGet(Request request, Response response,
            Entity entity) {
        Representation output = null;

        // Get variants for a resource
        boolean found = false;
        Iterator<Preference<MediaType>> iterator = request.getClientInfo()
                .getAcceptedMediaTypes().iterator();
        while (iterator.hasNext() && !found) {
            Preference<MediaType> pref = iterator.next();
            found = pref.getMetadata().equals(MediaType.TEXT_URI_LIST);
        }

        if (found) {
            // Try to list all variants of this resource
            // 1- set up base name as the longest part of the name without known
            // extensions (beginning from the left)
            String baseName = entity.getBaseName();

            // 2- looking for resources with the same base name
            Entity parent = entity.getParent();

            if (parent != null) {
                Collection<Entity> entities = parent.getChildren();

                if (entities != null) {
                    ReferenceList rl = new ReferenceList(entities.size());
                    String scheme = request.getResourceRef().getScheme();
                    String path = request.getResourceRef().getPath();
                    String encodedParentDirectoryURI = path.substring(0,
                            path.lastIndexOf("/"));
                    String encodedEntityName = path.substring(path
                            .lastIndexOf("/") + 1);

                    for (Entity entry : entities) {
                        if (baseName.equals(entry.getBaseName())) {
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
                    Collection<Entity> children = entity.getChildren();
                    ReferenceList rl = new ReferenceList(children.size());
                    String directoryUri = request.getResourceRef()
                            .getTargetRef().toString();

                    // Ensures that the directory URI ends with a slash
                    if (!directoryUri.endsWith("/")) {
                        directoryUri += "/";
                    }

                    for (Entity entry : children) {
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
                    output = entity.getRepresentation(getMetadataService()
                            .getDefaultMediaType(), getTimeToLive());
                    output.setLocationRef(request.getResourceRef());
                    Entity.updateMetadata(entity.getName(), output, true,
                            getMetadataService());
                }
            } else {
                // We look for the possible variant which has the same
                // metadata based on extensions (in a distinct order) and
                // default metadata.
                Entity uniqueVariant = null;

                // 1- set up base name as the longest part of the name without
                // known extensions (beginning from the left)
                String baseName = entity.getBaseName();
                Variant entityVariant = entity.getVariant();

                // 2- looking for resources with the same base name
                Entity parent = entity.getParent();
                if (parent != null) {
                    Collection<Entity> files = parent.getChildren();

                    if (files != null) {
                        for (Entity entry : files) {
                            if (baseName.equals(entry.getBaseName())) {
                                Variant entryVariant = entry.getVariant();

                                if (entityVariant.isCompatible(entryVariant)) {
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
                    output = uniqueVariant.getRepresentation(
                            getMetadataService().getDefaultMediaType(),
                            getTimeToLive());
                    output.setLocationRef(request.getResourceRef());
                    Entity.updateMetadata(entity.getName(), output, true,
                            getMetadataService());
                }
            }
        }

        if (output == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        } else {
            output.setLocationRef(request.getResourceRef());
            response.setEntity(output);
            response.setStatus(Status.SUCCESS_OK);
        }
    }

    @Override
    protected void handleLocal(Request request, Response response,
            String decodedPath) {
        if (Method.GET.equals(request.getMethod())
                || Method.HEAD.equals(request.getMethod())) {
            handleEntityGet(request, response, getEntity(decodedPath));
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
        }
    }
}
