/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.xdb;

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

import com.noelios.restlet.ext.servlet.ServletWarClientHelper;

/**
 * Local client connector based on a XMLDB repository. WARs are deployed at
 * XMLDB repository using this directory layout /home/[SCHEMA]/wars/[AppName]/
 * where SCHEMA: is a database user, for example SCOTT AppName: is a Servlet
 * Name configured with XdbServerServlet adapter Note: For Servlet running with
 * PUBLIC grants run with an effective user ANONYMOUS so WARs deployment will be
 * located at /home/ANONYMOUS/wars
 * 
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 */
public class XdbServletWarClientHelper extends ServletWarClientHelper {
    /**
     * XMLDB base directory used to access to resources
     */
    private static final String BASE_DIR = "/home/";

    /**
     * XMLDB directory appended to connectedUser where WARs are deployed final
     * directory for WARs will be: /home/SCOTT/wars/HelloRestlet/ for example.
     */
    private static final String DEPLOY_DIR = "/wars/";

    /** The Servlet Config to use. */
    private volatile ServletConfig config;

    /**
     * Efective user who is running XdbServerServlet for example SCOTT or
     * ANONYMOUS if is a Servlet running with PUBLIC grants (no http
     * auhtorization is required)
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
     *            The client to help.
     * @param config
     *            The Servlet Config
     * @param conn
     *            The JDBC Connection
     */
    public XdbServletWarClientHelper(Client client, ServletConfig config,
            Connection conn) {
        super(client, config.getServletContext());
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

    @Override
    public void handle(Request request, Response response) {
        PreparedStatement stmt = null;
        ResultSet rset = null;

        if (request.getMethod().equals(Method.GET)
                || request.getMethod().equals(Method.HEAD)) {
            final String basePath = request.getResourceRef().getPath();
            final int lastSlashIndex = basePath.lastIndexOf('/');
            String entry = (lastSlashIndex == -1) ? basePath : basePath
                    .substring(lastSlashIndex + 1);
            Representation output = null;
            final String xdbResPath = BASE_DIR + this.connectedUser
                    + DEPLOY_DIR + this.config.getServletName() + basePath;

            if (basePath.endsWith("/")) {
                // Return the directory listing
                try {
                    stmt = this.conn
                            .prepareStatement("SELECT path(1),extractValue(res,'/Resource/@Container') "
                                    + "FROM resource_view WHERE under_path(res,1,?,1 ) = 1");
                    getLogger().info("looking resources at: " + xdbResPath);
                    stmt.setString(1, xdbResPath);
                    rset = stmt.executeQuery();
                    if (rset.next()) {
                        final ReferenceList rl = new ReferenceList();
                        rl.setIdentifier(request.getResourceRef());

                        while (rset.next()) {
                            entry = rset.getString(1)
                                    + (("true".equalsIgnoreCase(rset
                                            .getString(2))) ? "/" : "");
                            getLogger().info("Reference: " + basePath + entry);
                            rl.add(new Reference(basePath + entry));
                        }

                        output = rl.getTextRepresentation();
                    }
                } catch (final SQLException sqe) {
                    getLogger().throwing("XdbServletWarClientHelper",
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
                    stmt = this.conn
                            .prepareStatement("select xdburitype(?).getBlob(),"
                                    + "xdburitype(?).getContentType() "
                                    + "from dual");
                    stmt.setString(1, xdbResPath);
                    stmt.setString(2, xdbResPath);
                    getLogger().info("looking resources at: " + xdbResPath);
                    rset = stmt.executeQuery();
                    if (rset.next()) {
                        final Blob blob = (Blob) rset.getObject(1);
                        final String mediaType = rset.getString(2);
                        is = blob.getBinaryStream();
                        final MetadataService metadataService = getMetadataService(request);
                        output = new InputRepresentation(is, metadataService
                                .getDefaultMediaType());
                        output.setIdentifier(request.getResourceRef());
                        updateMetadata(metadataService, entry, output);

                        // See if the Servlet context specified
                        // a particular Mime Type
                        if (mediaType != null) {
                            getLogger().info("mediaType: " + mediaType);
                            output.setMediaType(new MediaType(mediaType));
                        }
                    }
                } catch (final SQLException sqe) {
                    getLogger().throwing("XdbServletWarClientHelper",
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
            stmt = this.conn.prepareStatement("select USER from dual");
            rset = stmt.executeQuery();
            if (rset.next()) {
                this.connectedUser = rset.getString(1);
            } else {
                this.connectedUser = "PUBLIC";
            }
            getLogger().info("efective user is: " + this.connectedUser);
        } catch (final SQLException sqe) {
            getLogger().throwing("XdbServletWarClientHelper", "start", sqe);
            throw new RuntimeException("Exception querying USER from dual ",
                    sqe);
        } finally {
            XdbServerServlet.closeDbResources(stmt, rset);
        }
    }
}
