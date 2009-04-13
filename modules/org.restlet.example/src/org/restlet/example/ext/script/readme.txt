           Introduction to the Restlet Example Application

                        Written by Tal Liron

This is an example application for using scripting languages with Restlet.
It is designed to run as is on any Java 5 platform: all dependent libraries
are included. It contains examples for all supported scripting languages.

The file structure of this application:

run.sh -      Run this to start the application! On most operating systems, you
              need to enable the "executable" permission on this file first.
              After running, you can browse to the application at
              http://localhost:8080/
              
              Edit this file to make sure you are using the correct Java.

run.bat -     As above, for Windows.

main.script - This is the script file first run by the wrapper. It is in charge
              of starting the Restlet server component. It is configured by
              conf/reslet.conf.

conf/

   These are editable configuration files.

   restlet.conf - Configuration file for the Restlet server component.
                  It is used by main.script. Here you can configure the HTTP
                  port used, parameters for the Restlet application, and
                  directories used by it.

jar/

   Java libraries used.

resources/

   These are REST resources implemented in various scripting languages.
   Their actual URL would depend on definitions in restlet.conf. Use
   them as examples of how to implement your own REST resources.

web/

   Files under here are text files with embedded scripts. This is the root
   URL of the server. They contain a mini-site explaining the power of
   using scripting languages with Restlet, with links to the demos.

   Of special importance is demos-rest.page, which contains an example of
   how to access REST resources using JavaScript running in the client web
   browser ("AJAX"). Note that it relies on the jQuery library for this.
   
web/test/

   Here are examples of using embedded scripts in various languages to generate
   HTML. Use them as examples on how to implement your own application.

web/static/

   Files under here are served as is by the server. This can include binary
   files as well as text files.