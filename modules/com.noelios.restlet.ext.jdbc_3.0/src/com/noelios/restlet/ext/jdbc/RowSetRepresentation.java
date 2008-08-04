/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.jdbc;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.WebRowSet;

import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import com.sun.rowset.WebRowSetImpl;

/**
 * XML Representation of a ResultSet instance wrapped either in a JdbcResult
 * instance or in a WebRowSet. Leverage the WebRowSet API to create the Response
 * entity.<br>
 * Give access to the JdbcResult instance and to the WebRowSet for retrieval of
 * the connected ResultSet in the same JVM (for advanced use cases).
 * 
 * @see <a href=
 *      "http://java.sun.com/j2se/1.5.0/docs/api/javax/sql/rowset/WebRowSet.html"
 *      >WebRowSet Interface< /a>
 * @author Thierry Boileau
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RowSetRepresentation extends OutputRepresentation {
    /**
     * Creates a WebRowSet from a ResultSet.
     * 
     * @param resultSet
     *            The result set to use to populate the Web row set.
     * @return A WebRowSet from a ResultSet.
     * @throws SQLException
     */
    private static WebRowSet create(ResultSet resultSet) throws SQLException {
        final WebRowSet result = new WebRowSetImpl();

        if (resultSet != null) {
            result.populate(resultSet);
        }

        return result;
    }

    /** JdbcResult instance that gives access to the resultSet. */
    private volatile JdbcResult jdbcResult;

    /** Inner WebRowSet Instance. */
    private volatile WebRowSet webRowSet;

    /**
     * Constructor.
     * 
     * @param jdbcResult
     *            The inner JdbcResult.
     * @throws SQLException
     */
    public RowSetRepresentation(JdbcResult jdbcResult) throws SQLException {
        this(create((jdbcResult == null) ? null : jdbcResult.getResultSet()));
        this.jdbcResult = jdbcResult;
    }

    /**
     * Constructor.
     * 
     * @param resultSet
     *            The result set to use to populate the Web row set.
     * @throws SQLException
     */
    public RowSetRepresentation(ResultSet resultSet) throws SQLException {
        this(create(resultSet));
    }

    /**
     * Constructor.
     * 
     * @param webRowSet
     *            The inner WebRowSet.
     */
    public RowSetRepresentation(WebRowSet webRowSet) {
        super(MediaType.TEXT_XML);
        this.webRowSet = webRowSet;
    }

    /**
     * Returns the inner JdbcResult instance or null.
     * 
     * @return The inner JdbcResult instance or null.
     */
    public JdbcResult getJdbcResult() {
        return this.jdbcResult;
    }

    /**
     * Returns the inner WebRowSet instance.
     * 
     * @return The inner WebRowSet instance.
     */
    public WebRowSet getWebRowSet() {
        return this.webRowSet;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            this.webRowSet.writeXml(outputStream);
        } catch (final SQLException se) {
            throw new IOException(se.getMessage());
        }

        try {
            if (this.jdbcResult != null) {
                this.jdbcResult.release();
            }
        } catch (final SQLException se) {
            throw new IOException(
                    "SQL exception while releasing the JdbcResult instance after writing the representation. "
                            + se.getMessage());
        }

        try {
            if (this.webRowSet != null) {
                this.webRowSet.release();
                this.webRowSet.close();
            }
        } catch (final SQLException se) {
            throw new IOException(
                    "Error while releasing the WebRowSet instance after writing the representation. "
                            + se.getMessage());
        }
    }
}
