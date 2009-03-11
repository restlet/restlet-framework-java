/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.io.ByteUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

/**
 * Connector to the file resources accessible. Here is the list of parameters
 * that are supported:
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

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public FileClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.FILE);
    }

    /**
     * Check that all extensions of the file correspond to a known metadata.
     * 
     * @param file
     *            The file whose extensions are checked.
     * @param metadataService
     *            The metadata service.
     * @return True if all extensions of the file are known by the metadata
     *         service.
     */
    protected boolean checkExtensionsConsistency(File file,
            MetadataService metadataService) {
        boolean knownExtension = true;

        final Collection<String> set = Entity.getExtensions(file.getName(),
                metadataService);
        final Iterator<String> iterator = set.iterator();
        while (iterator.hasNext() && knownExtension) {
            knownExtension = metadataService.getMetadata(iterator.next()) != null;
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
     * @param metadataService
     *            metadata helper
     * @param representation
     *            the provided representation
     * @return true if the metadata of the representation are compatible with
     *         the metadata extracted from the filename
     */
    private boolean checkMetadataConsistency(String fileName,
            MetadataService metadataService, Representation representation) {
        boolean result = true;
        if (representation != null) {
            final Variant var = new Variant();
            updateMetadata(metadataService, fileName, var);
            // "var" contains the theorical correct metadata
            if (!var.getLanguages().isEmpty()
                    && !representation.getLanguages().isEmpty()
                    && !var.getLanguages().containsAll(
                            representation.getLanguages())) {
                result = false;
            }
            if ((var.getMediaType() != null)
                    && (representation.getMediaType() != null)
                    && !(var.getMediaType().includes(representation
                            .getMediaType()))) {
                result = false;
            }
            if (!var.getEncodings().isEmpty()
                    && !representation.getEncodings().isEmpty()
                    && !var.getEncodings().containsAll(
                            representation.getEncodings())) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public Entity getEntity(String decodedPath) {
        // Take care of the file separator.
        return new FileEntity(
                new File(LocalReference.localizePath(decodedPath)));
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
        final String scheme = request.getResourceRef().getScheme();
        if (Protocol.FILE.getSchemeName().equalsIgnoreCase(scheme)) {
            super.handle(request, response);
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only FILE is supported.");
        }
    }

    @Override
    protected void handleEntity(Request request, Response response,
            String path, String decodedPath, MetadataService metadataService) {
        if (Method.GET.equals(request.getMethod())
                || Method.HEAD.equals(request.getMethod())) {
            handleEntityGet(request, response, path, getEntity(decodedPath),
                    metadataService);
        } else if (Method.PUT.equals(request.getMethod())) {
            handleFilePut(request, response, decodedPath,
                    new File(decodedPath), metadataService);
        } else if (Method.DELETE.equals(request.getMethod())) {
            handleFileDelete(response, new File(decodedPath));
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
            response.getAllowedMethods().add(Method.PUT);
            response.getAllowedMethods().add(Method.DELETE);
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
                if (file.delete()) {
                    response.setStatus(Status.SUCCESS_NO_CONTENT);
                } else {
                    response.setStatus(Status.SERVER_ERROR_INTERNAL,
                            "Couldn't delete the directory");
                }
            } else {
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                        "Couldn't delete the non-empty directory");
            }
        } else {
            if (file.delete()) {
                response.setStatus(Status.SUCCESS_NO_CONTENT);
            } else {
                response.setStatus(Status.SERVER_ERROR_INTERNAL,
                        "Couldn't delete the file");
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
     * @param metadataService
     *            The metadata service.
     */
    protected void handleFilePut(Request request, Response response,
            String path, File file, final MetadataService metadataService) {

        // Deals with directory
        boolean isDirectory = false;
        if (file.exists()) {
            if (file.isDirectory()) {
                isDirectory = true;
                response.setStatus(new Status(Status.CLIENT_ERROR_FORBIDDEN,
                        "Can't put a new representation of a directory"));
                return;
            }
        } else {
            // No existing file or directory found
            if (path.endsWith("/")) {
                isDirectory = true;
                // Create a new directory and its parents if necessary
                if (file.mkdirs()) {
                    response.setStatus(Status.SUCCESS_NO_CONTENT);
                } else {
                    getLogger().log(Level.WARNING,
                            "Unable to create the new directory");
                    response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
                            "Unable to create the new directory"));
                }
                return;
            }
        }

        if (!isDirectory) {
            // Several checks : first the consistency of the metadata and
            // the filename
            boolean partialPut = !request.getRanges().isEmpty();
            if (!checkMetadataConsistency(file.getName(), metadataService,
                    request.getEntity())) {
                // ask the client to reiterate properly its request
                response.setStatus(new Status(Status.REDIRECTION_SEE_OTHER,
                        "The metadata are not consistent with the URI"));
                return;
            } else {
                // We look for the possible variants
                // 1- set up base name as the longest part of the name
                // without known extensions (beginning from the left)
                final String baseName = Entity.getBaseName(file.getName(),
                        metadataService);
                final Collection<String> extensions = Entity.getExtensions(file
                        .getName(), metadataService);
                // 2- loooking for resources with the same base name
                final File[] files = file.getParentFile().listFiles();
                File uniqueVariant = null;

                final List<File> variantsList = new ArrayList<File>();
                if (files != null) {
                    for (final File entry : files) {
                        if (baseName.equals(Entity.getBaseName(entry.getName(),
                                metadataService))) {
                            final Collection<String> entryExtensions = Entity
                                    .getExtensions(entry.getName(),
                                            metadataService);
                            if (entryExtensions.containsAll(extensions)) {
                                variantsList.add(entry);
                                if (extensions.containsAll(entryExtensions)) {
                                    // The right representation has been found.
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
                        // Negotiated resource (several variants, but not the
                        // right one).
                        // Check if the request could be completed or not.
                        // The request could be more precise
                        response
                                .setStatus(new Status(
                                        Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                                        "Unable to process properly the request. Several variants exist but none of them suits precisely."));
                        return;
                    } else {
                        // This resource does not exist, yet.
                        // Complete it with the default metadata
                        updateMetadata(metadataService, file.getName(), request
                                .getEntity());
                        if (request.getEntity().getLanguages().isEmpty()) {
                            if (metadataService.getDefaultLanguage() != null) {
                                request.getEntity().getLanguages().add(
                                        metadataService.getDefaultLanguage());
                            }
                        }
                        if (request.getEntity().getMediaType() == null) {
                            request.getEntity().setMediaType(
                                    metadataService.getDefaultMediaType());
                        }
                        if (request.getEntity().getEncodings().isEmpty()) {
                            if ((metadataService.getDefaultEncoding() != null)
                                    && !metadataService.getDefaultEncoding()
                                            .equals(Encoding.IDENTITY)) {
                                request.getEntity().getEncodings().add(
                                        metadataService.getDefaultEncoding());
                            }
                        }
                        // Update the URI
                        final StringBuilder fileName = new StringBuilder(
                                baseName);
                        if (metadataService.getExtension(request.getEntity()
                                .getMediaType()) != null) {
                            fileName.append("."
                                    + metadataService.getExtension(request
                                            .getEntity().getMediaType()));
                        }
                        for (final Language language : request.getEntity()
                                .getLanguages()) {
                            if (metadataService.getExtension(language) != null) {
                                fileName.append("."
                                        + metadataService
                                                .getExtension(language));
                            }
                        }
                        for (final Encoding encoding : request.getEntity()
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
                    return;
                } else {
                    File tmp = null;
                    boolean error = false;

                    if (file.exists()) {
                        // The PUT call is handled in two phases:
                        // 1- write a temporary file
                        // 2- rename the target file
                        if (partialPut) {
                            RandomAccessFile raf = null;
                            // Replace the content of the file
                            // First, create a temporary file
                            try {
                                // The temporary file used for partial PUT.
                                tmp = new File(file.getCanonicalPath() + "."
                                        + getTemporaryExtension());
                                // Support only one range.
                                Range range = request.getRanges().get(0);

                                if (tmp.exists() && !isResumeUpload()) {
                                    tmp.delete();
                                }

                                if (!tmp.exists()) {
                                    // Copy the target file.
                                    final BufferedReader br = new BufferedReader(
                                            new FileReader(file));
                                    final BufferedWriter wr = new BufferedWriter(
                                            new FileWriter(tmp));
                                    String s;
                                    while ((s = br.readLine()) != null) {
                                        wr.append(s);
                                    }

                                    br.close();
                                    wr.flush();
                                    wr.close();
                                }
                                raf = new RandomAccessFile(tmp, "rwd");

                                // Go to the desired offset.
                                if (range.getIndex() == Range.INDEX_LAST) {
                                    if (raf.length() <= range.getSize()) {
                                        raf.seek(range.getSize());
                                    } else {
                                        raf
                                                .seek(raf.length()
                                                        - range.getSize());
                                    }
                                } else {
                                    raf.seek(range.getIndex());
                                }

                                // Write the entity to the temporary file.
                                if (request.isEntityAvailable()) {
                                    ByteUtils.write(request.getEntity()
                                            .getStream(), raf);
                                }
                            } catch (IOException ioe) {
                                getLogger().log(Level.WARNING,
                                        "Unable to create the temporary file",
                                        ioe);
                                response.setStatus(new Status(
                                        Status.SERVER_ERROR_INTERNAL,
                                        "Unable to create a temporary file"));
                                error = true;
                            } finally {
                                try {
                                    if (raf != null) {
                                        raf.close();

                                        // Calling the garbage collector helps
                                        // to workaround lock issues on Windows
                                        System.gc();
                                    }
                                } catch (IOException ioe) {
                                    getLogger()
                                            .log(
                                                    Level.WARNING,
                                                    "Unable to close the temporary file",
                                                    ioe);
                                    response.setStatus(
                                            Status.SERVER_ERROR_INTERNAL, ioe);
                                    error = true;
                                }
                            }
                        } else {
                            FileOutputStream fos = null;
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
                                error = true;
                            } finally {
                                try {
                                    if (fos != null) {
                                        fos.close();
                                    }
                                } catch (IOException ioe) {
                                    getLogger()
                                            .log(
                                                    Level.WARNING,
                                                    "Unable to close the temporary file",
                                                    ioe);
                                    response.setStatus(
                                            Status.SERVER_ERROR_INTERNAL, ioe);
                                    error = true;
                                }
                            }
                        }

                        if (error) {
                            if (tmp.exists() && !isResumeUpload()) {
                                tmp.delete();
                            }
                            return;
                        }

                        // Then delete the existing file
                        if (tmp.exists() && file.delete()) {
                            // Finally move the temporary file to the
                            // existing file location
                            boolean renameSuccessfull = false;
                            if (tmp.renameTo(file)) {
                                if (request.getEntity() == null) {
                                    response
                                            .setStatus(Status.SUCCESS_NO_CONTENT);
                                } else {
                                    response.setStatus(Status.SUCCESS_OK);
                                }
                                renameSuccessfull = true;
                            } else {
                                // Many aspects of the behavior of the method
                                // "renameTo" are inherently platform-dependent:
                                // the rename operation might not be able to
                                // move a file from one file system to another.
                                if (tmp.exists()) {
                                    try {
                                        final BufferedReader br = new BufferedReader(
                                                new FileReader(tmp));
                                        final BufferedWriter wr = new BufferedWriter(
                                                new FileWriter(file));
                                        String s;
                                        while ((s = br.readLine()) != null) {
                                            wr.append(s);
                                        }

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
                            if (tmp.exists() && !isResumeUpload()) {
                                tmp.delete();
                            }
                        }
                    } else {
                        // The file does not exist yet.
                        final File parent = file.getParentFile();
                        if ((parent != null) && !parent.exists()) {
                            // Create the parent directories then the new file
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

                        // Create the new file
                        if (partialPut) {
                            // This is a partial PUT
                            RandomAccessFile raf = null;
                            try {
                                raf = new RandomAccessFile(file, "rwd");
                                // Support only one range.
                                Range range = request.getRanges().get(0);
                                // Go to the desired offset.
                                if (range.getIndex() == Range.INDEX_LAST) {
                                    if (raf.length() <= range.getSize()) {
                                        raf.seek(range.getSize());
                                    } else {
                                        raf
                                                .seek(raf.length()
                                                        - range.getSize());
                                    }
                                } else {
                                    raf.seek(range.getIndex());
                                }
                                // Write the entity to the file.
                                if (request.isEntityAvailable()) {
                                    ByteUtils.write(request.getEntity()
                                            .getStream(), raf);
                                }
                            } catch (FileNotFoundException fnfe) {
                                getLogger().log(Level.WARNING,
                                        "Unable to create the new file", fnfe);
                                response.setStatus(
                                        Status.SERVER_ERROR_INTERNAL, fnfe);
                            } catch (IOException ioe) {
                                getLogger().log(Level.WARNING,
                                        "Unable to create the new file", ioe);
                                response.setStatus(
                                        Status.SERVER_ERROR_INTERNAL, ioe);
                            } finally {
                                try {
                                    if (raf != null) {
                                        raf.close();

                                        // Calling the garbage collector helps
                                        // to workaround lock issues on Windows
                                        System.gc();
                                    }
                                } catch (IOException ioe) {
                                    getLogger()
                                            .log(
                                                    Level.WARNING,
                                                    "Unable to close the new file",
                                                    ioe);
                                    response.setStatus(
                                            Status.SERVER_ERROR_INTERNAL, ioe);
                                }
                            }

                        } else {
                            // This is simple PUT of the full entity
                            FileOutputStream fos = null;
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
                                response.setStatus(
                                        Status.SERVER_ERROR_INTERNAL, fnfe);
                            } catch (IOException ioe) {
                                getLogger().log(Level.WARNING,
                                        "Unable to create the new file", ioe);
                                response.setStatus(
                                        Status.SERVER_ERROR_INTERNAL, ioe);
                            } finally {
                                try {
                                    if (fos != null) {
                                        fos.close();
                                    }
                                } catch (IOException ioe) {
                                    getLogger()
                                            .log(
                                                    Level.WARNING,
                                                    "Unable to close the new file",
                                                    ioe);
                                    response.setStatus(
                                            Status.SERVER_ERROR_INTERNAL, ioe);
                                }
                            }
                        }
                    }
                }
            }
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
}
