package dataLoader.data;

import java.util.Collection;

/**
 * Represents a dependency on a module.
 */
public class ModuleDependency extends Dependency {
    /** The module. */
    private Module module;

    /**
     * Constructor.
     * 
     * @param module
     *            The module.
     * @param mavenScope
     *            The maven scope property.
     * @param optional
     *            Is this dependency optional?
     * @param editions
     *            The list of editions for which this dependency is useful.
     */
    public ModuleDependency(Module module, String mavenScope, boolean optional,
            Collection<Edition> editions) {
        super();
        this.module = module;
        setMavenScope(mavenScope);
        setEditions(editions);
        setOptional(optional);
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

}
