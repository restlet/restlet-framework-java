/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
 * Connector to the local entities. That connector supports the content negotiation feature (i.e.,
 * for GET and HEAD methods) and implements the response to GET/HEAD methods.
 *
 * @author Thierry Boileau
 */
public abstract class EntityClientHelper extends LocalClientHelper {

    /**
     * Constructor.
     *
     * @param client The client to help.
     */
    protected EntityClientHelper(Client client) {
        super(client);
    }

    /**
     * Generate a Reference for a variant name (which is URL decoded) and handle the translation
     * between the incoming requested path (which is URL encoded).
     *
     * @param scheme The scheme of the requested resource.
     * @param encodedParentDirPath The encoded path of the parent directory of the requested
     *     resource.
     * @param encodedEntityName The encoded name of the requested resource.
     * @param decodedVariantName The decoded name of a returned resource.
     * @return A new Reference.
     */
    public Reference createReference(
            String scheme,
            String encodedParentDirPath,
            String encodedEntityName,
            String decodedVariantName) {
        return new Reference(
                scheme
                        + "://"
                        + encodedParentDirPath
                        + "/"
                        + getReencodedVariantEntityName(encodedEntityName, decodedVariantName));
    }

    /**
     * Returns a local entity for the given path.
     *
     * @param path The path of the entity.
     * @return A local entity for the given path.
     */
    public abstract Entity getEntity(String path);

    /**
     * Percent-encodes the given percent-decoded variant name of a resource whose percent-encoded
     * name is given. Tries to match the longest common part of both encoded entity name and decoded
     * variant name.
     *
     * @param encodedEntityName the percent-encoded name of the initial resource
     * @param decodedVariantEntityName the percent-decoded entity name of a variant of the initial
     *     resource.
     * @return The variant percent-encoded entity name.
     */
    protected String getReencodedVariantEntityName(
            String encodedEntityName, String decodedVariantEntityName) {
        int i;
        int j = 0;

        char[] encodedChars = encodedEntityName.toCharArray();
        char[] decodedChars = decodedVariantEntityName.toCharArray();

        for (i = 0; (i < decodedChars.length) && (j < encodedChars.length); i++) {
            char decodedChar = decodedChars[i];
            char encodedChar = encodedChars[j];

            if (encodedChar == '%') {
                String dec = Reference.decode(encodedEntityName.substring(j, j + 3));
                if (decodedChar == dec.charAt(0)) {
                    j += 3;
                } else {
                    return encodedEntityName.substring(0, j)
                            + decodedVariantEntityName.substring(i);
                }
            } else if (decodedChar == encodedChar) {
                j++;
            } else {
                String dec = Reference.decode(encodedEntityName.substring(j, j + 1));
                if (decodedChar == dec.charAt(0)) {
                    j++;
                } else {
                    return encodedEntityName.substring(0, j)
                            + decodedVariantEntityName.substring(i);
                }
            }
        }

        if (j == encodedEntityName.length()) {
            return encodedEntityName.substring(0, j) + decodedVariantEntityName.substring(i);
        }

        return encodedEntityName.substring(0, j);
    }

    /**
     * Handles a GET call.
     *
     * @param request The request to answer.
     * @param response The response to modify.
     * @param entity The requested entity (normal or directory).
     */
    protected void handleEntityGet(Request request, Response response, Entity entity) {
        Representation result;

        // Get variants for a resource
        boolean found = false;
        Iterator<Preference<MediaType>> iterator =
                request.getClientInfo().getAcceptedMediaTypes().iterator();
        while (iterator.hasNext() && !found) {
            Preference<MediaType> pref = iterator.next();
            found = pref.getMetadata().equals(MediaType.TEXT_URI_LIST);
        }

        if (found) {
            result = getAllVariantsRepresentation(request, entity);
        } else {
            if (entity.exists()) {
                if (entity.isDirectory()) {
                    // Return the directory listing
                    result = getDirectoryRepresentation(request, entity);
                } else {
                    // Return the file content
                    result =
                            entity.getRepresentation(
                                    getMetadataService().getDefaultMediaType(), getTimeToLive());
                    result.setLocationRef(request.getResourceRef());
                    Entity.updateMetadata(entity.getName(), result, true, getMetadataService());
                }
            } else {
                // Look for the same variant
                result = getUniqueVariantRepresentation(request, entity);
            }
        }

        if (result == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        } else {
            result.setLocationRef(request.getResourceRef());
            response.setEntity(result);
            response.setStatus(Status.SUCCESS_OK);
        }
    }

    /**
     * We look for the possible variant which has the same metadata based on extensions (in a
     * distinct order) and default metadata.
     */
    private Representation getUniqueVariantRepresentation(
            final Request request, final Entity entity) {

        Entity uniqueVariant = null;

        // 1- set up the base name as the longest part of the name without known extensions
        // (beginning from the left)
        String baseName = entity.getBaseName();
        Variant entityVariant = entity.getVariant();

        // 2- looking for resources with the same base name
        Entity parent = entity.getParent();

        if (parent == null) {
            return null;
        }

        Collection<Entity> files = parent.getChildren();

        if (files == null) {
            return null;
        }

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

        if (uniqueVariant == null) {
            return null;
        }

        // Return the file content
        final MediaType defaultMediaType = getMetadataService().getDefaultMediaType();
        Representation result = uniqueVariant.getRepresentation(defaultMediaType, getTimeToLive());
        result.setLocationRef(request.getResourceRef());
        Entity.updateMetadata(entity.getName(), result, true, getMetadataService());
        return result;
    }

    /** Returns a representation of the listing of the given directory. */
    private static Representation getDirectoryRepresentation(
            final Request request, final Entity directory) {
        Collection<Entity> children = directory.getChildren();
        ReferenceList rl = new ReferenceList(children.size());
        String directoryUri = request.getResourceRef().getTargetRef().toString();

        // Ensures that the directory URI ends with a slash
        if (!directoryUri.endsWith("/")) {
            directoryUri += "/";
        }

        for (Entity entry : children) {
            if (entry.isDirectory()) {
                rl.add(directoryUri + Reference.encode(entry.getName()) + "/");
            } else {
                rl.add(directoryUri + Reference.encode(entry.getName()));
            }
        }
        return rl.getTextRepresentation();
    }

    /** Returns a representation of all variants of the given entity. */
    private Representation getAllVariantsRepresentation(
            final Request request, final Entity entity) {
        Representation result = null;

        // 1- set up the base name as the longest part of the name without known
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
                String encodedParentDirectoryURI = path.substring(0, path.lastIndexOf('/'));
                String encodedEntityName = path.substring(path.lastIndexOf('/') + 1);

                for (Entity entry : entities) {
                    if (baseName.equals(entry.getBaseName())) {
                        rl.add(
                                createReference(
                                        scheme,
                                        encodedParentDirectoryURI,
                                        encodedEntityName,
                                        entry.getName()));
                    }
                }

                result = rl.getTextRepresentation();
            }
        }
        return result;
    }

    @Override
    protected void handleLocal(Request request, Response response, String decodedPath) {
        if (Method.GET.equals(request.getMethod()) || Method.HEAD.equals(request.getMethod())) {
            handleEntityGet(request, response, getEntity(decodedPath));
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
        }
    }
}
