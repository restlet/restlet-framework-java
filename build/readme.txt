--------------------------
Building Restlet Framework
--------------------------

Basically, we use Ant to build Restlet. The main build script is
'build.xml' located in this directory. To launch the build process
you can simply run 'ant'.

Otherwise, you can list the available build targets by running 'ant -p'. 
Here is the output at time of writing:

Main targets:
 build [default]               Regenerate the source code and build the editions.
 build-current                 Build the editions with the current source code.
 clean                         Clean the staging area.
 clean-dist                    Clean distributions files.
 clean-temp                    Clean temporary build files.
 copy-eclipse                  Copy the generated artifacts to the project
 generate                      Regenerate the source code.
 generate-artifacts            Generates artifacts for the unique source code
 generate-libraries-manifests  Generates the manifest.mf files for the unique source code
 generate-manifests            Generates the manifest.mf files for the unique source code
 generate-misc                 Generates miscellaneous files.
 generate-modules-manifests    Generates the manifest.mf files for the unique source code
 generate-poms                 Generates the pom files for the unique source code
 prepare-stage-maven           Prepare the generation of the maven distributions.
 rebuild                       Clean the temp files, regenerate the source code and build the editions.
 regenerate                    Clean and regenerate the source code.
Default target: build

The build uses configuration variables located in 'build.properties'. 
This file contains the default value and shouldn't be locally modified 
as it is save in the Git repository. If you need to customize it, you 
should create a 'custom.properties' file following the same format that
will be detected by the build and will override the default values.

