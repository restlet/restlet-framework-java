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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.service.MetadataService;
import org.restlet.util.ByteUtils;

/**
 * Connector to the file resources accessible
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @author Thierry Boileau
 */
public class FileClientHelper extends LocalClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *                The client to help.
     */
    public FileClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.FILE);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    public void handle(Request request, Response response) {
        String scheme = request.getResourceRef().getScheme();

        // Ensure that all ".." and "." are normalized into the path
        // to preven unauthorized access to user directories.
        request.getResourceRef().normalize();

        if (scheme.equalsIgnoreCase("file")) {
            handleFile(request, response, request.getResourceRef().getPath());
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only FILE is supported.");
        }
    }

    /**
     * Handles a call for the FILE protocol.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @param path
     *                The file or directory path.
     */
    protected void handleFile(Request request, Response response, String path) {
        // As the path may be percent-encoded, it has to be percent-decoded.
        // Then, all generated uris must be encoded.
        String decodedPath = LocalReference
                .localizePath(Reference.decode(path));
        File file = new File(decodedPath);
        MetadataService metadataService = getMetadataService(request);

        if (request.getMethod().equals(Method.GET)
                || request.getMethod().equals(Method.HEAD)) {
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
                // 1- set up base name as the longest part of the name without
                // known extensions (beginning from the left)
                String baseName = getBaseName(file, metadataService);
                // 2- looking for resources with the same base name
                if (file.getParentFile() != null) {
                    File[] files = file.getParentFile().listFiles();
                    if (files != null) {
                        ReferenceList rl = new ReferenceList(files.length);

                        String encodedParentDirectoryURI = path.substring(0,
                                path.lastIndexOf("/"));
                        String encodedFileName = path.substring(path
                                .lastIndexOf("/") + 1);

                        for (File entry : files) {
                            if (baseName.equals(getBaseName(entry, metadataService))) {
                                rl
                                        .add(LocalReference
                                                .createFileReference(encodedParentDirectoryURI
                                                        + "/"
                                                        + getReencodedVariantFileName(
                                                                encodedFileName,
                                                                entry.getName())));
                            }
                        }
                        output = rl.getTextRepresentation();
                    }
                }
            } else {
                if ((file != null) && file.exists()) {
                    if (file.isDirectory()) {
                        // Return the directory listing
                        File[] files = file.listFiles();
                        ReferenceList rl = new ReferenceList(files.length);
                        rl.setIdentifier(request.getResourceRef());
                        String directoryUri = request.getResourceRef()
                                .toString();

                        // Ensures that the directory URI ends with a slash
                        if (!directoryUri.endsWith("/")) {
                            directoryUri += "/";
                        }

                        for (File entry : files) {
                            rl.add(directoryUri + entry.getName());
                        }

                        output = rl.getTextRepresentation();
                    } else {
                        // Return the file content
                        output = new FileRepresentation(file, metadataService
                                .getDefaultMediaType(), getTimeToLive());
                        updateMetadata(metadataService, file.getName(), output);
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
        } else if (request.getMethod().equals(Method.PUT)) {
            // Several checks : first the consistency of the metadata and the
            // filename
            if (!checkMetadataConsistency(file.getName(), metadataService,
                    request.getEntity())) {
                // ask the client to reiterate properly its request
                response.setStatus(new Status(Status.REDIRECTION_SEE_OTHER,
                        "The metadata are not consistent with the URI"));
            } else {
                // Deals with directory
                boolean isDirectory = false;
                if (file.exists()) {
                    if (file.isDirectory()) {
                        isDirectory = true;
                        response
                                .setStatus(new Status(
                                        Status.CLIENT_ERROR_FORBIDDEN,
                                        "Can't put a new representation of a directory"));
                    }
                } else {
                    // No existing file or directory found
                    if (path.endsWith("/")) {
                        isDirectory = true;
                        // Create a new directory and its necessary parents
                        if (file.mkdirs()) {
                            response.setStatus(Status.SUCCESS_NO_CONTENT);
                        } else {
                            getLogger().log(Level.WARNING,
                                    "Unable to create the new directory");
                            response.setStatus(new Status(
                                    Status.SERVER_ERROR_INTERNAL,
                                    "Unable to create the new directory"));
                        }
                    }
                }

                if (!isDirectory) {
                    // We look for the possible variants
                    // 1- set up base name as the longest part of the name
                    // without known extensions (beginning from the left)
                    String baseName = getBaseName(file, metadataService);
                    Set<String> extensions = getExtensions(file,
                            metadataService);
                    // 2- loooking for resources with the same base name
                    File[] files = file.getParentFile().listFiles();
                    File uniqueVariant = null;

                    List<File> variantsList = new ArrayList<File>();
                    if (files != null) {
                        for (File entry : files) {
                            if (entry.getName().startsWith(baseName)) {
                                Set<String> entryExtensions = getExtensions(
                                        entry, metadataService);
                                if (entryExtensions.containsAll(extensions)) {
                                    variantsList.add(entry);
                                    if (extensions.containsAll(entryExtensions)) {
                                        // The right representation has been
                                        // found.
                                        uniqueVariant = entry;
                                    }
                                }
                            }
                        }
                    }
                    if (uniqueVariant != null) {
                        file = uniqueVariant;
                    } else {
                        if (!variantsList.isEmpty()) {
                            // Negociated resource (several variants, but not
                            // the right one).
                            // Check if the request could be completed or not.
                            // The request could be more precise
                            response
                                    .setStatus(new Status(
                                            Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                                            "Unable to process properly the request. Several variants exist but none of them suits precisely."));
                        } else {
                            // This resource does not exist, yet.
                            // Complete it with the default metadata
                            updateMetadata(metadataService, file.getName(),
                                    request.getEntity());
                            if (request.getEntity().getLanguages().isEmpty()) {
                                if (metadataService.getDefaultLanguage() != null) {
                                    request.getEntity().getLanguages().add(
                                            metadataService
                                                    .getDefaultLanguage());
                                }
                            }
                            if (request.getEntity().getMediaType() == null) {
                                request.getEntity().setMediaType(
                                        metadataService.getDefaultMediaType());
                            }
                            if (request.getEntity().getEncodings().isEmpty()) {
                                if (metadataService.getDefaultEncoding() != null
                                        && !metadataService
                                                .getDefaultEncoding().equals(
                                                        Encoding.IDENTITY)) {
                                    request.getEntity().getEncodings().add(
                                            metadataService
                                                    .getDefaultEncoding());
                                }
                            }
                            // Update the URI
                            StringBuilder fileName = new StringBuilder(baseName);
                            if (metadataService.getExtension(request
                                    .getEntity().getMediaType()) != null) {
                                fileName.append("."
                                        + metadataService.getExtension(request
                                                .getEntity().getMediaType()));
                            }
                            for (Language language : request.getEntity()
                                    .getLanguages()) {
                                if (metadataService.getExtension(language) != null) {
                                    fileName.append("."
                                            + metadataService
                                                    .getExtension(language));
                                }
                            }
                            for (Encoding encoding : request.getEntity()
                                    .getEncodings()) {
                                if (metadataService.getExtension(encoding) != null) {
                                    fileName.append("."
                                            + metadataService
                                                    .getExtension(encoding));
                                }
                            }
                            file = new File(file.getParentFile(), fileName
                                    .toString());
                        }
                    }
                    // Before putting the file representation, we check that all
                    // the extensions are known
                    if (!checkExtensionsConsistency(file, metadataService)) {
                        response
                                .setStatus(new Status(
                                        Status.SERVER_ERROR_INTERNAL,
                                        "Unable to process properly the URI. At least one extension is not known by the server."));
                    } else {
                        File tmp = null;

                        if (file.exists()) {
                            FileOutputStream fos = null;
                            // Replace the content of the file
                            // First, create a temporary file
                            try {
                                tmp = File.createTempFile("restlet-upload",
                                        "bin");

                                if (request.isEntityAvailable()) {
                                    fos = new FileOutputStream(tmp);
                                    ByteUtils.write(request.getEntity()
                                            .getStream(), fos);
                                }
                            } catch (IOException ioe) {
                                getLogger().log(Level.WARNING,
                                        "Unable to create the temporary file",
                                        ioe);
                                response.setStatus(new Status(
                                        Status.SERVER_ERROR_INTERNAL,
                                        "Unable to create a temporary file"));
                            } finally {
                                try {
                                    if (fos != null)
                                        fos.close();
                                } catch (IOException ioe) {
                                    getLogger()
                                            .log(
                                                    Level.WARNING,
                                                    "Unable to close the temporary file",
                                                    ioe);
                                    response
                                            .setStatus(new Status(
                                                    Status.SERVER_ERROR_INTERNAL,
                                                    "Unable to close a temporary file"));
                                }
                            }

                            // Then delete the existing file
                            if (file.delete()) {
                                // Finally move the temporary file to the
                                // existing file location
                                boolean renameSuccessfull = false;
                                if ((tmp != null) && tmp.renameTo(file)) {
                                    if (request.getEntity() == null) {
                                        response
                                                .setStatus(Status.SUCCESS_NO_CONTENT);
                                    } else {
                                        response.setStatus(Status.SUCCESS_OK);
                                    }
                                    renameSuccessfull = true;
                                } else {
                                    // Many aspects of the behavior of the
                                    // method "renameTo" are inherently
                                    // platform-dependent: The rename operation
                                    // might not be able to move a file from one
                                    // filesystem to another.
                                    if (tmp != null && tmp.exists()) {
                                        try {
                                            BufferedReader br = new BufferedReader(
                                                    new FileReader(tmp));
                                            BufferedWriter wr = new BufferedWriter(
                                                    new FileWriter(file));
                                            String s;
                                            while ((s = br.readLine()) != null)
                                                wr.append(s);

                                            br.close();
                                            wr.flush();
                                            wr.close();
                                            renameSuccessfull = true;
                                            tmp.delete();
                                        } catch (Exception e) {
                                            renameSuccessfull = false;
                                        }
                                    }
                                    if (!renameSuccessfull) {
                                        getLogger()
                                                .log(Level.WARNING,
                                                        "Unable to move the temporary file to replace the existing file");
                                        response
                                                .setStatus(new Status(
                                                        Status.SERVER_ERROR_INTERNAL,
                                                        "Unable to move the temporary file to replace the existing file"));
                                    }
                                }
                            } else {
                                getLogger().log(Level.WARNING,
                                        "Unable to delete the existing file");
                                response.setStatus(new Status(
                                        Status.SERVER_ERROR_INTERNAL,
                                        "Unable to delete the existing file"));
                            }
                        } else {
                            File parent = file.getParentFile();
                            if ((parent != null) && !parent.exists()) {
                                // Create the parent directories then the new
                                // file
                                if (!parent.mkdirs()) {
                                    getLogger()
                                            .log(Level.WARNING,
                                                    "Unable to create the parent directory");
                                    response
                                            .setStatus(new Status(
                                                    Status.SERVER_ERROR_INTERNAL,
                                                    "Unable to create the parent directory"));
                                }
                            }
                            FileOutputStream fos = null;
                            // Create the new file
                            try {
                                if (file.createNewFile()) {
                                    if (request.getEntity() == null) {
                                        response
                                                .setStatus(Status.SUCCESS_NO_CONTENT);
                                    } else {
                                        fos = new FileOutputStream(file);
                                        ByteUtils.write(request.getEntity()
                                                .getStream(), fos);
                                        response
                                                .setStatus(Status.SUCCESS_CREATED);
                                    }
                                } else {
                                    getLogger().log(Level.WARNING,
                                            "Unable to create the new file");
                                    response.setStatus(new Status(
                                            Status.SERVER_ERROR_INTERNAL,
                                            "Unable to create the new file"));
                                }
                            } catch (FileNotFoundException fnfe) {
                                getLogger().log(Level.WARNING,
                                        "Unable to create the new file", fnfe);
                                response.setStatus(new Status(
                                        Status.SERVER_ERROR_INTERNAL,
                                        "Unable to create the new file"));
                            } catch (IOException ioe) {
                                getLogger().log(Level.WARNING,
                                        "Unable to create the new file", ioe);
                                response.setStatus(new Status(
                                        Status.SERVER_ERROR_INTERNAL,
                                        "Unable to create the new file"));
                            } finally {
                                try {
                                    if (fos != null)
                                        fos.close();
                                } catch (IOException ioe) {
                                    getLogger()
                                            .log(
                                                    Level.WARNING,
                                                    "Unable to close the temporary file",
                                                    ioe);
                                    response
                                            .setStatus(new Status(
                                                    Status.SERVER_ERROR_INTERNAL,
                                                    "Unable to close a temporary file"));
                                }
                            }
                        }
                    }
                }
            }
        } else if (request.getMethod().equals(Method.DELETE)) {
            if (file.isDirectory()) {
                if (file.listFiles().length == 0) {
                    if (file.delete()) {
                        response.setStatus(Status.SUCCESS_NO_CONTENT);
                    } else {
                        response.setStatus(new Status(
                                Status.SERVER_ERROR_INTERNAL,
                                "Couldn't delete the directory"));
                    }
                } else {
                    response.setStatus(new Status(
                            Status.CLIENT_ERROR_FORBIDDEN,
                            "Couldn't delete the non-empty directory"));
                }
            } else {
                if (file.delete()) {
                    response.setStatus(Status.SUCCESS_NO_CONTENT);
                } else {
                    response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
                            "Couldn't delete the file"));
                }
            }
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
            response.getAllowedMethods().add(Method.PUT);
            response.getAllowedMethods().add(Method.DELETE);
        }
    }

    /**
     * Returns the base name as the longest part of the name without known
     * extensions (beginning from the left)
     * 
     * @param file
     * @param metadataService
     * @return the base name of the file
     */
    private String getBaseName(File file, MetadataService metadataService) {
        String[] result = file.getName().split("\\.");
        StringBuilder baseName = new StringBuilder().append(result[0]);
        boolean extensionFound = false;
        for (int i = 1; (i < result.length) && !extensionFound; i++) {
            extensionFound = metadataService.getMetadata(result[i]) != null;
            if (!extensionFound) {
                baseName.append(".").append(result[i]);
            }
        }
        return baseName.toString();
    }

    /**
     * Returns the Set of extensions of a file
     * 
     * @param file
     * @param metadataService
     * @return
     */
    private Set<String> getExtensions(File file, MetadataService metadataService) {
        Set<String> result = new TreeSet<String>();

        String[] tokens = file.getName().split("\\.");
        boolean extensionFound = false;
        int i;
        for (i = 1; (i < tokens.length) && !extensionFound; i++) {
            extensionFound = metadataService.getMetadata(tokens[i]) != null;
        }
        if (extensionFound) {
            for (--i; (i < tokens.length); i++) {
                result.add(tokens[i]);
            }
        }

        return result;
    }

    /**
     * Checks that the URI and the representation are compatible. The whole set
     * of metadata of the representation must be included in the set of those of
     * the URI
     * 
     * @param fileName
     *                The name of the resource
     * @param metadataService
     *                metadata helper
     * @param representation
     *                the provided representation
     * @return true if the metadata of the representation are compatible with
     *         the metadata extracted from the filename
     */
    private boolean checkMetadataConsistency(String fileName,
            MetadataService metadataService, Representation representation) {
        boolean result = true;
        if (representation != null) {
            Variant var = new Variant();
            updateMetadata(metadataService, fileName, var);
            // "rep" contains the theorical correct metadata
            if (!representation.getLanguages().isEmpty()
                    && !var.getLanguages().containsAll(
                            representation.getLanguages())) {
                result = false;
            }
            if (representation.getMediaType() != null
                    && !(var.getMediaType() != null && var.getMediaType()
                            .includes(representation.getMediaType()))) {
                result = false;
            }
            if (!representation.getEncodings().isEmpty()
                    && !var.getEncodings().containsAll(
                            representation.getEncodings())) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Check that all extensions of the file correspond to a known metadata
     * 
     * @param file
     * @param metadataService
     * @param representation
     * @return
     */
    private boolean checkExtensionsConsistency(File file,
            MetadataService metadataService) {
        boolean knownExtension = true;

        Set<String> set = getExtensions(file, metadataService);
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext() && knownExtension) {
            knownExtension = metadataService.getMetadata(iterator.next()) != null;
        }

        return knownExtension;
    }

    /**
     * Percent-encodes the given percent-decoded variant name of a resource
     * whose percent-encoded name is given. Tries to match the longest common
     * part of both encoded file name and decoded variant name.
     * 
     * @param encodedFileName
     *                the percent-encoded name of the initial resource
     * @param decodedVariantFileName
     *                the percent-decoded file name of a variant of the initial
     *                resource.
     * @return the variant percent-encoded file name.
     */
    private String getReencodedVariantFileName(String encodedFileName,
            String decodedVariantFileName) {
        int i = 0;
        int j = 0;
        boolean stop = false;
        for (i = 0; i < decodedVariantFileName.length()
                && (j < encodedFileName.length()) && !stop; i++) {
            String decodedChar = decodedVariantFileName.substring(i, i + 1);
            if (decodedChar.equals(encodedFileName.substring(j, j + 1))) {
                j++;
            } else {
                if (encodedFileName.substring(j, j + 1).equals("%")) {
                    if (decodedChar.equals(Reference.decode(encodedFileName
                            .substring(j, j + 3)))) {
                        j += 3;
                    } else {
                        stop = true;
                    }
                } else {
                    if (decodedChar.equals(Reference.decode(encodedFileName
                            .substring(j, j + 1)))) {
                        j++;
                    } else {
                        stop = true;
                    }
                }
            }
        }

        if (stop) {
            return encodedFileName.substring(0, j)
                    + decodedVariantFileName.substring(i - 1);
        } else {
            if (j == encodedFileName.length()) {
                return encodedFileName.substring(0, j)
                        + decodedVariantFileName.substring(i);
            } else {
                return encodedFileName.substring(0, j);
            }
        }
    }
}
