package dataLoader.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base behaviour and attributes for all dependencies.
 */
public class Dependency {

    /** The current list of editions this dependency is declared on. */
    private Collection<Edition> editions;

    /** "Scope" property for the POM file. */
    private String mavenScope;

    /** Is this dependency optional?. */
    private boolean optional;

    public Collection<Edition> getEditions() {
        if (editions == null) {
            editions = new ArrayList<Edition>();
        }
        return editions;
    }

    public String getMavenScope() {
        return mavenScope;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setEditions(Collection<Edition> editions) {
        this.editions = editions;
    }

    public void setMavenScope(String mavenScope) {
        this.mavenScope = mavenScope;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

}
