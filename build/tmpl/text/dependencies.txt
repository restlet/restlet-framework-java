
==============================
Dependencies between JAR files
==============================


Below is a list of the dependencies between Restlet libraries. You need to ensure 
that all the dependencies of the libraries that you are using are on the classpath
of your Restlet program, otherwise ClassNotFound exceptions will be thrown.

A minimal Restlet application requires the org.restlet and com.noelios.restlet JARs.

To configure connectors such as HTTP server or HTTP client connectors, please refer
to the Restlet User Guide: http://wiki.restlet.org/docs_1.1/

 
org.restlet (Restlet API)
-----------
 - J2SE 5.0
 - com.noelios.restlet (Noelios Restlet Engine, the Reference Implementation of the Restlet API)


org.restlet.ext.atom_1.0 (Atom client API)
------------------------
 - nothing beside org.restlet and com.noelios.restlet JARs.


org.restlet.ext.fileupload_1.2 (Integration with Apache FileUpload)
------------------------------
 - javax.servlet_2.5
 - org.apache.commons.fileupload_1.2


org.restlet.ext.freemarker_2.3 (Integration with FreeMarker template engine)
------------------------------
 - org.freemarker_2.3


org.restlet.ext.jaxb_2.1 (Integration with JAXB 2.1)
------------------------
 - javax.xml.bind_2.1
 - javax.xml.stream_1.0


org.restlet.ext.jaxrs_1.0 (Integration with JAX-RS 1.0)
-------------------------
 - javax.ws.rs_1.0
 - javax.xml.bind_2.1 (optional)
 - javax.xml.stream_1.0 (optional)
 - javax.mail_1.4 (optional)
 - javax.activation_1.1 (optional)
 - org.json_2.0 (optional)


org.restlet.ext.jibx_1.1 (Integration with JiBX 1.1)
------------------------
 - org.jibx_1.1


org.restlet.ext.json_2.0 (Integration with JavaScript Object Notation)
------------------------
 - org.json_2.0


org.restlet.ext.spring_2.5 (Integration with Spring Framework)
--------------------------
 - org.springframework_2.5


org.restlet.ext.velocity_1.5 (Integration with Apache Velocity)
----------------------------
 - org.apache.velocity_1.5
 - org.apache.commons.collections_3.2
 - org.apache.commons.lang_2.3


org.restlet.ext.wadl_1.0 (Support for WADL configuration)
------------------------
 - nothing beside org.restlet and com.noelios.restlet JARs.


com.noelios.restlet.example (Tutorial examples)
---------------------------
 - com.noelios.restlet.ext.net
 - com.noelios.restlet.ext.simple_3.1
 - org.simpleframework_3.1
 - com.db4o_7.0 (for book examples)
 - edu.purdue.cs.bloat_1.0 (for book examples)


com.noelios.restlet.ext.grizzly_1.8 (HTTP server connector base on Grizzly NIO framework)
-----------------------------------
 - com.sun.grizzly_1.8


com.noelios.restlet.ext.gwt_1.5 (Integration with GWT 1.5)
--------------------------------------
 - com.noelios.restlet.ext.servlet_2.5
 

com.noelios.restlet.ext.httpclient_3.1 (HTTP client connector based on Apache Commons HTTP Client)
--------------------------------------
 - org.apache.commons.codec 1.3
 - org.apache.commons.httpclient 3.1
 - org.apache.commons.logging 1.1


com.noelios.restlet.ext.javamail_1.4 (SMTP client connector based on JavaMail)
------------------------------------
 - javax.mail_1.4
 - javax.activation_1.1


com.noelios.restlet.ext.jdbc_3.0 (JDBC client connector using Apache connections pool)
--------------------------------
 - org.apache.commons.dbcp_1.2
 - org.apache.commons.logging_1.1
 - org.apache.commons.pool_1.3


com.noelios.restlet.ext.jetty_6.1 (Jetty 6 server HTTP connector)
---------------------------------
 - javax.servlet_2.5
 - org.mortbay.jetty_6.1


com.noelios.restlet.ext.net (HTTP client connector based on JDK's HttpUrlConnection)
---------------------------
 - nothing beside org.restlet and com.noelios.restlet JARs.


com.noelios.restlet.ext.oauth (Authentication plugin for the OAuth scheme)
-----------------------------
 - net.oauth_1.0


com.noelios.restlet.ext.servlet_2.5 (Servlet base server HTTP connector)
-----------------------------------
 - Only a Servlet container like Tomcat.


com.noelios.restlet.ext.simple_3.1 (Simple server HTTP connector)
----------------------------------
 - org.simpleframework_3.1


com.noelios.restlet.ext.spring_2.5 (Integration with Spring Framework)
----------------------------------
 - com.noelios.restlet.ext.servlet_2.5
 - org.springframework_2.5


com.noelios.restlet.ext.xdb_11.1 (Integration with Oracle 11g XDB)
--------------------------------
 - com.noelios.restlet.ext.servlet_2.5
 