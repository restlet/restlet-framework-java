Copyright (c) 1998-2007 Caucho Technology. All Rights Reserved.

This is the README file for Quercus(tm)
=======================================
TABLE OF CONTENTS
  I)   Introduction
  II)  Getting Started
  III) Using a database with Quercus
  IV)  Quercus Standalone vs. Quercus Professional
  V)   Quercus on Glassfish
  VI)  Resources


I) Introduction 
---------------

Thank you for downloading the Quercus standalone .war package.

Quercus is Caucho Technology's 100% Java implementation of PHP 5.

II) Getting Started
-------------------

First, make sure you are running JDK 5 or greater.

Then follow your server's instructions for deploying a .war file.  After
deploying, browse to the Quercus webapp and the index page will check that
Quercus is correctly deployed.

III) Using a database with Quercus
----------------------------------

Currently, Quercus standalone is only able to obtain database connections from
a DataSource configured using JNDI.  Application servers typically provide a
mechanism for making a connection pool DataSource available with JNDI.

Quercus database connection methods accept the JNDI name directly:

  $conn = mysql_connect("java:comp/env/jdbc/myDatabaseName")

  OR

  $pdo = new PDO("java:comp/env/jdbc/myDatabaseName");

For existing scripts that use traditional PHP connection information:

  $conn = mysql_connect("localhost", "user", "pass");

  OR

  $pdo = new PDO("mysql:host=localhost", "user", "pass");

Quercus standdalone will ignore the arguments and will connect directly to a
preconfigured JDBC database.  For these cases, configure Quercus to use the
JDBC database by adding a tag to WEB-INF/web.xml:

    <init-param>
      <param-name>database</param-name>
      <param-value>jdbc/myDatabaseName</param-value>
    </init-param>

Consult the documentation for the application server you are using for
instructions on configuring a database and making it available with a JNDI
name.

IV) Quercus Standalone vs. Quercus Professional
-----------------------------------------------

Quercus Professional is included with the Resin Professional application
server.  Some Quercus features require the advanced features of Resin
Professional and are not available with Quercus standalone.

1) Quercus Professional compiles php scripts into java bytecode.

2) Quercus Professional is able to create database connection pools from
   traditional PHP connection arguments.  Quercus standalone requires a JNDI
   configuration.

V) Quercus on Glassfish
--------------------------

It is simple to deploy Quercus on Glassfish.  It is no different from the
standard procedure to deploy any other webapp on Glassfish:

1) Download the latest Quercus .war file from http://quercus.caucho.com.

2) Log into Glassfish web admin.

3) Click on deploy Web Application (.war).

4) Point Glassfish to the location of the Quercus .war file'. Then click OK.
   Glassfish will automatically deploy Quercus.

5) Quercus should now be running.  To check, browse to the Quercus webapp and
   an index page will show up indicating the status of Quercus.  Or you can
   click on "Launch" from Glassfish admin's "Web Applications" page and your
   browser will go to the Quercus webapp.

6) PHP files can now be added to the root directory of the expanded web
    application.

The following steps are only needed if you intend to use a database in
Quercus with Glassfish:

7) Download the databse driver and place it into Glassfish's
   lib directory.

8) In Glassfish web admin, create a JDBC Connection Pool.  Specify the database
   name, username, and password.

9) In Glassfish web admin, create a JDBC Resource with a JNDI name , for example 
   "jdbc/myDatabaseName".

10) Configure Quercus to use the JDBC database by adding a tag to
    WEB-INF/web.xml:

    <init-param>
      <param-name>database</param-name>
      <param-value>jdbc/myDatabaseName</param-value>
    </init-param>


VI) Resources
-------------

1) http://quercus.caucho.com
   This is the home page for Quercus.  Go here for documentation, guides,
   examples, and Quercus downloads.

2) http://forum.caucho.com
   The Caucho Forums is a good place to discuss Quercus,  Resin, Caucho, and Hessian.

3) http://www.caucho.com
   If you like Quercus, please come visit Caucho, the people behind Quercus.

4) http://maillist.caucho.com
   Mailing lists for Resin and Hessian.

