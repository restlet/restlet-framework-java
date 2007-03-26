#!/bin/sh

# build first

root=../..
base=${root}/build/temp/classes
libraries=${root}/libraries
cp=${base}/com.noelios.restlet.ext.jxta_2.5:${base}/org.restlet:${base}/com.noelios.restlet
lib=${libraries}/net.jxta_2.5/net.jxta.jar:${libraries}/net.jxta_2.5/net.jxta.ext.network.jar:${libraries}/net.jxta_2.5/net.jxta.ext.configuration.jar:${libraries}/net.jxta_2.5/guice-1.0.jar:${libraries}/net.jxta_2.5/aopalliance.jar:${libraries}/javax.servlet_2.4/javax.servlet.jar:${libraries}/org.mortbay.jetty_4.2/org.mortbay.jetty.jar:${libraries}/org.bouncycastle_1.34/org.bouncycastle-jdk14.jar
sp="-Djava.util.logging.config.file=./logging.properties"
main=com.noelios.restlet.ext.jxta.prototype.Main

cp -r ./src/com/noelios/restlet/ext/jxta/resources ${base}/com.noelios.restlet.ext.jxta_2.5/com/noelios/restlet/ext/jxta

java ${sp} -cp ${cp}:${lib} ${main} $*
