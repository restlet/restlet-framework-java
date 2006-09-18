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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.restlet.Call;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.noelios.restlet.data.ObjectRepresentation;
import com.noelios.restlet.impl.Factory;
import com.noelios.restlet.impl.connector.ClientImpl;

/**
 * Client connector to a JDBC database. To send a request to the server create a new instance of
 * JdbcCall and invoke the handle() method. Alteratively you can create a new Call with the JDBC URI as the
 * resource reference and use an XML request as the input representation.<br/><br/> Database connections are
 * optionally pooled using Apache Commons DBCP. In this case, a different connection pool is created for each
 * unique combination of JDBC URI and connection properties.<br/><br/> Do not forget to register your JDBC
 * drivers before using this client. See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/sql/DriverManager.html"> JDBC DriverManager API</a> for
 * details<br/> <br/> Sample XML request:<br/> <br/> {@code <?xml version="1.0" encoding="ISO-8859-1" ?>}<br/>
 * {@code <request>}<br/> &nbsp;&nbsp;{@code <header>}<br/> &nbsp;&nbsp;&nbsp;&nbsp;{@code <connection>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <usePooling>true</usePooling>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="user">scott</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="password">tiger</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="...">1234</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <property name="...">true</property >}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code </connection>}<br/> &nbsp;&nbsp;&nbsp;&nbsp;{@code <returnGeneratedKeys>true</returnGeneratedKeys>}<br/>
 * &nbsp;&nbsp;{@code </header>}<br/> &nbsp;&nbsp;{@code <body>}<br/> &nbsp;&nbsp;&nbsp;&nbsp;{@code SELECT * FROM customers}<br/>
 * &nbsp;&nbsp;{@code </body>}<br/> {@code </request>}
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class JdbcClient extends ClientImpl
{
   /** Map of connection factories. */
	private List<ConnectionSource> connectionSources;
   
   /**
    * Constructor.
    */
   public JdbcClient()
   {
   	getProtocols().add(Protocol.JDBC);

      // Set up the list of factories
      this.connectionSources = new ArrayList<ConnectionSource>();
   }
     
   /**
    * Creates an uniform call.
    * @param jdbcURI The database's JDBC URI (ex: jdbc:mysql://[hostname]/[database]).
    * @param request The request to send (valid XML request).
    */
   public static Call create(String jdbcURI, Representation request)
   {
      Call result = new Call();
      result.getClient().setName(Factory.VERSION_HEADER);
      result.setMethod(Method.POST);
      result.setResourceRef(jdbcURI);
      result.setInput(request);
      return result;
   }

   /**
    * Handles a REST call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      Connection connection = null;

      try
      {
  			if(!isStarted()) start();

  			// Parse the JDBC URI
         String connectionURI = call.getResourceRef().toString();

         // Parse the request to extract necessary info
         DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document request = docBuilder.parse(call.getInput().getStream());

         Element rootElt = (Element)request.getElementsByTagName("request").item(0);
         Element headerElt = (Element)rootElt.getElementsByTagName("header").item(0);
         Element connectionElt = (Element)headerElt.getElementsByTagName("connection").item(0);

         // Read the connection pooling setting
         Node usePoolingNode = connectionElt.getElementsByTagName("usePooling").item(0);
         boolean usePooling = usePoolingNode.getTextContent().equals("true") ? true : false;

         // Read the connection properties
         NodeList propertyNodes = connectionElt.getElementsByTagName("property");
         Node propertyNode = null;
         Properties properties = null;
         String name = null;
         String value = null;
         for(int i = 0; i < propertyNodes.getLength(); i++)
         {
            propertyNode = propertyNodes.item(i);

            if(properties == null) properties = new Properties();
            name = propertyNode.getAttributes().getNamedItem("name").getTextContent();
            value = propertyNode.getTextContent();
            properties.setProperty(name, value);
         }

         Node returnGeneratedKeysNode = headerElt.getElementsByTagName("returnGeneratedKeys").item(0);
         boolean returnGeneratedKeys = returnGeneratedKeysNode.getTextContent().equals("true") ? true : false;

         // Read the SQL body
         Node sqlRequestNode = rootElt.getElementsByTagName("body").item(0);
         String sqlRequest = sqlRequestNode.getTextContent();

         if(call.getMethod().equals(Method.POST))
         {
            // Execute the SQL request
            connection = getConnection(connectionURI, properties, usePooling);
            Statement statement = connection.createStatement();
            statement.execute(sqlRequest, returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS
                  : Statement.NO_GENERATED_KEYS);
            JdbcResult result = new JdbcResult(statement);
            call.setOutput(new ObjectRepresentation(result));

            // Commit any changes to the database
            connection.commit();
         }
         else
         {
            throw new IllegalArgumentException("Only the POST method is supported");
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            if(connection != null) connection.close();
         }
         catch(SQLException se)
         {
            // LOG ERROR throw new "An error occured while trying to close a
            // database connection", se);
         }
      }
   }

   /**
    * Returns a JDBC connection.
    * @param uri The connection URI.
    * @param properties The connection properties.
    * @param usePooling Indicates if the connection pooling should be used.
    * @return The JDBC connection.
    * @throws SQLException
    */
   protected Connection getConnection(String uri, Properties properties, boolean usePooling)
         throws SQLException
   {
      Connection result = null;

      if(usePooling)
      {
         for(ConnectionSource c : connectionSources)
         {
            // Check if the connection URI is identical
            // and if the same number of properties is present
            if((result == null) && c.getUri().equalsIgnoreCase(uri)
                  && (properties.size() == c.getProperties().size()))
            {
               // Check that the properties tables are equivalent
               boolean equal = true;
               for(Object key : c.getProperties().keySet())
               {
                  if(equal && properties.containsKey(key))
                  {
                     equal = equal && (properties.get(key) == c.getProperties().get(key));
                  }
                  else
                  {
                     equal = false;
                  }
               }

               if(equal)
               {
                  result = c.getConnection();
               }
            }
         }

         if(result == null)
         {
            // No existing connection source found
            ConnectionSource cs = new ConnectionSource(uri, properties);
            this.connectionSources.add(cs);
            result = cs.getConnection();
         }
      }
      else
      {
         result = DriverManager.getConnection(uri, properties);
      }

      return result;
   }

   /**
    * Escapes quotes in a SQL query.
    * @param query The SQL query to escape.
    * @return The escaped SQL query.
    */
   public static String sqlEncode(String query)
   {
      StringBuilder result = new StringBuilder(query.length() + 10);
      char currentChar;

      for(int i = 0; i < query.length(); i++)
      {
         currentChar = query.charAt(i);
         if(currentChar == '\'')
         {
            result.append("''");
         }
         else
         {
            result.append(currentChar);
         }
      }

      return result.toString();
   }

   /**
    * Creates a connection pool for a given connection configuration.
    * @param uri The connection URI.
    * @param properties The connection properties.
    * @return The new connection pool.
    */
   protected static ObjectPool createConnectionPool(String uri, Properties properties)
   {
      // Create an ObjectPool that will serve as the actual pool of connections
      ObjectPool result = new GenericObjectPool(null);

      // Create a ConnectionFactory that the pool will use to create Connections
      ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, properties);

      // Create the PoolableConnectionFactory, which wraps the "real"
      // Connections created by the ConnectionFactory with
      // the classes that implement the pooling functionality.
      @SuppressWarnings("unused")
      PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
            result, null, null, false, false);

      return result;
   }

   /**
    * Pooling data source which remembers its connection properties and URI.
    */
   class ConnectionSource extends PoolingDataSource
   {
      /** The connection URI. */
      protected String uri;

      /** The connection properties. */
      protected Properties properties;

      /**
       * Constructor.
       * @param uri The connection URI.
       * @param properties The connection properties.
       */
      public ConnectionSource(String uri, Properties properties)
      {
         super(createConnectionPool(uri, properties));
         this.uri = uri;
         this.properties = properties;
      }

      /**
       * Returns the connection URI.
       * @return The connection URI.
       */
      public String getUri()
      {
         return uri;
      }

      /**
       * Returns the connection properties.
       * @return The connection properties.
       */
      public Properties getProperties()
      {
         return properties;
      }
   }

}
