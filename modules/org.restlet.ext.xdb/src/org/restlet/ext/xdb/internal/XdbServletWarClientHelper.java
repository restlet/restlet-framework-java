/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.xdb.internal;

import java.io.InputStream;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletConfig;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Status;
import org.restlet.engine.local.Entity;
import org.restlet.ext.servlet.internal.ServletWarClientHelper;
import org.restlet.ext.xdb.XdbServerServlet;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

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
     * XMLDB base directory used to access to user resources URL:
     * file:///$HOME/myPath/myFile.ext XDB: /home/USER/myPath/myFile.ext
     */
    private static final String HOME_DIR = "/$HOME";

    /**
     * XMLDB base directory used to access to deployed resources as WAR. URL:
     * war:///myPath/myFile.ext XDB:
     * /home/USER/wars/HelloRestlet/myPath/myFile.ext
     */
    private static final String USER_DIR = "/home/";

    /**
     * XMLDB path directory used to access to deployed resources as WAR. URL:
     * war:///myPath/myFile.ext XDB:
     * /home/USER/wars/HelloRestlet/myPath/myFile.ext
     */
    private static final String DEPLOY_DIR = "/wars/";

    /** The Servlet Config to use. */
    private volatile ServletConfig config;

    /**
     * SQL Connection to XMLDB repository
     */
    private volatile Connection conn = null;

    /**
     * Efective user who is running XdbServerServlet for example SCOTT or
     * ANONYMOUS if is a Servlet running with PUBLIC grants (no http
     * auhtorization is required)
     */
    private volatile String connectedUser;

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
    public void handleLocal(Request request, Response response,
            String decodedPath) {
        PreparedStatement stmt = null;
        ResultSet rset = null;

        if (request.getMethod().equals(Method.GET)
                || request.getMethod().equals(Method.HEAD)) {
            final String basePath = request.getResourceRef().getPath();
            final String xdbResPath;
            if (request.getProtocol().equals(Protocol.FILE)) {
                xdbResPath = (basePath.startsWith(HOME_DIR)) ? USER_DIR
                        + this.connectedUser
                        + basePath.substring(HOME_DIR.length()) : basePath;
            } else {
                xdbResPath = USER_DIR + this.connectedUser + DEPLOY_DIR
                        + this.config.getServletName() + basePath;
            }
            final int lastSlashIndex = basePath.lastIndexOf('/');
            String entry = (lastSlashIndex == -1) ? basePath : basePath
                    .substring(lastSlashIndex + 1);
            Representation output = null;

            if (basePath.endsWith("/")) {
                // Return the directory listing
                try {
                    stmt = this.conn
                            .prepareStatement("SELECT path(1),extractValue(res,'/Resource/@Container') "
                                    + "FROM resource_view WHERE under_path(res,1,?,1 ) = 1");
                    getLogger().info("looking resources at: " + xdbResPath);
                    stmt.setString(1, xdbResPath);
                    rset = stmt.executeQuery();
                    final ReferenceList rl = new ReferenceList();
                    final String baseUri = request.getResourceRef().getScheme()
                            + "://" + basePath;
                    rl.setIdentifier(request.getResourceRef());

                    while (rset.next()) {
                        entry = rset.getString(1)
                                + (("true".equalsIgnoreCase(rset.getString(2))) ? "/"
                                        : "");
                        getLogger().info("Reference: " + baseUri + entry);
                        rl.add(new Reference(baseUri + entry));
                    }

                    output = rl.getTextRepresentation();
                } catch (SQLException sqe) {
                    getLogger().throwing("XdbServletWarClientHelper",
                            "handleWar", sqe);
                    throw new RuntimeException(
                            "Exception querying resource_view - xdbResPath: "
                                    + xdbResPath, sqe);
                } finally {
                    XdbServerServlet.closeDbResources(stmt, rset);
                }

                response.setEntity(output);
                response.setStatus(Status.SUCCESS_OK);
            } else {
                // Return the entry content
                try {
                    stmt = this.conn
                            .prepareStatement("select xdbURIType(any_path).getBlob(),"
                                    + "extractValue(res,'/Resource/ModificationDate'),"
                                    + "extractValue(res,'/Resource/ContentType') "
                                    + "from resource_view where equals_path(res,?)=1");
                    stmt.setString(1, xdbResPath);
                    getLogger().info("looking resources at: " + xdbResPath);
                    rset = stmt.executeQuery();
                    if (rset.next()) {
                        final Blob blob = (Blob) rset.getObject(1);
                        final Timestamp modTime = rset.getTimestamp(2);
                        final String mediaType = rset.getString(3);
                        if (blob != null) {
                            InputStream is = null;
                            is = blob.getBinaryStream();
                            final MetadataService metadataService = getMetadataService();
                            output = new InputRepresentation(is,
                                    metadataService.getDefaultMediaType());
                            output.setLocationRef(request.getResourceRef());
                            Entity.updateMetadata(entry, output, true,
                                    getMetadataService());

                            // See if the Servlet context specified
                            // a particular MIME Type
                            if (mediaType != null) {
                                getLogger().info("mediaType: " + mediaType);
                                getLogger().info("modTime: " + modTime);
                                output.setMediaType(new MediaType(mediaType));
                                output.setModificationDate(new Date(modTime
                                        .getTime()));
                            }
                            response.setEntity(output);
                            response.setStatus(Status.SUCCESS_OK);
                        } else {
                            // Blob content is null, sure is an Schema based
                            // resource
                            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        }
                    }

                } catch (SQLException sqe) {
                    if (sqe.getErrorCode() == 31001) {
                        // ORA-31001: Invalid resource handle or path name
                        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    } else {
                        getLogger().throwing("XdbServletWarClientHelper",
                                "handleWar: ", sqe);
                        throw new RuntimeException(
                                "Exception querying xdburitype(?).getBlob() - xdbResPath: "
                                        + xdbResPath, sqe);
                    }
                } finally {
                    XdbServerServlet.closeDbResources(stmt, rset);
                }
            }
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
        } catch (SQLException sqe) {
            getLogger().throwing("XdbServletWarClientHelper", "start", sqe);
            throw new RuntimeException("Exception querying USER from dual ",
                    sqe);
        } finally {
            XdbServerServlet.closeDbResources(stmt, rset);
        }
    }
}
