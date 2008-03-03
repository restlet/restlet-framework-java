rem register Restlet XDB Servlet Adapter Demo
rem execute logged as SYS or other DBA user
rem requires two arguments with the schema used to test Restlet, for example RESTLET
set define '$';
define sch=$1;

begin
  -- REST WS with public access
  dbms_java.grant_permission( 'ANONYMOUS', 'SYS:java.lang.RuntimePermission', 'getClassLoader', '' );
  commit;
end;
/

DECLARE
  configxml SYS.XMLType;
begin
  dbms_xdb.deleteServletMapping('UsersRestlet');
  dbms_xdb.deleteServlet('UsersRestlet');
  dbms_xdb.addServlet(name=>'UsersRestlet',language=>'Java',class=>'com.noelios.restlet.ext.xdb.XDBServerServlet',dispname=>'Restlet Servlet',schema=>'PUBLIC');
  -- Modify the configuration
  -- Due this servlet provide public access, it can not load 
  -- '/home/'||USER||'/restlet/UsersRestlet.xml' from XMLDB repository
  SELECT INSERTCHILDXML(xdburitype('/xdbconfig.xml').getXML(),'/xdbconfig/sysconfig/protocolconfig/httpconfig/webappconfig/servletconfig/servlet-list/servlet[servlet-name="UsersRestlet"]','init-param',
  XMLType('<init-param xmlns="http://xmlns.oracle.com/xdb/xdbconfig.xsd">
                  <param-name>org.restlet.application</param-name>
                  <param-value>org.restlet.example.tutorial.Part12</param-value>
                  <description>REST Application</description>
           </init-param>'),'xmlns="http://xmlns.oracle.com/xdb/xdbconfig.xsd"') INTO configxml
  FROM DUAL;
  -- Update the configuration to use the modified version
  --I got this error at this line :
  dbms_xdb.cfg_update(configxml);
  dbms_xdb.addServletSecRole(SERVNAME => 'UsersRestlet',ROLENAME => 'PUBLIC',ROLELINK => 'PUBLIC');
  dbms_xdb.addServletMapping('/users/*','UsersRestlet');
  commit;
end;
/
commit;
