package dataLoader.data;

/**
 * Smallest part of a library: a package.
 */
public class LibraryPackage extends Identified {
    /** The description of the package. */
    private String description;

    /** Artifact id (maven). */
    private String mavenArtifactId;

    /** Group id (maven). */
    private String mavenGroupId;

    /** Version (maven). */
    private String mavenVersion;

    /** Name of the package (e.g.: org.db4o.nativequery). */
    private String name;

    public LibraryPackage(String id) {
        super(id);
    }

    public String getDescription() {
        return description;
    }

    public String getMavenArtifactId() {
        return mavenArtifactId;
    }

    public String getMavenGroupId() {
        return mavenGroupId;
    }

    public String getMavenVersion() {
        return mavenVersion;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMavenArtifactId(String mavenArtifactId) {
        this.mavenArtifactId = mavenArtifactId;
    }

    public void setMavenGroupId(String mavenGroupId) {
        this.mavenGroupId = mavenGroupId;
    }

    public void setMavenVersion(String mavenVersion) {
        this.mavenVersion = mavenVersion;
    }

    public void setName(String name) {
        this.name = name;
    }

}
