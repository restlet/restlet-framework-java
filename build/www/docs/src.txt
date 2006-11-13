
============
Source files
============


In this directory you will find the source files for the Restlet project, 
organized by plugins. The main purpose is to let you link the sources to
the Restlet plugins in your preferred IDE in order to enable debugging 
inside the Restlet code.

If you want to rebuild the whole project by yourself, you need to directly
use the Subversion (SVN) source repository located on the development Web site
at Tigris: http://restlet.tigris.org/source/browse/restlet/

The main development branch is available in the "trunk" directory. Also, each
version released is tagged and available in the "tags" directory.

There are several SVN clients available. These ones work great for us:
 - Tortoise SVN, a Windows shell extension: 
   http://restlet.tigris.org/source/browse/restlet/
 - Subclipse, an Eclipse IDE plugin:
   http://subclipse.tigris.org/
 
More clients available, see the list at http://subversion.tigris.org/links.html

Once, you have checked out the source code from the SVN repository, you need to
ensure that a recent version of Ant is intalled (superior to 1.6.5). You can get
installation instructions here: http://ant.apache.org/

Then, you need to adjust your JAVA_HOME environment property to make it point to
a JDK root directory (version 1.5 or above).

From the source root directory, you can do an "ant -p" to see the list of
available targets and a simple "ant" will rebuild the whole project. You can
finally customize your build by changing some settings in the "build.properties"
file.