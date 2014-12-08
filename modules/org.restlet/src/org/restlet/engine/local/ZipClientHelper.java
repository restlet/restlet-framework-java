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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
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
 * ZIP and JAR client connector. Only works for archives available as local
 * files.<br>
 * <br>
 * Handles GET, HEAD and PUT request on resources referenced as :
 * zip:file://<file path>
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class ZipClientHelper extends LocalClientHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The helped client.
     */
    public ZipClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.ZIP);
        getProtocols().add(Protocol.JAR);
    }

    /**
     * Handles a call for a local entity. By default, only GET and HEAD methods
     * are implemented.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param decodedPath
     *            The URL decoded entity path.
     */
    @Override
    protected void handleLocal(Request request, Response response,
            String decodedPath) {
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
                handleGet(request, response, file, entryName,
                        getMetadataService());
            } else if (Method.PUT.equals(request.getMethod())) {
                handlePut(request, response, file, entryName);
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

        if (!file.exists()) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        } else {
            ZipFile zipFile;

            try {
                zipFile = new ZipFile(file);
            } catch (Exception e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return;
            }

            Entity entity = new ZipEntryEntity(zipFile, entryName,
                    metadataService);
            if (!entity.exists()) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
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
                    output = entity.getRepresentation(
                            metadataService.getDefaultMediaType(),
                            getTimeToLive());
                    output.setLocationRef(request.getResourceRef());
                    Entity.updateMetadata(entity.getName(), output, true,
                            getMetadataService());
                }

                response.setStatus(Status.SUCCESS_OK);
                response.setEntity(output);
            }
        }
    }

    /**
     * Handles a PUT call.
     * 
     * @param request
     *            The request to answer.
     * @param response
     *            The response to update.
     * @param file
     *            The Zip archive file.
     * @param entryName
     *            The Zip archive entry name.
     */
    protected void handlePut(Request request, Response response, File file,
            String entryName) {
        boolean zipExists = file.exists();
        ZipOutputStream zipOut = null;

        if ("".equals(entryName) && request.getEntity() != null
                && request.getEntity().getDisposition() != null) {
            entryName = request.getEntity().getDisposition().getFilename();
        }
        if (entryName == null) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Must specify an entry name.");
            return;
        }
        // boolean canAppend = true;
        boolean canAppend = !zipExists;
        boolean isDirectory = entryName.endsWith("/");
        boolean wrongReplace = false;
        try {
            if (zipExists) {
                ZipFile zipFile = new ZipFile(file);
                // Already exists ?
                canAppend &= null == zipFile.getEntry(entryName);
                // Directory with the same name ?
                if (isDirectory) {
                    wrongReplace = null != zipFile.getEntry(entryName
                            .substring(0, entryName.length() - 1));
                } else {
                    wrongReplace = null != zipFile.getEntry(entryName + "/");
                }

                canAppend &= !wrongReplace;
                zipFile.close();
            }

            Representation entity;
            if (isDirectory) {
                entity = null;
            } else {
                entity = request.getEntity();
            }

            if (canAppend) {
                try {
                    // zipOut = new ZipOutputStream(new BufferedOutputStream(new
                    // FileOutputStream(file, true)));
                    zipOut = new ZipOutputStream(new BufferedOutputStream(
                            new FileOutputStream(file)));
                    writeEntityStream(entity, zipOut, entryName);
                    zipOut.close();
                } catch (Exception e) {
                    response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
                    return;
                } finally {
                    if (zipOut != null)
                        zipOut.close();
                }
                response.setStatus(Status.SUCCESS_CREATED);
            } else {
                if (wrongReplace) {
                    response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                            "Directory cannot be replaced by a file or file by a directory.");
                } else {
                    File writeTo = null;
                    ZipFile zipFile = null;
                    try {
                        writeTo = File.createTempFile("restlet_zip_", "zip");
                        zipFile = new ZipFile(file);
                        zipOut = new ZipOutputStream(new BufferedOutputStream(
                                new FileOutputStream(writeTo)));
                        Enumeration<? extends ZipEntry> entries = zipFile
                                .entries();
                        boolean replaced = false;
                        while (entries.hasMoreElements()) {
                            ZipEntry e = entries.nextElement();
                            if (!replaced && entryName.equals(e.getName())) {
                                writeEntityStream(entity, zipOut, entryName);
                                replaced = true;
                            } else {
                                zipOut.putNextEntry(e);
                                IoUtils.copy(
                                        new BufferedInputStream(zipFile
                                                .getInputStream(e)), zipOut);
                                zipOut.closeEntry();
                            }
                        }
                        if (!replaced) {
                            writeEntityStream(entity, zipOut, entryName);
                        }
                        zipFile.close();
                        zipOut.close();
                    } finally {
                        try {
                            if (zipFile != null)
                                zipFile.close();
                        } finally {
                            if (zipOut != null)
                                zipOut.close();
                        }
                    }

                    if (!(IoUtils.delete(file) && writeTo.renameTo(file))) {
                        if (!file.exists())
                            file.createNewFile();
                        FileInputStream fis = null;
                        FileOutputStream fos = null;
                        try {
                            fis = new FileInputStream(writeTo);
                            fos = new FileOutputStream(file);
                            // ByteUtils.write(fis.getChannel(),
                            // fos.getChannel());
                            IoUtils.copy(fis, fos);
                            response.setStatus(Status.SUCCESS_OK);
                        } finally {
                            try {
                                if (fis != null)
                                    fis.close();
                            } finally {
                                if (fos != null)
                                    fos.close();
                            }
                        }
                    } else {
                        response.setStatus(Status.SUCCESS_OK);
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
            return;
        }
    }

    /**
     * Writes an entity to a given ZIP output stream with a given ZIP entry
     * name.
     * 
     * @param entity
     *            The entity to write.
     * @param out
     *            The ZIP output stream.
     * @param entryName
     *            The ZIP entry name.
     * @return True if the writing was successful.
     * @throws IOException
     */
    private boolean writeEntityStream(Representation entity,
            ZipOutputStream out, String entryName) throws IOException {
        if (entity != null && !entryName.endsWith("/")) {
            ZipEntry entry = new ZipEntry(entryName);
            if (entity.getModificationDate() != null)
                entry.setTime(entity.getModificationDate().getTime());
            else {
                entry.setTime(System.currentTimeMillis());
            }
            out.putNextEntry(entry);
            IoUtils.copy(new BufferedInputStream(entity.getStream()), out);
            out.closeEntry();
            return true;
        }

        out.putNextEntry(new ZipEntry(entryName));
        out.closeEntry();
        return false;
    }
}
