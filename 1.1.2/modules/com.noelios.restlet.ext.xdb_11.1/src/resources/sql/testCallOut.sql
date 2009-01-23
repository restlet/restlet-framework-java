rem execute this demo as RESTLET user

CREATE OR REPLACE PROCEDURE Part02a(s1 VARCHAR2, s2 VARCHAR2) AS LANGUAGE JAVA
NAME 'org.restlet.example.tutorial.Part02a.main(java.lang.String[])';
/

CREATE OR REPLACE PROCEDURE Part02b(s1 VARCHAR2, s2 VARCHAR2) AS LANGUAGE JAVA
NAME 'org.restlet.example.tutorial.Part02b.main(java.lang.String[])';
/

CREATE OR REPLACE PROCEDURE AwsTest(KeyId VARCHAR2, pass VARCHAR2) AS LANGUAGE JAVA
NAME 'org.restlet.example.misc.AwsTest.main(java.lang.String[])';
/

begin
  -- see output at $ORACLE_SID_ora_xxxxx.trc files at $ORACLE_BASE/diag/rdbms/$ORACLE_SID/trace directory
  Part02a('a','b');
  Part02b('a','b');
  -- put a correct AWS Access Key Id and password
  AwsTest('44CF9590006BF252F707','OtxrzxIsfpFjA7SwPzILwy8Bw21TLhquhboDYROV');
end;
/
