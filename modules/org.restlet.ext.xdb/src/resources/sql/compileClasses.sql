rem Compiled valid classes under RESTLET schema
declare
  res NUMBER;
begin
  for c in (SELECT dbms_java.longname(object_name) cName
            FROM user_objects
            WHERE object_type = 'JAVA CLASS' AND status = 'VALID'
            AND dbms_java.longname(object_name) like 'org/restlet/%') loop
    res := dbms_java.compile_class(c.cName);
  end loop;
end;
/
