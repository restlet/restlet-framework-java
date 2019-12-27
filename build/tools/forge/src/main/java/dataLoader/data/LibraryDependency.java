package dataLoader.data;

import java.util.Collection;

/**
 * Represents a dependency to a library.
 */
public class LibraryDependency extends Dependency {

    /** The library. */
    private Library library;

    /** Is this dependency the primary one? */
    private boolean primary;

    /**
     * Constructor.
     * 
     * @param library
     *            The library.
     * @param mavenScope
     *            The maven scope property.
     * @param primary
     *            Is this dependency primary?
     * @param optional
     *            Is this dependency optional?
     * @param editions
     *            The list of editions for which this dependency is useful.
     */
    public LibraryDependency(Library library, String mavenScope,
            boolean primary, boolean optional, Collection<Edition> editions) {
        super();
        this.library = library;
        this.primary = primary;
        setMavenScope(mavenScope);
        setOptional(optional);
        setEditions(editions);
    }

    public Library getLibrary() {
        return library;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

}
