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

package org.restlet.ext.jdbc;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.sql.rowset.WebRowSet;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;

/**
 * XML Representation of a ResultSet instance wrapped either in a JdbcResult
 * instance or in a WebRowSet. Leverage the WebRowSet API to create the Response
 * entity.<br>
 * Give access to the JdbcResult instance and to the WebRowSet for retrieval of
 * the connected ResultSet in the same JVM (for advanced use cases).
 * 
 * @see <a href=
 *      "http://java.sun.com/j2se/1.5.0/docs/api/javax/sql/rowset/WebRowSet.html"
 *      >WebRowSet Interface</a>
 * @author Thierry Boileau
 * @author Jerome Louvel
 */
public class RowSetRepresentation extends WriterRepresentation {
    /**
     * Creates a WebRowSet from a ResultSet.
     * 
     * @param resultSet
     *            The result set to use to populate the Web row set.
     * @param start
     *            The start index of the page or 0 for the first result.
     * @param limit
     *            The page size or -1 if no limit is set.
     * @return A WebRowSet from a ResultSet.
     * @throws SQLException
     */
    private static WebRowSet create(ResultSet resultSet, int start, int limit)
            throws SQLException {
        WebRowSet result = null;

        try {
            result = (WebRowSet) Class.forName("com.sun.rowset.WebRowSetImpl")
                    .newInstance();
        } catch (Throwable t) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to instantiate the Sun's WebRowSet implementation",
                    t);
        }

        if (resultSet != null) {
            if (limit > -1) {
                result.setPageSize(limit);
                result.setMaxRows(limit);
            }

            result.populate(resultSet, start < 0 ? 1 : start + 1);
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
        this(jdbcResult, -1, -1);
    }

    /**
     * Constructor with paging.
     * 
     * @param jdbcResult
     *            The inner JdbcResult.
     * @param start
     *            The start index of the page or 0 for the first result.
     * @param limit
     *            The page size or -1 if no limit is set.
     * @throws SQLException
     */
    public RowSetRepresentation(JdbcResult jdbcResult, int start, int limit)
            throws SQLException {
        this(create((jdbcResult == null) ? null : jdbcResult.getResultSet(),
                start, limit));
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
        this(resultSet, -1, -1);
    }

    /**
     * Constructor with paging.
     * 
     * @param resultSet
     *            The result set to use to populate the Web row set.
     * @param start
     *            The start index of the page or 1 for the first result.
     * @param limit
     *            The page size or -1 if no limit is set.
     * @throws SQLException
     */
    public RowSetRepresentation(ResultSet resultSet, int start, int limit)
            throws SQLException {
        this(create(resultSet, start, limit));
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
    public void write(Writer writer) throws IOException {
        try {
            this.webRowSet.writeXml(writer);
        } catch (SQLException se) {
            throw new IOException(se.getMessage());
        }

        try {
            if (this.jdbcResult != null) {
                this.jdbcResult.release();
            }
        } catch (SQLException se) {
            throw new IOException(
                    "SQL exception while releasing the JdbcResult instance after writing the representation. "
                            + se.getMessage());
        }

        try {
            if (this.webRowSet != null) {
                this.webRowSet.release();
                this.webRowSet.close();
            }
        } catch (SQLException se) {
            throw new IOException(
                    "Error while releasing the WebRowSet instance after writing the representation. "
                            + se.getMessage());
        }
    }
}
