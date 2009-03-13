rem register Restlet XDB Servlet Adapter Demo
rem execute logged as SYS or other DBA user
rem requires two arguments with the schema used to test Restlet, for example RESTLET
set define '$';
define sch=$1;
-- Grants required for public REST WS
begin
  -- REST WS with public access
  dbms_java.grant_permission( 'ANONYMOUS', 'SYS:java.lang.RuntimePermission', 'getClassLoader', '' );
  dbms_java.grant_permission( 'ANONYMOUS', 'SYS:java.util.logging.LoggingPermission', 'control', '' );
  commit;
end;
/

-- Grants required for public REST WS
-- http://download.oracle.com/docs/cd/B28359_01/appdev.111/b28369/xdb22pro.htm#CHDHAGBF
alter user anonymous account unlock;

rem UserService test
rem http://localhost:8080/userapp/users/scott/orders/300
DECLARE
  configxml SYS.XMLType;
begin
  dbms_xdb.deleteServletMapping('UsersRestlet');
  dbms_xdb.deleteServlet('UsersRestlet');
  dbms_xdb.addServlet(name=>'UsersRestlet',language=>'Java',class=>'org.restlet.ext.xdb.XdbServerServlet',dispname=>'Restlet Servlet',schema=>'PUBLIC');
  -- Modify the configuration
  -- Due this servlet provide public access, it can not load 
  -- '/home/'||USER||'/restlet/UsersRestlet.xml' from XMLDB repository
  SELECT INSERTCHILDXML(xdburitype('/xdbconfig.xml').getXML(),'/xdbconfig/sysconfig/protocolconfig/httpconfig/webappconfig/servletconfig/servlet-list/servlet[servlet-name="UsersRestlet"]','init-param',
  XMLType('<init-param xmlns="http://xmlns.oracle.com/xdb/xdbconfig.xsd">
                  <param-name>org.restlet.application</param-name>
                  <param-value>$sch:org.restlet.example.tutorial.Part12</param-value>
                  <description>REST User Application</description>
           </init-param>'),'xmlns="http://xmlns.oracle.com/xdb/xdbconfig.xsd"') INTO configxml
  FROM DUAL;
  -- Update the configuration to use the modified version
  --I got this error at this line :
  dbms_xdb.cfg_update(configxml);
  dbms_xdb.addServletSecRole(SERVNAME => 'UsersRestlet',ROLENAME => 'PUBLIC',ROLELINK => 'PUBLIC');
  dbms_xdb.addServletMapping('/userapp/*','UsersRestlet');
  commit;
end;
/
commit;

rem redirect test
rem http://localhost:8080/searchapp/search?kwd=myKeyword1+myKeyword2
rem will be routed to
rem http://www.google.com/search?q=site:mysite.org+myKeyword1%20myKeyword2
DECLARE
  configxml SYS.XMLType;
begin
  dbms_xdb.deleteServletMapping('SearchRestlet');
  dbms_xdb.deleteServlet('SearchRestlet');
  dbms_xdb.addServlet(name=>'SearchRestlet',language=>'Java',class=>'org.restlet.ext.xdb.XdbServerServlet',dispname=>'Restlet Servlet',schema=>'PUBLIC');
  -- Modify the configuration
  -- Due this servlet provide public access, it can not load 
  -- '/home/'||USER||'/restlet/UsersRestlet.xml' from XMLDB repository
  SELECT INSERTCHILDXML(xdburitype('/xdbconfig.xml').getXML(),'/xdbconfig/sysconfig/protocolconfig/httpconfig/webappconfig/servletconfig/servlet-list/servlet[servlet-name="SearchRestlet"]','init-param',
  XMLType('<init-param xmlns="http://xmlns.oracle.com/xdb/xdbconfig.xsd">
                  <param-name>org.restlet.application</param-name>
                  <param-value>$sch:org.restlet.example.tutorial.Part10</param-value>
                  <description>REST Search Application</description>
           </init-param>'),'xmlns="http://xmlns.oracle.com/xdb/xdbconfig.xsd"') INTO configxml
  FROM DUAL;
  -- Update the configuration to use the modified version
  --I got this error at this line :
  dbms_xdb.cfg_update(configxml);
  dbms_xdb.addServletSecRole(SERVNAME => 'SearchRestlet',ROLENAME => 'PUBLIC',ROLELINK => 'PUBLIC');
  dbms_xdb.addServletMapping('/searchapp/*','SearchRestlet');
  commit;
end;
/
commit;
