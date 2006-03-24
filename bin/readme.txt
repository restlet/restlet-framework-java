------------------------
Running Restlet services
------------------------

A common requirement for server-side Restlet applications is the ability to run 
as a system service. For this purpose, the Restlet project recommmands the use 
of the powerful Java Service Wrapper project developped by Tanuki Software:
http://wrapper.tanukisoftware.org

This wrapper is based on a single multi-platform configuration file, a wrapper 
JAR file and a set of platform-specific binary files to launch, install and 
uninstall your service. See the Java Service Wrapper web site for details.

A skeleton configuration file can be found under "./conf/wrapper.conf". This 
file must be customized to provided your application's main class, its 
parameters and all the classpath entries.

Java Service Wrapper binaries are provided for the Windows x86-32 (see ./win)
and Linux x86-32 (see ./linux) platforms but several other platforms are 
supported as well. See the Java Service Wrapper web site for details.

Thanks to Malcolm Sparks for suggesting the use of this great wrapper tool.

Best regards,
Jerome Louvel
