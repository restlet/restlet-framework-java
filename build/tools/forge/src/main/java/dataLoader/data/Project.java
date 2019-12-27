package dataLoader.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a project seen as a set of editions, libraries, and modules.
 */
public class Project {
    /** The set of editions. */
    Map<String, Edition> editions;

    /** The set of libraries. */
    Map<String, Library> libraries;

    /** The set of modules. */
    Map<String, Module> modules;

    public Map<String, Edition> getEditions() {
        if (editions == null) {
            editions = new HashMap<String, Edition>();
        }
        return editions;
    }

    public Map<String, Library> getLibraries() {
        if (libraries == null) {
            libraries = new HashMap<String, Library>();
        }
        return libraries;
    }

    public Map<String, Module> getModules() {
        if (modules == null) {
            modules = new HashMap<String, Module>();
        }
        return modules;
    }

}
