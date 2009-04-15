
set java=java
set main=com.threecrickets.scripturian.ScriptedMain

set jars=jar/restlet/org.restlet.jar;^
jar/restlet/org.restlet.ext.script.jar;^
jar/restlet/com.threecrickets.scripturian.jar;^
jar/restlet/org.restlet.ext.json.jar;^
jar/restlet/org.json.jar;^
jar/scripting/javax.script.jar;^
jar/scripting/org.mozilla.rhino.jar;^
jar/scripting/com.sun.phobos.script.jar;^
jar/scripting/com.caucho.quercus.jar;^
jar/scripting/javax.servlet.jar;^
jar/scripting/com.caucho.resin.util.jar;^
jar/scripting/jython.jar;^
jar/scripting/com.sun.script.jython.jar;^
jar/scripting/org.jruby.jar;^
jar/scripting/com.sun.script.jruby.jar;^
jar/scripting/org.codehaus.groovy.jar;^
jar/scripting/org.apache.velocity.jar;^
jar/scripting/com.sun.script.velocity.jar

%java% -cp %jars% %main%
