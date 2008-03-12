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

package com.noelios.restlet.ext.xdb;

import com.noelios.restlet.local.WarClientHelper;

import java.io.InputStream;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import javax.servlet.ServletConfig;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.service.MetadataService;


/**
 * Local client connector based on a XMLDB repository.
 * WARs are deployed at XMLDB repository using this directory layout
 * /home/[SCHEMA]/wars/[AppName]/
 * where SCHEMA: is a database user, for example SCOTT
 *      AppName: is a Servlet Name configured with XdbServerServlet adapter
 * Note:
 *   For Servlet running with PUBLIC grants run with an effective user ANONYMOUS
 * so WARs deployment will be located at /home/ANONYMOUS/wars
 *
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 */
public class XdbServletWarClientHelper extends WarClientHelper {
    /**
     * XMLDB base directory used to access to resources
     */
    private static final String BASE_DIR = "/home/";

    /**
     * XMLDB directory appended to connectedUser where WARs are deployed
     * final directory for WARs will be:
     * /home/SCOTT/wars/HelloRestlet/
     * for example.
     */
    private static final String DEPLOY_DIR = "/wars/";

    /** The Servlet Config to use. */
    private volatile ServletConfig config;

    /**
     * Efective user who is running XdbServerServlet for example SCOTT or
     * ANONYMOUS if is a Servlet running with PUBLIC grants
     * (no http auhtorization is required)
     */
    private volatile String connectedUser;

    /**
     * SQL Connection to XMLDB repository
     */
    private volatile Connection conn = null;

    /**
     * Constructor.
     *
     * @param client
     *                The client to help.
     * @param config
     *                The Servlet Config
     * @param conn
     *                The JDBC Connection
     */
    public XdbServletWarClientHelper(Client client, ServletConfig config,
                                     Connection conn) {
        super(client);
        this.config = config;
        this.conn = conn;
    }

    /**
     * Returns the Servlet Config.
     *
     * @return The Servlet Config.
     */
    public ServletConfig getConfig() {
        return this.config;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleWar(Request request, Response response) {
        PreparedStatement stmt = null;
        ResultSet rset = null;
        if (request.getMethod().equals(Method.GET) 
            || request.getMethod().equals(Method.HEAD)) {
            String basePath = request.getResourceRef().getPath();
            int lastSlashIndex = basePath.lastIndexOf('/');
            String entry =
                (lastSlashIndex == -1) 
                ? basePath : basePath.substring(lastSlashIndex + 1);
            Representation output = null;
            String xdbResPath =
                BASE_DIR
                + connectedUser
                + DEPLOY_DIR
                + this.config.getServletName()
                + basePath;

            if (basePath.endsWith("/")) {
                // Return the directory listing
                try {
                    stmt = conn.prepareStatement(
                    "SELECT path(1),extractValue(res,'/Resource/@Container') "
                    + "FROM resource_view WHERE under_path(res,1,?,1 ) = 1");
                    this.getLogger().info("looking resources at: "
                                          + xdbResPath);
                    stmt.setString(1, xdbResPath);
                    rset = stmt.executeQuery();
                    if (rset.next()) {
                        ReferenceList rl = new ReferenceList();
                        rl.setIdentifier(request.getResourceRef());

                        while (rset.next()) {
                            entry = rset.getString(1)
                                    + (("true".equalsIgnoreCase(rset.getString(2)))
                                    ? "/" : "");
                            this.getLogger().info("Reference: " + basePath
                                                  + entry);
                            rl.add(new Reference(basePath + entry));
                        }

                        output = rl.getTextRepresentation();
                    }
                } catch (SQLException sqe) {
                    this.getLogger().throwing("XdbServletWarClientHelper",
                                              "handleWar", sqe);
                    throw new RuntimeException(
                             "Exception querying resource_view - xdbResPath: "
                             + xdbResPath, sqe);
                } finally {
                    XdbServerServlet.closeDbResources(stmt, rset);
                }
            } else {
                // Return the entry content
                try {
                    InputStream is = null;
                    stmt = conn.prepareStatement(
                      "select xdburitype(?).getBlob(),"
                      + "xdburitype(?).getContentType() " + "from dual");
                    stmt.setString(1, xdbResPath);
                    stmt.setString(2, xdbResPath);
                    this.getLogger().info("looking resources at: "
                                          + xdbResPath);
                    rset = stmt.executeQuery();
                    if (rset.next()) {
                        Blob blob = (Blob) rset.getObject(1);
                        String mediaType = rset.getString(2);
                        is = blob.getBinaryStream();
                        MetadataService metadataService =
                            getMetadataService(request);
                        output =
                                new InputRepresentation(is,
                                        metadataService.getDefaultMediaType());
                        output.setIdentifier(request.getResourceRef());
                        updateMetadata(metadataService, entry, output);

                        // See if the Servlet context specified
                        // a particular Mime Type
                        if (mediaType != null) {
                            this.getLogger().info("mediaType: " + mediaType);
                            output.setMediaType(new MediaType(mediaType));
                        }
                    }
                } catch (SQLException sqe) {
                    this.getLogger().throwing("XdbServletWarClientHelper",
                                              "handleWar", sqe);
                    throw new RuntimeException(
                   "Exception querying xdburitype(?).getBlob() - xdbResPath: "
                   + xdbResPath, sqe);
                } finally {
                    XdbServerServlet.closeDbResources(stmt, rset);
                }
            }

            response.setEntity(output);
            response.setStatus(Status.SUCCESS_OK);
        } else {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.GET);
            response.getAllowedMethods().add(Method.HEAD);
        }
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try {
            stmt = conn.prepareStatement("select USER from dual");
            rset = stmt.executeQuery();
            if (rset.next()) {
                connectedUser = rset.getString(1);
            } else {
                connectedUser = "PUBLIC";
            }
            this.getLogger().info("efective user is: " + connectedUser);
        } catch (SQLException sqe) {
            this.getLogger().throwing("XdbServletWarClientHelper", "start",
                                      sqe);
            throw new RuntimeException("Exception querying USER from dual ",
                                       sqe);
        } finally {
            XdbServerServlet.closeDbResources(stmt, rset);
        }
    }
}
