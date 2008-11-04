rem Compiled valid classes under RESTLET schema in batch to reduce memory usage
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

declare
  res NUMBER;
begin
  for c in (SELECT dbms_java.longname(object_name) cName
            FROM user_objects
            WHERE object_type = 'JAVA CLASS' AND status = 'VALID'
            AND dbms_java.longname(object_name) like 'com/noelios/restlet/%') loop
    res := dbms_java.compile_class(c.cName);
  end loop;
end;
/
