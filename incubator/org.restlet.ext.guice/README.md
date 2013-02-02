# Restlet Guice extension 

This extension provides two independent ways to inject Restlet
ServerResource instances:

1.  Members (methods and fields) of resources extending [SelfInjectingServerResource](
        https://github.com/restlet/restlet-framework-java/blob/guice-integration/incubator/org.restlet.ext.guice/src/org/restlet/ext/guice/SelfInjectingServerResource.java
    ) will be injected if an instance of [SelfInjectingServerResourceModule](
        https://github.com/restlet/restlet-framework-java/blob/guice-integration/incubator/org.restlet.ext.guice/src/org/restlet/ext/guice/SelfInjectingServerResourceModule.java   
    ) is 
    passed to the Guice injector creation call.
    
2.  A `Finder` instance returned by a [FinderFactory](
        https://github.com/restlet/restlet-framework-java/blob/guice-integration/incubator/org.restlet.ext.guice/src/org/restlet/ext/guice/FinderFactory.java
    ) method call will create
    and inject resource instances determined by the type argument (and optional
    annotation argument) passed to that method call, according to the bindings 
    established by the injector from which the `FinderFactory` was obtained.
    
The first approach is more lightweight, but it relies on static injection and
does not permit constructor injection. Read about it [here](
  http://tembrel.blogspot.com/2012/03/restlet-guice-extension-considered.html
).

The second approach is more complicated, but more flexible. Read about it [here](
  http://tembrel.blogspot.com/2009/05/dependency-injection-in-restlet-20-with.html
).
