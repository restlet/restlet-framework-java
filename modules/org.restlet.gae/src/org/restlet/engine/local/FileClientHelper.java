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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
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
