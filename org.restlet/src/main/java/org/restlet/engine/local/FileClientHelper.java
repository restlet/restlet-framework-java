/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.restlet.resource.Directory;

/**
 * Connector to the file resources accessible. Here is the list of parameters that are supported.
 * They should be set in the Client's context before it is started:
 *
 * <table>
 * <caption>list of supported parameters</caption>
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
 * <td>The name of the extension to use to store the temporary content while
 * uploading content via the PUT method.</td>
 * </tr>
 * <tr>
 * <td>resumeUpload</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if a failed upload can be resumed. This will prevent the
 * deletion of the temporary file created.</td>
 * </tr>
 * </table>
 *
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public class FileClientHelper extends EntityClientHelper {

    private static final String ERROR_MESSAGE_UNABLE_FILE_CREATION =
            "Unable to create the new file";

    /**
     * Constructor.
     *
     * @param client The client to help.
     */
    public FileClientHelper(Client client) {
        super(client);
        getProtocols().add(FILE);
    }

    /**
     * Check that all extensions of the file correspond to a known metadata.
     *
     * @param file The file whose extensions are checked.
     * @return True if the metadata service knows all extensions of the file.
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
     * Checks that the URI and the representation are compatible. The whole set of metadata of the
     * representation must be included in the set of those of the URI
     *
     * @param fileName The name of the resource
     * @param representation The provided representation.
     * @return True if the metadata of the representation is compatible with the metadata extracted
     *     from the filename
     */
    private boolean checkMetadataConsistency(String fileName, Representation representation) {
        if (representation != null) {
            Variant variant = new Variant();
            Entity.updateMetadata(fileName, variant, false, getMetadataService());

            // "variant" contains the theoretical correct metadata
            if (!variant.getLanguages().isEmpty()
                    && !representation.getLanguages().isEmpty()
                    && !new HashSet<>(variant.getLanguages())
                            .containsAll(representation.getLanguages())) {
                return false;
            }

            if ((variant.getMediaType() != null)
                    && (representation.getMediaType() != null)
                    && !(variant.getMediaType().includes(representation.getMediaType()))) {
                return false;
            }

            return variant.getEncodings().isEmpty()
                    || representation.getEncodings().isEmpty()
                    || new HashSet<>(variant.getEncodings())
                            .containsAll(representation.getEncodings());
        }
        return true;
    }

    @Override
    public Entity getEntity(String decodedPath) {
        return new FileEntity(getFileWithLocalizedPath(decodedPath), getMetadataService());
    }

    /**
     * Returns a new {@link File} instance for the given path name.<br>
     * It ensures to translate the "/" to the local supported file separator (mainly useful for
     * Windows OS).
     *
     * @param path The Path of the file
     * @return a new {@link File} instance for the given path name.
     */
    private static File getFileWithLocalizedPath(final String path) {
        return new File(LocalReference.localizePath(path));
    }

    /**
     * Returns the name of the extension to use to store the temporary content while uploading
     * content via the PUT method. Defaults to "tmp".
     *
     * @return The name of the extension to use to store the temporary content.
     */
    public String getTemporaryExtension() {
        return getHelpedParameters().getFirstValue("temporaryExtension", "tmp");
    }

    @Override
    protected void handleLocal(Request request, Response response, String decodedPath) {
        String scheme = request.getResourceRef().getScheme();

        if (!FILE.getSchemeName().equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(
                    format(
                            "Protocol \"%s\" not supported by the connector. Only FILE is supported.",
                            scheme));
        }

        handleFile(request, response, decodedPath);
    }

    protected void handleFile(Request request, Response response, String decodedPath) {
        final Directory directory =
                (Directory) request.getAttributes().get("org.restlet.directory");
        final File fileWithLocalizedPath = getFileWithLocalizedPath(decodedPath);

        if (!isFileInDirectory(directory, fileWithLocalizedPath)) {
            response.setStatus(CLIENT_ERROR_FORBIDDEN);
        } else if (GET.equals(request.getMethod()) || HEAD.equals(request.getMethod())) {
            handleEntityGet(request, response, getEntity(decodedPath));
        } else if (PUT.equals(request.getMethod())) {
            handleFilePut(request, response, decodedPath, fileWithLocalizedPath);
        } else if (DELETE.equals(request.getMethod())) {
            handleFileDelete(response, fileWithLocalizedPath);
        } else {
            response.setStatus(CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(GET);
            response.getAllowedMethods().add(HEAD);
            response.getAllowedMethods().add(PUT);
            response.getAllowedMethods().add(DELETE);
        }
    }

    /**
     * Indicates whether the given file is located inside the root directory.
     *
     * @param directory The root directory
     * @param file The file.
     * @return True if the path is located under the root directory, false otherwise.
     */
    private static boolean isFileInDirectory(final Directory directory, final File file) {
        boolean result = true;

        if (directory != null) {
            final String fileAbsolute = directory.getRootRef().getPath(true);
            final String filePath;

            if (fileAbsolute.indexOf(':') == 2 || fileAbsolute.indexOf('|') == 2) {
                filePath = fileAbsolute.substring(1);
            } else {
                filePath = fileAbsolute;
            }

            final Path rootDirectoryPath = Paths.get(filePath).normalize();
            final Path actualFilePath = file.toPath().normalize();
            result = !rootDirectoryPath.relativize(actualFilePath).toString().startsWith("..");
        }

        return result;
    }

    /**
     * Handles a DELETE call for the FILE protocol.
     *
     * @param response The response to modify.
     * @param file The file or directory to delete.
     */
    protected void handleFileDelete(Response response, File file) {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                if (IoUtils.delete(file)) {
                    response.setStatus(SUCCESS_NO_CONTENT);
                } else {
                    response.setStatus(SERVER_ERROR_INTERNAL, "Couldn't delete the directory");
                }
            } else {
                response.setStatus(
                        CLIENT_ERROR_FORBIDDEN, "Couldn't delete the non-empty directory");
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
     * @param request The request to update.
     * @param response The response to modify.
     * @param path The encoded path of the requested file or directory.
     * @param file The requested file or directory.
     */
    protected void handleFilePut(Request request, Response response, String path, File file) {
        // Handle directory
        Status handleDirectoryPutStatus = doHandleDirectoryPut(path, file);
        if (handleDirectoryPutStatus != null) { // Directory has been handled
            response.setStatus(handleDirectoryPutStatus);
        } else {
            response.setStatus(doHandleFilePut(request, file));
        }
    }

    /**
     * Handles a PUT call for the FILE protocol.
     *
     * @param request The request to update.
     * @param file The requested file or directory.
     * @return the status of the request.
     */
    private Status doHandleFilePut(Request request, File file) {
        // Several checks: first the consistency of the metadata and the filename
        if (!checkMetadataConsistency(file.getName(), request.getEntity())) {
            // Ask the client to properly reiterate its request
            return new Status(
                    CLIENT_ERROR_BAD_REQUEST, "The metadata is not consistent with the URI");
        }

        // We look for the possible variants
        // Set up base name as the longest part of the name without known extensions
        // (beginning from the left)
        final String baseName = Entity.getBaseName(file.getName(), getMetadataService());

        final Variants variants = lookForVariants(baseName, file);

        if (variants.exactVariant != null) {
            file = variants.exactVariant;
        } else if (!variants.variants.isEmpty()) {
            // Several possible variants, but can't choose one precisely.
            return new Status(
                    CLIENT_ERROR_NOT_ACCEPTABLE,
                    "Unable to process properly the request. Several variants exist but none of them suits precisely.");
        } else {
            file = computeNewFile(request, file, baseName);
        }

        // Before putting the file representation, we check that all the extensions are known
        if (!checkExtensionsConsistency(file)) {
            return new Status(
                    SERVER_ERROR_INTERNAL,
                    "Unable to process properly the URI. At least one extension is not known by the server.");
        }

        // This helper supports only a single "bytes" range.
        Range range =
                (!request.getRanges().isEmpty() && isBytesRange(request.getRanges().getFirst()))
                        ? request.getRanges().getFirst()
                        : null;

        if (file.exists()) {
            return (range == null)
                    ? replaceFile(request, file)
                    : updateFileWithPartialContent(request, file, range);
        } else {

            Status createParentDirectoryStatus = createParentDirectoryIfNecessary(file);
            if (createParentDirectoryStatus != null) {
                return createParentDirectoryStatus;
            }

            // Create the new file
            return (range == null)
                    ? createFile(request, file)
                    : createFileWithPartialContent(request, file, range);
        }
    }

    private Status createParentDirectoryIfNecessary(final File file) {
        File parent = file.getParentFile();

        if ((parent != null) && !parent.exists() && !parent.mkdirs()) {
            String message = "Unable to create the parent directory";
            getLogger().warning(message);
            return new Status(SERVER_ERROR_INTERNAL, message);
        }
        return null;
    }

    /**
     * Computes the file corresponding to the request when no exact variant has been found. It
     * completes the URI with the metadata of the request and updates the file name accordingly.
     */
    private File computeNewFile(final Request request, File file, final String baseName) {
        // This resource does not exist, yet. Complete it with the default metadata
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
        // often leveraged by operating systems to detect a file type
        updateFileExtension(fileName, request.getEntity().getMediaType());

        file = new File(file.getParentFile(), fileName.toString());
        return file;
    }

    /**
     * Returns the files that among the parent directory share the same base name as the given file.
     */
    private Variants lookForVariants(final String baseName, final File file) {
        File uniqueVariant = null;
        final List<File> variantsList = new ArrayList<>();

        // Look for resources with the same base name
        FileFilter filter =
                file1 ->
                        file1.isFile()
                                && baseName.equals(
                                        Entity.getBaseName(file1.getName(), getMetadataService()));

        File[] files = file.getParentFile().listFiles(filter);

        if (files != null && files.length > 0) {
            // Set the list of extensions, due to the file name and the default metadata.
            // TODO It seems we could handle more clearly the equivalence
            // between the file name space and the target resource (URI completed by default
            // metadata)
            Variant variant = new Variant();
            Entity.updateMetadata(file.getName(), variant, false, getMetadataService());
            Collection<String> extensions = Entity.getExtensions(variant, getMetadataService());

            for (File entry : files) {
                Collection<String> entryExtensions =
                        Entity.getExtensions(entry.getName(), getMetadataService());

                if (entryExtensions.containsAll(extensions)) {
                    variantsList.add(entry);

                    if (extensions.containsAll(entryExtensions)) {
                        // The right representation has been found.
                        uniqueVariant = entry;
                    }
                }
            }
        }

        return new Variants(uniqueVariant, variantsList);
    }

    private record Variants(File exactVariant, List<File> variants) {}

    /**
     * Consider the case of a PUT request on a directory. Returns a non-null status if the request
     * has been handled, null otherwise (the request is not targeting a directory or the directory
     * already exists).
     *
     * @param path The path of the requested file or directory.
     * @param file The requested file or directory.
     * @return the status of the request or null if nothing has been done.
     */
    private Status doHandleDirectoryPut(final String path, final File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                return new Status(
                        CLIENT_ERROR_FORBIDDEN, "Can't put a new representation of a directory");
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
        return null;
    }

    /** The update is handled in two phases: write a temporary file, replace the target file. */
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

            updateRangeInFile(request, range, tmp);

            return replaceFileByTemporaryFile(request, file, tmp);
        } catch (IOException ioe) {
            getLogger().log(WARNING, "Unable to create the temporary file", ioe);
            if (tmp != null) {
                cleanTemporaryFileIfUploadNotResumed(tmp);
            }
            return new Status(SERVER_ERROR_INTERNAL, "Unable to create a temporary file");
        }
    }

    private void updateRangeInFile(final Request request, final Range range, final File tmp)
            throws IOException {
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
        }
    }

    private Status replaceFile(Request request, File file) {
        File tmp = null;
        try {
            tmp = createPrivateTempFile();
            if (request.isEntityAvailable()) {
                Files.copy(request.getEntity().getStream(), tmp.toPath(), REPLACE_EXISTING);
            }

            return replaceFileByTemporaryFile(request, file, tmp);
        } catch (IOException ioe) {
            getLogger().log(WARNING, "Unable to create the temporary file", ioe);
            cleanTemporaryFileIfUploadNotResumed(tmp);
            return new Status(SERVER_ERROR_INTERNAL, "Unable to create a temporary file");
        }
    }

    /** Create a temporary file with private access rights. */
    private static File createPrivateTempFile() throws IOException {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        return Files.createTempFile("restlet-upload", "bin", attr).toFile();
    }

    private Status replaceFileByTemporaryFile(Request request, File file, File tmp) {
        if (!tmp.exists()) {
            return new Status(
                    SERVER_ERROR_INTERNAL, "Can't replace the existing file without new content.");
        }

        // Then delete the existing file
        if (!IoUtils.delete(file)) {
            cleanTemporaryFileIfUploadNotResumed(tmp);
            return new Status(SERVER_ERROR_INTERNAL, "Unable to delete the existing file");
        }

        // Finally, move the temporary file to the existing file location
        if (tmp.renameTo(file)) {
            if (request.isEntityAvailable()) {
                return SUCCESS_NO_CONTENT;
            }
            return SUCCESS_OK;
        }

        // Many aspects of the behavior of the method "renameTo" are inherently platform-dependent.
        // The rename operation might not be able to move a file from one file system to another.
        if (!tmp.exists()) {
            return new Status(
                    SERVER_ERROR_INTERNAL,
                    "Unable to move the temporary file to replace the existing file");
        }
        try {
            Files.move(tmp.toPath(), file.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            return new Status(
                    SERVER_ERROR_INTERNAL,
                    e,
                    "Unable to move the temporary file to replace the existing file");
        }
        return SUCCESS_OK;
    }

    /** Create a new file with the given content. */
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
        } catch (IOException ioe) {
            getLogger().log(WARNING, ERROR_MESSAGE_UNABLE_FILE_CREATION, ioe);
            return new Status(SERVER_ERROR_INTERNAL, ioe);
        }
    }

    /** Store the input stream of the request in the given file. */
    private Status createFile(Request request, File file) {
        try {
            if (request.isEntityAvailable()) {
                Files.copy(request.getEntity().getStream(), file.toPath());
                return SUCCESS_CREATED;
            } else if (file.createNewFile()) { // create empty file
                return SUCCESS_NO_CONTENT;
            } else { // creation of empty file failed (file already exists)
                getLogger().warning(ERROR_MESSAGE_UNABLE_FILE_CREATION);
                return new Status(SERVER_ERROR_INTERNAL, ERROR_MESSAGE_UNABLE_FILE_CREATION);
            }
        } catch (IOException ioe) {
            getLogger().log(WARNING, ERROR_MESSAGE_UNABLE_FILE_CREATION, ioe);
            return new Status(SERVER_ERROR_INTERNAL, ioe);
        }
    }

    private void cleanTemporaryFileIfUploadNotResumed(File tmp) {
        if (tmp != null && tmp.exists() && !isResumeUpload()) {
            IoUtils.delete(tmp);
        }
    }

    /**
     * Indicates if a failed upload can be resumed. This will prevent the deletion of the temporary
     * file created. Defaults to "false".
     *
     * @return True if a failed upload can be resumed, false otherwise.
     */
    public boolean isResumeUpload() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue("resumeUpload", "false"));
    }

    /**
     * Complete the given file name with the extension corresponding to the given metadata.
     *
     * @param fileName The file name to complete.
     * @param metadata The metadata.
     */
    private void updateFileExtension(StringBuilder fileName, Metadata metadata) {
        boolean defaultMetadata = true;

        if (getMetadataService() != null) {
            if (metadata instanceof Language language) {
                defaultMetadata = language.equals(getMetadataService().getDefaultLanguage());
            } else if (metadata instanceof MediaType mediaType) {
                defaultMetadata = mediaType.equals(getMetadataService().getDefaultMediaType());
            } else if (metadata instanceof CharacterSet characterSet) {
                defaultMetadata =
                        characterSet.equals(getMetadataService().getDefaultCharacterSet());
            } else if (metadata instanceof Encoding encoding) {
                defaultMetadata = encoding.equals(getMetadataService().getDefaultEncoding());
            }
        }

        // We only add an extension for metadata that differs from default ones
        if (!defaultMetadata) {
            String extension = getMetadataService().getExtension(metadata);

            if (extension != null) {
                fileName.append(".").append(extension);
            } else {
                if (metadata.getParent() != null) {
                    updateFileExtension(fileName, metadata.getParent());
                }
            }
        }
    }
}
