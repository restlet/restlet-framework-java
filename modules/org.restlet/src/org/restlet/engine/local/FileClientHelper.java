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

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.logging.Level.WARNING;
import static org.restlet.data.Method.DELETE;
import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.HEAD;
import static org.restlet.data.Method.PUT;
import static org.restlet.data.Protocol.FILE;
import static org.restlet.data.Range.isBytesRange;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.restlet.data.Status.CLIENT_ERROR_FORBIDDEN;
import static org.restlet.data.Status.CLIENT_ERROR_METHOD_NOT_ALLOWED;
import static org.restlet.data.Status.CLIENT_ERROR_NOT_ACCEPTABLE;
import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;
import static org.restlet.data.Status.SUCCESS_CREATED;
import static org.restlet.data.Status.SUCCESS_NO_CONTENT;
import static org.restlet.data.Status.SUCCESS_OK;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

/**
 * Connector to the file resources accessible. Here is the list of parameters
 * that are supported. They should be set in the Client's context before it is
 * started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>temporaryExtension</td>
 * <td>String</td>
 * <td>tmp</td>
 * <td>The name of the extension to use to store the temporary content while uploading content via the PUT method.</td>
 * </tr>
 * <tr>
 * <td>resumeUpload</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if a failed upload can be resumed. This will prevent the deletion of the temporary file created.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public class FileClientHelper extends EntityClientHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public FileClientHelper(Client client) {
        super(client);
        getProtocols().add(FILE);
    }

    /**
     * Check that all extensions of the file correspond to a known metadata.
     * 
     * @param file
     *            The file whose extensions are checked.
     * @return True if all extensions of the file are known by the metadata
     *         service.
     */
    protected boolean checkExtensionsConsistency(File file) {
        boolean knownExtension = true;

        Collection<String> set = Entity.getExtensions(file.getName(), getMetadataService());
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext() && knownExtension) {
            knownExtension = getMetadataService().getMetadata(iterator.next()) != null;
        }

        return knownExtension;
    }

    /**
     * Checks that the URI and the representation are compatible. The whole set
     * of metadata of the representation must be included in the set of those of
     * the URI
     * 
     * @param fileName
     *            The name of the resource
     * @param representation
     *            The provided representation.
     * @return True if the metadata of the representation are compatible with
     *         the metadata extracted from the filename
     */
    private boolean checkMetadataConsistency(String fileName, Representation representation) {
        if (representation != null) {
            Variant var = new Variant();
            Entity.updateMetadata(fileName, var, false, getMetadataService());

            // "var" contains the theoretical correct metadata
            if (!var.getLanguages().isEmpty()
                    && !representation.getLanguages().isEmpty()
                    && !var.getLanguages().containsAll(representation.getLanguages())) {
                return false;
            }

            if ((var.getMediaType() != null)
                    && (representation.getMediaType() != null)
                    && !(var.getMediaType().includes(representation.getMediaType()))) {
                return false;
            }

            if (!var.getEncodings().isEmpty()
                    && !representation.getEncodings().isEmpty()
                    && !var.getEncodings().containsAll(representation.getEncodings())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Entity getEntity(String decodedPath) {
        // Take care of the file separator.
        return new FileEntity(
                new File(LocalReference.localizePath(decodedPath)),
                getMetadataService());
    }

    /**
     * Returns the name of the extension to use to store the temporary content
     * while uploading content via the PUT method. Defaults to "tmp".
     * 
     * @return The name of the extension to use to store the temporary content.
     */
    public String getTemporaryExtension() {
        return getHelpedParameters().getFirstValue("temporaryExtension", "tmp");
    }

    @Override
    protected void handleLocal(Request request, Response response,
            String decodedPath) {
        String scheme = request.getResourceRef().getScheme();

        if (!FILE.getSchemeName().equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(format(
                    "Protocol \"%s\" not supported by the connector. Only FILE is supported.", scheme));
        }

        handleFile(request, response, decodedPath);
    }

    protected void handleFile(Request request, Response response, String decodedPath) {
        if (GET.equals(request.getMethod())
                || HEAD.equals(request.getMethod())) {
            handleEntityGet(request, response, getEntity(decodedPath));
        } else if (PUT.equals(request.getMethod())) {
            handleFilePut(request, response, decodedPath, new File(decodedPath));
        } else if (DELETE.equals(request.getMethod())) {
            handleFileDelete(response, new File(decodedPath));
        } else {
            response.setStatus(CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(GET);
            response.getAllowedMethods().add(HEAD);
            response.getAllowedMethods().add(PUT);
            response.getAllowedMethods().add(DELETE);
        }
    }

    /**
     * Handles a DELETE call for the FILE protocol.
     * 
     * @param response
     *            The response to update.
     * @param file
     *            The file or directory to delete.
     */
    protected void handleFileDelete(Response response, File file) {
        if (file.isDirectory()) {
            if (file.listFiles().length == 0) {
                if (IoUtils.delete(file)) {
                    response.setStatus(SUCCESS_NO_CONTENT);
                } else {
                    response.setStatus(SERVER_ERROR_INTERNAL, "Couldn't delete the directory");
                }
            } else {
                response.setStatus(CLIENT_ERROR_FORBIDDEN, "Couldn't delete the non-empty directory");
            }
        } else {
            if (IoUtils.delete(file)) {
                response.setStatus(SUCCESS_NO_CONTENT);
            } else {
                response.setStatus(SERVER_ERROR_INTERNAL, "Couldn't delete the file");
            }
        }
    }

    /**
     * Handles a PUT call for the FILE protocol.
     * 
     * @param request
     *            The request to update.
     * @param response
     *            The response to update.
     * @param path
     *            The encoded path of the requested file or directory.
     * @param file
     *            The requested file or directory.
     */
    protected void handleFilePut(Request request, Response response, String path, File file) {
        response.setStatus(doHandleFilePut(request, path, file));
    }

    private Status doHandleFilePut(Request request, String path, File file) {
        // Handle directory
        if (file.exists()) {
            if (file.isDirectory()) {
                return new Status(CLIENT_ERROR_FORBIDDEN, "Can't put a new representation of a directory");
            }
        } else if (path.endsWith("/")) {
            // It seems the request targets a directory
            // Create a new directory and its parents if necessary
            if (file.mkdirs()) {
                return SUCCESS_NO_CONTENT;
            } else {
                getLogger().warning("Unable to create the new directory");
                return new Status(SERVER_ERROR_INTERNAL, "Unable to create the new directory");
            }
        }

        // Several checks : first the consistency of the metadata and the filename
        if (!checkMetadataConsistency(file.getName(), request.getEntity())) {
            // Ask the client to reiterate properly its request
            return new Status(CLIENT_ERROR_BAD_REQUEST, "The metadata are not consistent with the URI");
        }

        // We look for the possible variants
        // Set up base name as the longest part of the name without known extensions (beginning from the left)
        final String baseName = Entity.getBaseName(file.getName(), getMetadataService());

        // Look for resources with the same base name
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile()
                        && baseName.equals(Entity.getBaseName(file.getName(), getMetadataService()));
            }
        };

        File[] files = file.getParentFile().listFiles(filter);
        File uniqueVariant = null;
        List<File> variantsList = new ArrayList<File>();

        if (files != null && files.length > 0) {
            // Set the list of extensions, due to the file name and the
            // default metadata.
            // TODO It seems we could handle more clearly the equivalence
            // between the file name space and the target resource (URI
            // completed by default metadata)
            Variant variant = new Variant();
            Entity.updateMetadata(file.getName(), variant, false, getMetadataService());
            Collection<String> extensions = Entity.getExtensions(variant, getMetadataService());

            for (File entry : files) {
                Collection<String> entryExtensions = Entity.getExtensions(entry.getName(), getMetadataService());

                if (entryExtensions.containsAll(extensions)) {
                    variantsList.add(entry);

                    if (extensions.containsAll(entryExtensions)) {
                        // The right representation has been found.
                        uniqueVariant = entry;
                    }
                }
            }
        }

        if (uniqueVariant != null) {
            file = uniqueVariant;
        } else {
            if (!variantsList.isEmpty()) {
                // Negotiated resource (several variants, but not the right one).
                // Check if the request could be completed or not.
                // The request could be more precise
                return new Status(CLIENT_ERROR_NOT_ACCEPTABLE,
                        "Unable to process properly the request. Several variants exist but none of them suits precisely.");
            }

            // This resource does not exist, yet. Complete it with the
            // default metadata
            Entity.updateMetadata(file.getName(), request.getEntity(), true, getMetadataService());

            // Update the URI
            StringBuilder fileName = new StringBuilder(baseName);

            for (Language language : request.getEntity().getLanguages()) {
                updateFileExtension(fileName, language);
            }

            for (Encoding encoding : request.getEntity().getEncodings()) {
                updateFileExtension(fileName, encoding);
            }

            // It is important to finish with the media type as it is
            // often leveraged by operating systems to detect file type
            updateFileExtension(fileName, request.getEntity().getMediaType());

            file = new File(file.getParentFile(), fileName.toString());
        }

        // Before putting the file representation, we check that all the extensions are known
        if (!checkExtensionsConsistency(file)) {
            return new Status(SERVER_ERROR_INTERNAL,
                    "Unable to process properly the URI. At least one extension is not known by the server.");
        }

        // This helper supports only single "bytes" range.
        Range range = (!request.getRanges().isEmpty()
                && isBytesRange(request.getRanges().get(0))) ? request.getRanges().get(0) : null;

        if (file.exists()) {
            // The PUT call is handled in two phases:
            // 1- write a temporary file
            // 2- rename the target file
            if (range != null) {
                return updateFileWithPartialContent(request, file, range);
            }
            return replaceFile(request, file);
        } else {
            // The file does not exist yet.
            File parent = file.getParentFile();

            if ((parent != null) && !parent.exists()) {
                // Create the parent directories then the new file
                if (!parent.mkdirs()) {
                    String message = "Unable to create the parent directory";
                    getLogger().warning(message);
                    return new Status(SERVER_ERROR_INTERNAL, message);
                }
            }

            // Create the new file
            if (range != null) {
                return createFileWithPartialContent(request, file, range);
            }
            return createFile(request, file);
        }
    }

    private Status updateFileWithPartialContent(Request request, File file, Range range) {
        File tmp = null;

        // Replace the content of the file. First, create a temporary file
        try {
            // The temporary file used for partial PUT.
            tmp = new File(file.getCanonicalPath() + "." + getTemporaryExtension());

            cleanTemporaryFileIfUploadNotResumed(tmp);

            if (!tmp.exists()) {
                // Copy the target file.
                Files.copy(file.toPath(), tmp.toPath());
            }

            try (RandomAccessFile raf = new RandomAccessFile(tmp, "rwd")) {
                // Go to the desired offset.
                if (range.getIndex() == Range.INDEX_LAST) {
                    if (raf.length() <= range.getSize()) {
                        raf.seek(range.getSize());
                    } else {
                        raf.seek(raf.length() - range.getSize());
                    }
                } else {
                    raf.seek(range.getIndex());
                }

                // Write the entity to the temporary file.
                if (request.isEntityAvailable()) {
                    IoUtils.copy(request.getEntity().getStream(), raf);
                }
            } catch (IOException ioe) {
                getLogger().log(WARNING, "Unable to close the temporary file", ioe);
                cleanTemporaryFileIfUploadNotResumed(tmp);
                return new Status(SERVER_ERROR_INTERNAL, ioe);
            }

            return replaceFileByTemporaryFile(request, file, tmp);
        } catch (IOException ioe) {
            getLogger().log(WARNING, "Unable to create the temporary file", ioe);
            cleanTemporaryFileIfUploadNotResumed(tmp);
            return new Status(SERVER_ERROR_INTERNAL, "Unable to create a temporary file");
        }
    }

    private Status replaceFile(Request request, File file) {
        File tmp = null;
        try {
            tmp = File.createTempFile("restlet-upload", "bin");
            if (request.isEntityAvailable()) {
                Files.copy(request.getEntity().getStream(), tmp.toPath(), REPLACE_EXISTING);
            }
        } catch (IOException ioe) {
            getLogger().log(WARNING, "Unable to create the temporary file", ioe);
            cleanTemporaryFileIfUploadNotResumed(tmp);
            return new Status(SERVER_ERROR_INTERNAL, "Unable to create a temporary file");
        }
        return replaceFileByTemporaryFile(request, file, tmp);
    }

    private Status replaceFileByTemporaryFile(Request request, File file, File tmp) {
        if (!tmp.exists()) {
            return new Status(SERVER_ERROR_INTERNAL, "Can't replace the existing file without new content.");
        }

        // Then delete the existing file
        if (!IoUtils.delete(file)) {
            cleanTemporaryFileIfUploadNotResumed(tmp);
            return new Status(SERVER_ERROR_INTERNAL, "Unable to delete the existing file");
        }

        // Finally move the temporary file to the existing file location
        if (tmp.renameTo(file)) {
            if (request.isEntityAvailable()) {
                return SUCCESS_NO_CONTENT;
            }
            return SUCCESS_OK;
        }

        // Many aspects of the behavior of the method "renameTo" are inherently platform-dependent.
        // The rename operation might not be able to move a file from one file system to another.
        if (!tmp.exists()) {
            return new Status(SERVER_ERROR_INTERNAL, "Unable to move the temporary file to replace the existing file");
        }
        try {
            Files.move(tmp.toPath(), file.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            return new Status(SERVER_ERROR_INTERNAL, e,
                    "Unable to move the temporary file to replace the existing file");
        }
        return SUCCESS_OK;
    }

    private Status createFileWithPartialContent(Request request, File file, Range range) {
        // This is a partial PUT
        try (RandomAccessFile raf = new RandomAccessFile(file, "rwd")) {
            // Go to the desired offset.
            if (range.getIndex() == Range.INDEX_LAST) {
                if (raf.length() <= range.getSize()) {
                    raf.seek(range.getSize());
                } else {
                    raf.seek(raf.length() - range.getSize());
                }
            } else {
                raf.seek(range.getIndex());
            }
            // Write the entity to the file.
            if (request.isEntityAvailable()) {
                IoUtils.copy(request.getEntity().getStream(), raf);
                return SUCCESS_CREATED;
            }
            return SUCCESS_NO_CONTENT;
        } catch (FileNotFoundException fnfe) {
            getLogger().log(WARNING, "Unable to create the new file", fnfe);
            return new Status(SERVER_ERROR_INTERNAL, fnfe);
        } catch (IOException ioe) {
            getLogger().log(WARNING, "Unable to create the new file", ioe);
            return new Status(SERVER_ERROR_INTERNAL, ioe);
        }
    }

    private Status createFile(Request request, File file) {
        // This is simple PUT of the full entity
        try {
            if (request.isEntityAvailable()) {
                Files.copy(request.getEntity().getStream(), file.toPath());
                return SUCCESS_CREATED;
            }
            if (file.createNewFile()) {
                return SUCCESS_NO_CONTENT;
            }
        } catch (IOException ioe) {
            getLogger().log(WARNING, "Unable to create the new file", ioe);
            return new Status(SERVER_ERROR_INTERNAL, ioe);
        }

        String message = "Unable to create the new file";
        getLogger().warning(message);
        return new Status(SERVER_ERROR_INTERNAL, message);
    }
    
    private void cleanTemporaryFileIfUploadNotResumed(File tmp) {
        if (tmp.exists() && !isResumeUpload()) {
            IoUtils.delete(tmp);
        }
    }

    /**
     * Indicates if a failed upload can be resumed. This will prevent the
     * deletion of the temporary file created. Defaults to "false".
     * 
     * @return True if a failed upload can be resumed, false otherwise.
     */
    public boolean isResumeUpload() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "resumeUpload", "false"));
    }

    /**
     * Complete the given file name with the extension corresponding to the
     * given metadata.
     * 
     * @param fileName
     *            The file name to complete.
     * @param metadata
     *            The metadata.
     */
    private void updateFileExtension(StringBuilder fileName, Metadata metadata) {
        boolean defaultMetadata = true;

        if (getMetadataService() != null) {
            if (metadata instanceof Language) {
                Language language = (Language) metadata;
                defaultMetadata = language.equals(getMetadataService()
                        .getDefaultLanguage());
            } else if (metadata instanceof MediaType) {
                MediaType mediaType = (MediaType) metadata;
                defaultMetadata = mediaType.equals(getMetadataService()
                        .getDefaultMediaType());
            } else if (metadata instanceof CharacterSet) {
                CharacterSet characterSet = (CharacterSet) metadata;
                defaultMetadata = characterSet.equals(getMetadataService()
                        .getDefaultCharacterSet());
            } else if (metadata instanceof Encoding) {
                Encoding encoding = (Encoding) metadata;
                defaultMetadata = encoding.equals(getMetadataService()
                        .getDefaultEncoding());
            }
        }

        // We only add extension for metadata that differs from default ones
        if (!defaultMetadata) {
            String extension = getMetadataService().getExtension(metadata);

            if (extension != null) {
                fileName.append("." + extension);
            } else {
                if (metadata.getParent() != null) {
                    updateFileExtension(fileName, metadata.getParent());
                }
            }
        }
    }
}
