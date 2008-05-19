/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet.ext.jdbc;

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
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.noelios.restlet.ClientHelper;
import com.noelios.restlet.Engine;

/**
 * Client connector to a JDBC database.<br/> To send a request to the server,
 * create a new instance of a client supporting the JDBC Protocol and invoke the
 * handle() method.<br/> Alternatively, you can create a new Call with the JDBC
 * URI as the resource reference and use an XML request as the entity.<br/><br/>
 * Database connections are optionally pooled using Apache Commons DBCP. In this
 * case, a different connection pool is created for each unique combination of
 * JDBC URI and connection properties.<br/><br/> Do not forget to register
 * your JDBC drivers before using this client. See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/sql/DriverManager.html">
 * JDBC DriverManager API</a> for details<br/><br/> Sample XML request:<br/><br/>
 * {@code <?xml version="1.0" encoding="ISO-8859-1" ?>}<br/> {@code <request>}<br/>
 * &nbsp;&nbsp;{@code <header>}<br/> &nbsp;&nbsp;&nbsp;&nbsp;{@code <connection>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <usePooling>true</usePooling>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="user">scott</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="password">tiger</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="...">1234</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="...">true</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code </connection>}<br/> &nbsp;&nbsp;&nbsp;&nbsp;{@code <returnGeneratedKeys>true</returnGeneratedKeys>}<br/>
 * &nbsp;&nbsp;{@code </header>}<br/> &nbsp;&nbsp;{@code <body>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <statement>UPDATE myTable SET myField1="value1" </statement>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <statement>SELECT msField1, myField2 FROM myTable</statement>}<br/>
 * &nbsp;&nbsp;{@code </body>}<br/> {@code </request>}<br/><br/>Several SQL
 * Statements can be specified.<br/> A RowSetRepresentation of the last
 * correctly executed SQL request is returned to the Client.</br>
 * 
 * @see com.noelios.restlet.ext.jdbc.RowSetRepresentation
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @author Thierry Boileau
 */
public class JdbcClientHelper extends ClientHelper {
    /** Map of connection factories. */
    private List<ConnectionSource> connectionSources;

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
                Document requestDoc = docBuilder.parse(request.getEntity()
                        .getStream());

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

                // Read the connection properties
                NodeList propertyNodes = connectionElt
                        .getElementsByTagName("property");
                Node propertyNode = null;
                Properties properties = null;
                String name = null;
                String value = null;
                for (int i = 0; i < propertyNodes.getLength(); i++) {
                    propertyNode = propertyNodes.item(i);

                    if (properties == null)
                        properties = new Properties();
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
                response.setEntity(new RowSetRepresentation(result));

            } catch (SQLException se) {
                getLogger().log(Level.WARNING,
                        "Error while processing the SQL request", se);
                response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
                        "Error while processing the SQL request"));
            } catch (ParserConfigurationException pce) {
                getLogger().log(Level.WARNING,
                        "Error with XML parser configuration", pce);
                response.setStatus(new Status(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Error with XML parser configuration"));
            } catch (SAXException se) {
                getLogger().log(Level.WARNING,
                        "Error while parsing the XML document", se);
                response.setStatus(new Status(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Error while parsing the XML document"));
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING, "Input/Output exception", ioe);
                response.setStatus(new Status(Status.SERVER_ERROR_INTERNAL,
                        "Input/Output exception"));
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
            for (ConnectionSource c : connectionSources) {
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

    /**
     * Creates a connection pool for a given connection configuration.
     * 
     * @param uri
     *            The connection URI.
     * @param properties
     *            The connection properties.
     * @return The new connection pool.
     */
    protected static ObjectPool createConnectionPool(String uri,
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
     * Pooling data source which remembers its connection properties and URI.
     */
    private static class ConnectionSource extends PoolingDataSource {
        /** The connection URI. */
        protected String uri;

        /** The connection properties. */
        protected Properties properties;

        /**
         * Constructor.
         * 
         * @param uri
         *            The connection URI.
         * @param properties
         *            The connection properties.
         */
        public ConnectionSource(String uri, Properties properties) {
            super(createConnectionPool(uri, properties));
            this.uri = uri;
            this.properties = properties;
        }

        /**
         * Returns the connection URI.
         * 
         * @return The connection URI.
         */
        public String getUri() {
            return uri;
        }

        /**
         * Returns the connection properties.
         * 
         * @return The connection properties.
         */
        public Properties getProperties() {
            return properties;
        }
    }
}
