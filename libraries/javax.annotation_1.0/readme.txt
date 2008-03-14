----------------------------------------
Common Annotations for the Java Platform
----------------------------------------

With the addition of JSR 175 (A Metadata Facility for the JavaTM Programming 
Language) in the Java platform we envision that various technologies will use 
annotations to enable a declarative style of programming. It would be
unfortunate if these technologies each independently defined their own 
annotations for common concepts. It would be valuable to have consistency within
the Java EE and Java SE component technologies, but it will also be valuable to
allow consistency between Java EE and Java SE.

It is the intention of this specification to define a small set of common 
annotations that will be available for use within other specifications. It is 
hoped that this will help to avoid unnecessary redundancy or duplication between
annotations defined in different Java Specification Requests (JSR). This would
allow us to have the common annotations all in one place and let the 
technologies refer to this specification rather than have them specified in 
multiple specifications. This way all technologies can use the same version of 
the annotations and there will be consistency in the annotations used across the
platforms.

For more information:
http://jcp.org/en/jsr/detail?id=175