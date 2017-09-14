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

package org.restlet.ext.jdbc;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC result wrapper. Used by the JDBC client connector as a response entity
 * of JDBC calls.
 * 
 * @author Jerome Louvel
 * @deprecated Use a persistence technology such as Mybatis or Hibernate
 *             instead.
 */
@Deprecated
public class JdbcResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The JDBC statement. */
    private volatile transient Statement statement;

    /**
     * Constructor.
     * 
     * @param statement
     *            The JDBC statement.
     */
    public JdbcResult(Statement statement) {
        this.statement = statement;
    }

    /**
     * Returns the generated keys.
     * 
     * @return The generated keys.
     * @throws SQLException
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        return this.statement.getGeneratedKeys();
    }

    /**
     * Returns the result set.
     * 
     * @return The result set.
     * @throws SQLException
     */
    public ResultSet getResultSet() throws SQLException {
        return this.statement.getResultSet();
    }

    /**
     * Returns the update count.
     * 
     * @return The update count.
     * @throws SQLException
     */
    public int getUpdateCount() throws SQLException {
        return this.statement.getUpdateCount();
    }

    /**
     * Release the statement connection. To call when result navigation is done.
     * 
     * @throws SQLException
     */
    public void release() throws SQLException {
        // One connection per jdbcResult
        // releasing the instance means releasing the connection too
        // and not only the statement.
        this.statement.getConnection().close();
    }

}
