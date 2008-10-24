package com.noelios.restlet.local;

import java.util.Collection;
import java.util.Iterator;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.service.MetadataService;

/**
 * Connector to the file resources accessible. That connector supports the
 * content negotiation feature (i.e. for GET and HEAD methods) and implements
 * the response to GET/HEAD methods.
 * 
 */
public abstract class LocalFileClientHelper extends LocalClientHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public LocalFileClientHelper(Client client) {
        super(client);
    }

    /**
     * Generate a Reference for a variant name (which is URL decoded) and handle
     * the translation between the incoming requested path (which is URL
     * encoded).
     * 
     * @param encodedParentDirPath
     *            The encoded path of the parent dir of the requested resource.
     * @param encodedFileName
     *            The encoded name of the requested resource.
     * @param decodedVariantName
     *            The decoded name of a returned resource.
     * @param scheme
     *            The scheme of the requested resource.
     * @return A new Reference.
     */
    public Reference createReference(String encodedParentDirPath,
            String encodedFileName, String decodedVariantName, String scheme) {
        Reference result = new Reference(scheme
                + "://"
                + encodedParentDirPath
                + "/"
                + getReencodedVariantFileName(encodedFileName,
                        decodedVariantName));
        return result;
    }

    /**
     * Returns an instance of LocalFile according to a given path.
     * 
     * @param path
     *            The path of the file.
     * @return An instance of LocalFile according to the given path.
     */
    public abstract LocalFile getLocalFile(String path);

    /**
     * Percent-encodes the given percent-decoded variant name of a resource
     * whose percent-encoded name is given. Tries to match the longest common
     * part of both encoded file name and decoded variant name.
     * 
     * @param encodedFileName
     *            the percent-encoded name of the initial resource
     * @param decodedVariantFileName
     *            the percent-decoded file name of a variant of the initial
     *            resource.
     * @return the variant percent-encoded file name.
     */
    protected String getReencodedVariantFileName(String encodedFileName,
            String decodedVariantFileName) {
        int i = 0;
        int j = 0;
        boolean stop = false;
        char[] encodeds = encodedFileName.toCharArray();
        char[] decodeds = decodedVariantFileName.toCharArray();

        for (i = 0; (i < decodeds.length) && (j < encodeds.length) && !stop; i++) {
            char decodedChar = decodeds[i];
            char encodedChar = encodeds[j];

            if (encodedChar == '%') {
                String dec = Reference.decode(encodedFileName.substring(j,
                        j + 3));
                if (decodedChar == dec.charAt(0)) {
                    j += 3;
                } else {
                    stop = true;
                }
            } else if (decodedChar == decodedChar) {
                j++;
            } else {
                String dec = Reference.decode(encodedFileName.substring(j,
                        j + 1));
                if (decodedChar == dec.charAt(0)) {
                    j++;
                } else {
                    stop = true;
                }
            }
        }

        if (stop) {
            return encodedFileName.substring(0, j)
                    + decodedVariantFileName.substring(i - 1);
        }

        if (j == encodedFileName.length()) {
            return encodedFileName.substring(0, j)
                    + decodedVariantFileName.substring(i);
        }

        return encodedFileName.substring(0, j);
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
        // to preven unauthorized access to user directories.
        request.getResourceRef().normalize();
        String path = request.getResourceRef().getPath();
        // As the path may be percent-encoded, it has to be percent-decoded.
        // Then, all generated uris must be encoded.
        final String decodedPath = LocalReference.localizePath(Reference
                .decode(path));
        final MetadataService metadataService = getMetadataService(request);

        handleFile(request, response, path, decodedPath, metadataService);
    }

    /**
     * Handles a call for a local file. By default, only GET and HEAD methods
     * are implemented.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param path
     *            The file or directory path.
     * @param decodedPath
     *            The URL decoded file or directory path.
     * @param metadataService
     *            The metadataService.
     */
    protected void handleFile(Request request, Response response, String path,
            final String decodedPath, final MetadataService metadataService) {
        if (Method.GET.equals(request.getMethod())
                || Method.HEAD.equals(request.getMethod())) {
            handleFileGet(request, response, path, getLocalFile(decodedPath),
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
     *            The encoded path of the requested file or directory.
     * @param localFile
     *            The requested file or directory.
     * @param metadataService
     *            The metadata service.
     */
    protected void handleFileGet(Request request, Response response,
            String path, LocalFile localFile,
            final MetadataService metadataService) {
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
            final String baseName = localFile.getBaseName(metadataService);
            // 2- looking for resources with the same base name
            LocalFile parent = localFile.getParent();
            if (parent != null) {
                final Collection<LocalFile> files = parent.getFiles();
                if (files != null) {
                    final ReferenceList rl = new ReferenceList(files.size());

                    final String scheme = request.getResourceRef().getScheme();
                    final String encodedParentDirectoryURI = path.substring(0,
                            path.lastIndexOf("/"));
                    final String encodedFileName = path.substring(path
                            .lastIndexOf("/") + 1);

                    for (final LocalFile entry : files) {
                        if (baseName.equals(entry.getBaseName(metadataService))) {
                            rl.add(createReference(encodedParentDirectoryURI,
                                    encodedFileName, entry.getName(), scheme));
                        }
                    }
                    output = rl.getTextRepresentation();
                }
            }
        } else {
            if (localFile.exists()) {
                if (localFile.isDirectory()) {
                    // Return the directory listing
                    final Collection<LocalFile> files = localFile.getFiles();
                    final ReferenceList rl = new ReferenceList(files.size());
                    String directoryUri = request.getResourceRef().toString();

                    // Ensures that the directory URI ends with a slash
                    if (!directoryUri.endsWith("/")) {
                        directoryUri += "/";
                    }

                    for (final LocalFile entry : files) {
                        rl
                                .add(directoryUri
                                        + Reference.encode(entry.getName()));
                    }

                    output = rl.getTextRepresentation();
                } else {
                    // Return the file content
                    output = localFile.getRepresentation(metadataService
                            .getDefaultMediaType(), getTimeToLive());
                    output.setIdentifier(request.getResourceRef());
                    updateMetadata(metadataService, localFile.getName(), output);
                }
            } else {
                // We look for the possible variant which has the same
                // extensions in a distinct order.
                // 1- set up base name as the longest part of the name without
                // known extensions (beginning from the left)
                final String baseName = localFile.getBaseName(metadataService);
                final Collection<String> extensions = localFile
                        .getExtensions(metadataService);
                // 2- loooking for resources with the same base name
                final Collection<LocalFile> files = localFile.getFiles();
                LocalFile uniqueVariant = null;

                if (files != null) {
                    for (final LocalFile entry : files) {
                        if (baseName.equals(entry.getBaseName(metadataService))) {
                            final Collection<String> entryExtensions = entry
                                    .getExtensions(metadataService);
                            if (entryExtensions.containsAll(extensions)
                                    && extensions.containsAll(entryExtensions)) {
                                // The right representation has been found.
                                uniqueVariant = entry;
                                break;
                            }
                        }
                    }
                }
                if (uniqueVariant != null) {
                    // Return the file content
                    output = uniqueVariant.getRepresentation(metadataService
                            .getDefaultMediaType(), getTimeToLive());
                    output.setIdentifier(request.getResourceRef());
                    updateMetadata(metadataService, localFile.getName(), output);
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
