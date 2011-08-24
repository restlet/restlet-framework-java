rem Create the schema and grant required privileges
rem execute logged as SYS or other DBA user
rem requires two arguments with the schema used to test Restlet, for example RESTLET
rem and his password for example RESTLET
set define '$';
define sch=$1;
define pwd=$2;

drop user $sch cascade;

create user $sch identified by $pwd
temporary tablespace temp
default tablespace users
quota unlimited on users;

grant connect,resource to $sch;
rem required grants for remote debugging
rem uncomment this if you need remote debugging
rem grant DEBUG CONNECT SESSION, DEBUG ANY PROCEDURE, JAVADEBUGPRIV to $sch;

grant create public synonym to $sch;

begin
  -- required grants for using JDK1.4 logging
  dbms_java.grant_permission( UPPER('$sch'), 'SYS:java.lang.RuntimePermission', 'getClassLoader', '' );
  dbms_java.grant_permission( UPPER('$sch'), 'SYS:java.lang.RuntimePermission', 'createClassLoader', '' );
  dbms_java.grant_permission( UPPER('$sch'), 'SYS:java.util.logging.LoggingPermission', 'control', '' );
  dbms_java.grant_permission( UPPER('$sch'), 'SYS:java.lang.RuntimePermission', 'accessDeclaredMembers', '' );
  dbms_java.grant_permission( 'ANONYMOUS', 'SYS:java.lang.RuntimePermission', 'getClassLoader', '' );
  dbms_java.grant_permission( 'ANONYMOUS', 'SYS:java.lang.RuntimePermission', 'createClassLoader', '' );
  dbms_java.grant_permission( 'ANONYMOUS', 'SYS:java.util.logging.LoggingPermission', 'control', '' );
  dbms_java.grant_permission( 'ANONYMOUS', 'SYS:java.lang.RuntimePermission', 'accessDeclaredMembers', '' );
  -- required for remote debugging
  -- uncomment this if you need remote debugging
  -- dbms_java.grant_permission( UPPER('$sch'), 'SYS:java.net.SocketPermission','localhost:4000', 'connect,resolve' );
  -- required grants for some examples
  dbms_java.grant_permission( UPPER('$sch'), 'SYS:java.net.SocketPermission','www.restlet.org', 'connect,resolve' );
  dbms_java.grant_permission( UPPER('$sch'), 'SYS:java.net.SocketPermission','s3.amazonaws.com', 'connect,resolve' );
  commit;
end;
/

-- as XDB
alter session set current_schema = XDB
/

declare
  result boolean;
begin
  -- Create a Restlet home directory to store application config files and wars
  -- used with $sch owner
  if (not dbms_xdb.ExistsResource('/home')) then
    result := dbms_xdb.createFolder('/home');
  end if;
  if (not dbms_xdb.ExistsResource('/home/'||UPPER('$sch'))) then
    result := dbms_xdb.createFolder('/home/'||UPPER('$sch'));
    dbms_xdb.setAcl('/home/'||UPPER('$sch'),'/sys/acls/all_owner_acl.xml');
    update resource_view
         set res = updateXml(res,'/Resource/Owner/text()',UPPER('$sch'))
         where equals_path(res,'/home/'||UPPER('$sch')) = 1;
  end if;
end;
/
commit;

declare
  result boolean;
begin
  -- Create a Restlet home directory to store application config files and wars
  -- used with public restlet
  if (not dbms_xdb.ExistsResource('/home/ANONYMOUS')) then
    result := dbms_xdb.createFolder('/home/ANONYMOUS');
    dbms_xdb.setAcl('/home/ANONYMOUS','/sys/acls/all_all_acl.xml');
    update resource_view
         set res = updateXml(res,'/Resource/Owner/text()','ANONYMOUS')
         where equals_path(res,'/home/ANONYMOUS') = 1;
  end if;
end;
/
commit;
