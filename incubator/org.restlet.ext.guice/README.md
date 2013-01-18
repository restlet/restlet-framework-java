# Restlet Guice extension 

This extension provides two independent ways to inject Restlet
ServerResource instances:

1.  Members (methods and fields) of resources extending `SelfInjectingServerResource`
    will be injected if an instance of `SelfInjectingServerResourceModule` is 
    passed to `Guice.createInjector`.
    
2.  A `Finder` instance returned by a `FinderFactory` method call will create
    and inject resource instances determined by the type argument (and optional
    annotation argument) passed to that method call, according to the bindings 
    established by the injector from which the `FinderFactory` was obtained.
    
The first approach is more lightweight, but it relies on static injection and
does not permit constructor injection.

The second approach is more complicated, but more flexible.
