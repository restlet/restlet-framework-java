/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.local;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.ReferenceList;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * ZIP and JAR client connector. Only works for archives available as local files.<br>
 * <br>
 * Handles GET, HEAD, and PUT request on resources referenced as: zip:file://<file path>
 *
 * @author Remi Dewitte remi@gide.net
 */
public class ZipClientHelper extends LocalClientHelper {

    /**
     * Constructor.
     *
     * @param client The helped client.
     */
    public ZipClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.ZIP);
        getProtocols().add(Protocol.JAR);
    }

    /**
     * Handles a call for a local entity. By default, only GET and HEAD methods are implemented.
     *
     * @param request The request to handle.
     * @param response The response to modify.
     * @param decodedPath The URL decoded entity path.
     */
    @Override
    protected void handleLocal(Request request, Response response, String decodedPath) {
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
            if (Method.GET.equals(request.getMethod()) || Method.HEAD.equals(request.getMethod())) {
                handleGet(request, response, file, entryName, getMetadataService());
            } else if (Method.PUT.equals(request.getMethod())) {
                handlePut(request, response, file, entryName);
            } else {
                response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                response.getAllowedMethods().add(Method.GET);
                response.getAllowedMethods().add(Method.HEAD);
                response.getAllowedMethods().add(Method.PUT);
            }
        } else {
            response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED, "Only works on local files.");
        }
    }

    /**
     * Handles a GET call.
     *
     * @param request The request to answer.
     * @param response The response to modify.
     * @param file The Zip archive file.
     * @param entryName The Zip archive entry name.
     * @param metadataService The metadata service.
     */
    protected void handleGet(
            Request request,
            Response response,
            File file,
            String entryName,
            final MetadataService metadataService) {

        if (!file.exists()) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }

        boolean shouldCloseZipFile = true;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            final Entity entity = new ZipEntryEntity(zipFile, entryName, metadataService);
            if (entity.exists()) {
                final Representation output;

                if (entity.isDirectory()) {
                    // Return the directory listing
                    final Collection<Entity> children = entity.getChildren();
                    final ReferenceList rl = new ReferenceList(children.size());
                    String fileUri = LocalReference.createFileReference(file).toString();
                    String scheme = request.getResourceRef().getScheme();
                    String baseUri = scheme + ":" + fileUri + "!/";

                    for (final Entity entry : children) {
                        rl.add(baseUri + entry.getName());
                    }

                    output = rl.getTextRepresentation();

                } else { // Return the file content
                    shouldCloseZipFile = false; // Keep zipFile open for the stream to be read
                    output =
                            entity.getRepresentation(
                                    metadataService.getDefaultMediaType(), getTimeToLive());
                    output.setLocationRef(request.getResourceRef());
                    Entity.updateMetadata(entity.getName(), output, true, getMetadataService());
                }

                response.setStatus(Status.SUCCESS_OK);
                response.setEntity(output);
            } else {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        } catch (Exception e) {
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }

        if (shouldCloseZipFile && zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException ignored) {
                // Ignored exception
            }
        }
    }

    /**
     * Handles a PUT call.
     *
     * @param request The request to answer.
     * @param response The response to modify.
     * @param file The Zip archive file.
     * @param entryName The Zip archive entry name.
     */
    protected void handlePut(Request request, Response response, File file, String entryName) {
        if ("".equals(entryName)
                && request.getEntity() != null
                && request.getEntity().getDisposition() != null) {
            entryName = request.getEntity().getDisposition().getFilename();
        }
        if (entryName == null) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Must specify an entry name.");
            return;
        }

        boolean isDirectory = entryName.endsWith("/");
        try {
            if (file.exists()) {
                try (ZipFile zipFile = new ZipFile(file)) {
                    final boolean wrongReplace;
                    if (isDirectory) { // File with the same name?
                        final String directoryName = entryName.substring(0, entryName.length() - 1);
                        wrongReplace = null != zipFile.getEntry(directoryName);
                    } else { // Directory with the same name?
                        wrongReplace = null != zipFile.getEntry(entryName + "/");
                    }

                    if (wrongReplace) {
                        response.setStatus(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Directory cannot be replaced by a file or file by a directory.");
                        return;
                    }
                }
            }

            final Representation entity = isDirectory ? null : request.getEntity();

            if (file.exists()) {
                final File newZipFile =
                        copyZipFileWithUpdatedEntry(file, entryName, entity, isDirectory);
                Files.move(newZipFile.toPath(), file.toPath(), REPLACE_EXISTING);

                response.setStatus(Status.SUCCESS_OK);
            } else {
                try (ZipOutputStream zipOut =
                        new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                    writeEntityStream(entity, zipOut, entryName, isDirectory);
                    response.setStatus(Status.SUCCESS_CREATED);
                }
            }
        } catch (Exception e) {
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    private File copyZipFileWithUpdatedEntry(
            final File file,
            final String entryName,
            final Representation entity,
            final boolean isDirectory)
            throws IOException {

        final File writeTo = createPrivateTempFile();

        try (final ZipFile zipFile = new ZipFile(file);
                final ZipOutputStream zipOut =
                        new ZipOutputStream(
                                new BufferedOutputStream(new FileOutputStream(writeTo)))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            boolean replaced = false;
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (!replaced && entryName.equals(e.getName())) {
                    writeEntityStream(entity, zipOut, entryName, isDirectory);
                    replaced = true;
                } else {
                    zipOut.putNextEntry(e);
                    try (final BufferedInputStream zipStream =
                            new BufferedInputStream(zipFile.getInputStream(e))) {
                        IoUtils.copy(zipStream, zipOut);
                    }
                    zipOut.closeEntry();
                }
            }
            if (!replaced) {
                writeEntityStream(entity, zipOut, entryName, isDirectory);
            }
        }
        return writeTo;
    }

    /** Create a temporary file with private access rights. */
    private static File createPrivateTempFile() throws IOException {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        return Files.createTempFile("restlet_zip_", "zip", attr).toFile();
    }

    /**
     * Writes an entity to a given ZIP output stream with a given ZIP entry name.
     *
     * @param entity The entity to write.
     * @param out The ZIP output stream.
     * @param entryName The ZIP entry name.
     * @throws IOException Thrown if an I/O error occurs.
     */
    private void writeEntityStream(
            Representation entity, ZipOutputStream out, String entryName, boolean isDirectory)
            throws IOException {
        if (entity == null || isDirectory) {
            out.putNextEntry(new ZipEntry(entryName));
            out.closeEntry();
        } else {
            ZipEntry entry = new ZipEntry(entryName);
            if (entity.getModificationDate() != null) {
                entry.setTime(entity.getModificationDate().getTime());
            } else {
                entry.setTime(System.currentTimeMillis());
            }
            out.putNextEntry(entry);
            try (final BufferedInputStream entityStream =
                    new BufferedInputStream(entity.getStream())) {
                IoUtils.copy(entityStream, out);
            }
            out.closeEntry();
        }
    }
}
