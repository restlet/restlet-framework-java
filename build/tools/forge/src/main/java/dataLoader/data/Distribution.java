package dataLoader.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A distribution is a container for a set of libraries and modules.
 * 
 */
public class Distribution extends Identified {
    /** Description of the distribution. */
    private String description;

    /** List of libraries. */
    private List<Library> libraries;

    /** List of modules. */
    private List<Module> modules;

    public Distribution(String id) {
        super(id);
    }

    public String getDescription() {
        return description;
    }

    public List<Library> getLibraries() {
        if (libraries == null) {
            libraries = new ArrayList<Library>();
        }
        return libraries;
    }

    public List<Module> getModules() {
        if (modules == null) {
            modules = new ArrayList<Module>();
        }
        return modules;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
