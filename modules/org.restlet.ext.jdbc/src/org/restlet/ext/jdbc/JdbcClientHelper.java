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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.ClientHelper;
import org.restlet.engine.Engine;
import org.restlet.ext.jdbc.internal.ConnectionSource;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Client connector to a JDBC database. To send a request to the server, create
 * a new instance of a client supporting the JDBC Protocol and invoke the
 * handle() method. Alternatively, you can create a new {@link Request} with the
 * JDBC URI as the resource reference and use an XML request as the entity.
 * <p>
 * Database connections are optionally pooled using Apache Commons DBCP. In this
 * case, a different connection pool is created for each unique combination of
 * JDBC URI and connection properties.
 * <p>
 * Paging is supported via two header elements: "start" for the index of the
 * first result (0 by default) and "limit" for the maximum number of results
 * retrieved (unlimited by default).
 * <p>
 * Do not forget to register your JDBC drivers before using this client. See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/sql/DriverManager.html">
 * JDBC DriverManager API</a> for details.
 * <p>
 * Sample XML request:<br>
 * <br>
 * {@code <?xml version="1.0" encoding="ISO-8859-1" ?>}<br>
 * {@code <request>}<br>
 * &nbsp;&nbsp;{@code <header>}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <connection>}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <usePooling>true</usePooling>}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property
 * name="user">scott</property >}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property
 * name="password">tiger</property >}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property
 * name="...">1234</property >}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property
 * name="...">true</property >}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code </connection>}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <start>10</start>}<br>
 * <limit>20</limit>}<br>
 * <returnGeneratedKeys>true</returnGeneratedKeys>}<br>
 * &nbsp;&nbsp;{@code </header>}<br>
 * &nbsp;&nbsp;{@code <body>}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <statement>UPDATE myTable SET
 * myField1="value1" </statement>}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <statement>SELECT msField1, myField2 FROM
 * myTable</statement>}<br>
 * &nbsp;&nbsp;{@code </body>}<br>
 * {@code </request>}
 * <p>
 * Several SQL Statements can be specified. A RowSetRepresentation of the last
 * correctly executed SQL request is returned to the Client.
 * 
 * @see org.restlet.ext.jdbc.RowSetRepresentation
 * 
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public class JdbcClientHelper extends ClientHelper {
	/**
	 * Creates an uniform call.
	 * 
	 * @param jdbcURI
	 *            The database's JDBC URI (ex:
	 *            jdbc:mysql://[hostname]/[database]).
	 * @param request
	 *            The request to send (valid XML request).
	 */
	public static Request create(String jdbcURI, Representation request) {
		Request result = new Request();
		result.getClientInfo().setAgent(Engine.VERSION_HEADER);
		result.setMethod(Method.POST);
		result.setResourceRef(jdbcURI);
		result.setEntity(request);
		return result;
	}

	/**
	 * Creates a connection pool for a given connection configuration.
	 * 
	 * @param uri
	 *            The connection URI.
	 * @param properties
	 *            The connection properties.
	 * @return The new connection pool.
	 */
	public static ObjectPool createConnectionPool(String uri,
			Properties properties) {
		// Create an ObjectPool that will serve as the actual pool of
		// connections
		ObjectPool result = new GenericObjectPool(null);

		// Create a ConnectionFactory that the pool will use to create
		// Connections
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
				uri, properties);

		// Create the PoolableConnectionFactory, which wraps the "real"
		// Connections created by the ConnectionFactory with
		// the classes that implement the pooling functionality.
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
				connectionFactory, result, null, null, false, false);

		// To remove warnings
		poolableConnectionFactory.getPool();

		return result;
	}

	/**
	 * Escapes quotes in a SQL query.
	 * 
	 * @param query
	 *            The SQL query to escape.
	 * @return The escaped SQL query.
	 */
	public static String sqlEncode(String query) {
		StringBuilder result = new StringBuilder(query.length() + 10);
		char currentChar;

		for (int i = 0; i < query.length(); i++) {
			currentChar = query.charAt(i);
			if (currentChar == '\'') {
				result.append("''");
			} else {
				result.append(currentChar);
			}
		}

		return result.toString();
	}

	/** Map of connection factories. */
	private volatile List<ConnectionSource> connectionSources;

	/**
	 * Constructor.
	 * 
	 * @param client
	 *            The client to help.
	 */
	public JdbcClientHelper(Client client) {
		super(client);

		getProtocols().add(Protocol.JDBC);

		// Set up the list of factories
		this.connectionSources = new ArrayList<ConnectionSource>();
	}

	/**
	 * Returns a JDBC connection.
	 * 
	 * @param uri
	 *            The connection URI.
	 * @param properties
	 *            The connection properties.
	 * @param usePooling
	 *            Indicates if the connection pooling should be used.
	 * @return The JDBC connection.
	 * @throws SQLException
	 */
	protected Connection getConnection(String uri, Properties properties,
			boolean usePooling) throws SQLException {
		Connection result = null;

		if (usePooling) {
			for (ConnectionSource c : this.connectionSources) {
				// Check if the connection URI is identical
				// and if the same number of properties is present
				if ((result == null) && c.getUri().equalsIgnoreCase(uri)
						&& (properties.size() == c.getProperties().size())) {
					// Check that the properties tables are equivalent
					boolean equal = true;
					for (Object key : c.getProperties().keySet()) {
						if (equal && properties.containsKey(key)) {
							equal = equal
									&& (properties.get(key).equals(c
											.getProperties().get(key)));
						} else {
							equal = false;
						}
					}

					if (equal) {
						result = c.getConnection();
					}
				}
			}

			if (result == null) {
				// No existing connection source found
				ConnectionSource cs = new ConnectionSource(uri, properties);
				this.connectionSources.add(cs);
				result = cs.getConnection();
			}
		} else {
			result = DriverManager.getConnection(uri, properties);
		}

		return result;
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
		Connection connection = null;

		if (request.getMethod().equals(Method.POST)) {
			try {
				// Parse the JDBC URI
				String connectionURI = request.getResourceRef().toString();

				// Parse the request to extract necessary info
				DocumentBuilder docBuilder = DocumentBuilderFactory
						.newInstance().newDocumentBuilder();
				Document requestDoc = docBuilder.parse(new InputSource(request
						.getEntity().getReader()));

				Element rootElt = (Element) requestDoc.getElementsByTagName(
						"request").item(0);
				Element headerElt = (Element) rootElt.getElementsByTagName(
						"header").item(0);
				Element connectionElt = (Element) headerElt
						.getElementsByTagName("connection").item(0);

				// Read the connection pooling setting
				Node usePoolingNode = connectionElt.getElementsByTagName(
						"usePooling").item(0);
				boolean usePooling = usePoolingNode.getTextContent().equals(
						"true") ? true : false;

				// Read the paging setting
				Node startNode = headerElt.getElementsByTagName("start")
						.item(0);
				int start = startNode != null
						&& startNode.getTextContent().trim().length() > 0 ? Integer
						.parseInt(startNode.getTextContent())
						: 0;

				Node limitNode = headerElt.getElementsByTagName("limit")
						.item(0);
				int limit = limitNode != null
						&& limitNode.getTextContent().trim().length() > 0 ? Integer
						.parseInt(limitNode.getTextContent())
						: -1;

				// Read the connection properties
				NodeList propertyNodes = connectionElt
						.getElementsByTagName("property");
				Node propertyNode = null;
				Properties properties = null;
				String name = null;
				String value = null;
				for (int i = 0; i < propertyNodes.getLength(); i++) {
					propertyNode = propertyNodes.item(i);

					if (properties == null) {
						properties = new Properties();
					}
					name = propertyNode.getAttributes().getNamedItem("name")
							.getTextContent();
					value = propertyNode.getTextContent();
					properties.setProperty(name, value);
				}

				Node returnGeneratedKeysNode = headerElt.getElementsByTagName(
						"returnGeneratedKeys").item(0);
				boolean returnGeneratedKeys = returnGeneratedKeysNode
						.getTextContent().equals("true") ? true : false;

				// Read the SQL body and get the list of sql statements
				Element bodyElt = (Element) rootElt
						.getElementsByTagName("body").item(0);
				NodeList statementNodes = bodyElt
						.getElementsByTagName("statement");
				List<String> sqlRequests = new ArrayList<String>();
				for (int i = 0; i < statementNodes.getLength(); i++) {
					String sqlRequest = statementNodes.item(i).getTextContent();
					sqlRequests.add(sqlRequest);
				}

				// Execute the List of SQL requests
				connection = getConnection(connectionURI, properties,
						usePooling);
				JdbcResult result = handleSqlRequests(connection,
						returnGeneratedKeys, sqlRequests);
				response.setEntity(new RowSetRepresentation(result, start,
						limit));
			} catch (SQLException se) {
				getLogger().log(Level.WARNING,
						"Error while processing the SQL request", se);
				response.setStatus(Status.SERVER_ERROR_INTERNAL, se);
			} catch (ParserConfigurationException pce) {
				getLogger().log(Level.WARNING,
						"Error with XML parser configuration", pce);
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, pce);
			} catch (SAXException se) {
				getLogger().log(Level.WARNING,
						"Error while parsing the XML document", se);
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, se);
			} catch (IOException ioe) {
				getLogger().log(Level.WARNING, "Input/Output exception", ioe);
				response.setStatus(Status.SERVER_ERROR_INTERNAL, ioe);
			}
		} else {
			throw new IllegalArgumentException(
					"Only the POST method is supported");
		}
	}

	/**
	 * Helper
	 * 
	 * @param connection
	 * @param returnGeneratedKeys
	 * @param sqlRequests
	 * @return the result of the last executed SQL request
	 */
	private JdbcResult handleSqlRequests(Connection connection,
			boolean returnGeneratedKeys, List<String> sqlRequests) {
		JdbcResult result = null;
		try {
			connection.setAutoCommit(true);
			Statement statement = connection.createStatement();
			for (String sqlRequest : sqlRequests) {
				statement.execute(sqlRequest,
						returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS
								: Statement.NO_GENERATED_KEYS);
				result = new JdbcResult(statement);
			}

			// Commit any changes to the database
			if (!connection.getAutoCommit()) {
				connection.commit();
			}
		} catch (SQLException se) {
			getLogger().log(Level.WARNING,
					"Error while processing the SQL requests", se);
			try {
				if (!connection.getAutoCommit()) {
					connection.rollback();
				}
			} catch (SQLException se2) {
				getLogger().log(Level.WARNING,
						"Error while rollbacking the transaction", se);
			}
		}
		return result;

	}
}
