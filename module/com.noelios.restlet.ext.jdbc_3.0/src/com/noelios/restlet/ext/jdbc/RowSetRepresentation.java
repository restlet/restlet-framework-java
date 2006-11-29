/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.jdbc;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.sql.rowset.WebRowSet;

import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import com.sun.rowset.WebRowSetImpl;

/**
 * XML Representation of a ResultSet instance wrapped either in a JdbcResult
 * instance or in a WebRowSet. Leverage the WebRowSet API to create the Response
 * entity.<br/> Give access the JdbcResult instance and to the WebRowSet for
 * retrieval of the connected ResultSet in the same JVM (for advanced use
 * cases).<br/>
 * 
 * @see <a
 *      href="http://java.sun.com/j2se/1.5.0/docs/api/javax/sql/rowset/WebRowSet.html">WebRowSet
 *      Interface</a>
 * @author Thierry Boileau
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RowSetRepresentation extends OutputRepresentation {
	/**
	 * Creates a WebRowSet from a JdbcResult.
	 * 
	 * @param jdbcResult
	 *            The JdbcResult instance to wrap.
	 * @return A WebRowSet from a JdbcResult.
	 * @throws SQLException
	 */
	private static WebRowSet create(JdbcResult jdbcResult) throws SQLException {
		WebRowSet result = new WebRowSetImpl();

		if (jdbcResult.getResultSet() != null) {
			result.populate(jdbcResult.getResultSet());
		}

		return result;
	}

	/** Inner WebRowSet Instance. */
	private WebRowSet webRowSet;

	/** JdbcResult instance that gives access to the resultSet. */
	private JdbcResult jdbcResult;

	/**
	 * Constructor.
	 * 
	 * @param jdbcResult
	 *            The inner JdbcResult.
	 * @throws SQLException
	 */
	public RowSetRepresentation(JdbcResult jdbcResult) throws SQLException {
		this(create(jdbcResult));
		this.jdbcResult = jdbcResult;
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
		return jdbcResult;
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
			webRowSet.writeXml(outputStream);
		} catch (SQLException se) {
			throw new IOException(se.getMessage());
		}
	}
}
