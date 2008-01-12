/*
 * Copyright 2005-2008 Noelios Consulting.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;

/**
 * Connector to the WAR resources. Here is the list of parameters that are
 * supported: <table>
 * <tr>
 * <td>warPath</td>
 * <td>String</td>
 * <td>${user.home}/restlet.war</td>
 * <td>Path to the Web Application WAR file or directory.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class WarClientHelper extends FileClientHelper {
    /**
     * Restrict the access to the META-INF and WEB-INF directories. False by
     * default.
     */
    private volatile boolean restrict;

    /** The location of the Web Application archive file or directory path. */
    private volatile String warPath;

    /**
     * Indicates if the Web Application path corresponds to an archive file or a
     * directory path.
     */
    private volatile boolean webAppArchive;

    /** Cache of all the WAR file entries to improve directory listing time. */
    private volatile List<String> warEntries;

    /**
     * Constructor. Note that the common list of metadata associations based on
     * extensions is added, see the addCommonExtensions() method.
     * 
     * @param client
     *                The client to help.
     */
    public WarClientHelper(Client client) {
        super(client);
        getProtocols().clear();
        getProtocols().add(Protocol.WAR);
        this.restrict = false;
        this.warPath = null;
        this.webAppArchive = false;
        this.warEntries = null;
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

        if (scheme.equalsIgnoreCase("war")) {
            handleWar(request, response);
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only WAR is supported.");
        }
    }

    /**
     * Handles a call using the current Web Application.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    protected void handleWar(Request request, Response response) {
        getWarPath();

        if (this.webAppArchive) {
            try {
                String path = request.getResourceRef().getPath();
                JarFile war = new JarFile(getWarPath());
                // As the path may be percent-encoded, it has to be
                // percent-decoded.
                // Prepare a jar URI, removing the leading slash
                if ((path != null) && path.startsWith("/"))
                    path = path.substring(1);
                JarEntry entry = war.getJarEntry(Reference.decode(path));

                if (entry.isDirectory()) {
                    if (warEntries == null) {
                        // Cache of all the WAR file entries to improve
                        // directory listing time.
                        warEntries = new ArrayList<String>();
                        for (Enumeration<JarEntry> entries = war.entries(); entries
                                .hasMoreElements();) {
                            warEntries.add(entries.nextElement().getName());
                        }
                    }

                    // Return the directory listing
                    ReferenceList rl = new ReferenceList();
                    rl.setIdentifier(request.getResourceRef());

                    for (String warEntry : warEntries) {
                        if (warEntry.startsWith(path)) {
                            rl.add(new Reference(warEntry));
                        }
                    }

                    response.setEntity(rl.getTextRepresentation());
                    response.setStatus(Status.SUCCESS_OK);
                } else {
                    // Return the file content
                    Representation output = new InputRepresentation(war
                            .getInputStream(entry), null);
                    output.setIdentifier(request.getResourceRef());
                    updateMetadata(getMetadataService(request), path, output);
                    response.setEntity(output);
                    response.setStatus(Status.SUCCESS_OK);
                }
            } catch (IOException e) {
                getLogger().log(Level.WARNING,
                        "Unable to access to the WAR file", e);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }

        } else {
            String path = request.getResourceRef().getPath();

            if (isRestrict() && path.toUpperCase().startsWith("/WEB-INF/")) {
                getLogger().warning(
                        "Forbidden access to the WEB-INF directory detected. Path requested: "
                                + path);
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else if (isRestrict()
                    && path.toUpperCase().startsWith("/META-INF/")) {
                getLogger().warning(
                        "Forbidden access to the META-INF directory detected. Path requested: "
                                + path);
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                path = getWarPath() + path;
                handleFile(request, response, path);
            }
        }
    }

    /**
     * Returns the Web Application archive file or directory path.
     * 
     * @return The Web Application archive file or directory path.
     */
    public String getWarPath() {
        if (this.warPath == null) {
            this.warPath = getParameters().getFirstValue(
                    "warPath",
                    System.getProperty("user.home") + File.separator
                            + "restlet.war");
            File file = new File(this.warPath);

            if (file.exists()) {
                if (file.isDirectory()) {
                    this.webAppArchive = false;

                    // Adjust the archive directory path if necessary
                    if (warPath.endsWith("/"))
                        this.warPath = this.warPath.substring(0, this.warPath
                                .length() - 1);
                } else {
                    this.webAppArchive = true;
                }
            } else {
                getLogger().warning(
                        "Unable to find an existing directory or archive at: "
                                + this.warPath);
            }
        }

        return this.warPath;
    }

    /**
     * Indicates if the access to the META-INF and WEB-INF directories is
     * restricted. False by default.
     * 
     * @return True if the access is restricted.
     */
    public boolean isRestrict() {
        return this.restrict;
    }

    /**
     * Indicates if the access to the META-INF and WEB-INF directories is
     * restricted.
     * 
     * @param restrict
     *                True if the access is restricted.
     */
    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

}
