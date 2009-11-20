package org.restlet.ext.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Guice dependency injection for Restlet.
 */
public class RestletGuice {

    /**
     * Creates an injector from the given modules with FinderFactory bound to an
     * implementation that uses the injector's bindings to create Finder
     * instances.
     */
    public static Injector createInjector(Module... modules) {
        return injectorFor(null, new RestletGuiceModule(modules));
    }

    /**
     * Creates an injector from the given modules and stage with FinderFactory
     * bound to an implementation that uses the injector's bindings to create
     * Finder instances.
     */
    public static Injector createInjector(Stage stage, Module... modules) {
        return injectorFor(stage, new RestletGuiceModule(modules));
    }

    /**
     * Creates an injector from the given modules with FinderFactory bound to an
     * implementation that uses the injector's bindings to create Finder
     * instances.
     */
    public static Injector createInjector(Iterable<Module> modules) {
        return injectorFor(null, new RestletGuiceModule(modules));
    }

    /**
     * Creates an injector from the given modules and stage with FinderFactory
     * bound to an implementation that uses the injector's bindings to create
     * Finder instances.
     */
    public static Injector createInjector(Stage stage, Iterable<Module> modules) {
        return injectorFor(stage, new RestletGuiceModule(modules));
    }

    private static Injector injectorFor(Stage stage, RestletGuiceModule module) {
        if (stage == null) {
            return Guice.createInjector(module);
        } else {
            return Guice.createInjector(stage, module);
        }
    }
}