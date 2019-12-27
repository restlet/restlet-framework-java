package dataLoader.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a library or module.
 */
public abstract class Bundle extends Identified {
    /** The full name of the bundle activator. */
    private String activator;

    /** Map of attributes. */
    private Map<String, String> attributes;

    /** The description of the artifact. */
    private String description;

    /** The list of distributions this artifact is included in. */
    private List<String> distributions;

    /** Javadocs links. */
    private List<String> javadocsLinks;

    /** The additional XML instructions for the Maven POM file. */
    private String mavenMisc;

    /** The minor version of this artifact. */
    private String minorVersion;

    /** The name of the artifact. */
    private String name;

    /** the list of libraries this artifact depends on. */
    private List<LibraryDependency> neededLibraries;

    /** The vendor name. */
    private String provider;

    /** The path to find this bundle. */
    private String rootPath;

    /** The symbolic name (generally the package name). */
    private String symbolicName;

    /** The version suffix (e.g.: -snapshot). */
    private String versionSuffix;

    public Bundle(String id) {
        super(id);
    }

    public String getActivator() {
        return activator;
    }

    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        return attributes;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getDistributions() {
        if (distributions == null) {
            distributions = new ArrayList<String>();
        }
        return distributions;
    }

    public List<String> getJavadocsLinks() {
        if (javadocsLinks == null) {
            javadocsLinks = new ArrayList<String>();
        }
        return javadocsLinks;
    }

    public String getMavenMisc() {
        return mavenMisc;
    }

    public String getMinorVersion() {
        return minorVersion;
    }

    public String getName() {
        return name;
    }

    public List<LibraryDependency> getNeededLibraries() {
        if (neededLibraries == null) {
            neededLibraries = new ArrayList<LibraryDependency>();
        }
        return neededLibraries;
    }

    public abstract Set<String> getNeededPackages();

    public String getProvider() {
        return provider;
    }

    /**
     * Returns the root directory of the artifact.
     * 
     * @return The root directory of the artifact.
     */
    public abstract String getRootDirectory();

    public String getRootPath() {
        return rootPath;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getVersionFull() {
        if (getVersionSuffix() != null) {
            // Seuls les premiers caractères numériques sont pris en compte pour
            // la version "micro".
            StringBuilder sb = new StringBuilder();
            sb.append(getMinorVersion());
            sb.append(".");

            boolean isMicroPart = true;
            for (int i = 0; i < getVersionSuffix().length(); i++) {
                char c = getVersionSuffix().charAt(i);
                if (isMicroPart) {
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    } else {
                        // Fin de la partie "micro" du numéro de version, le
                        // reste est un qualificateur quelconque.
                        isMicroPart = false;
                        if (c != '.') {
                            sb.append(".");
                        }
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        }
        return getMinorVersion() + ".0";
    }

    public String getVersionSuffix() {
        return versionSuffix;
    }

    public void setActivator(String activator) {
        this.activator = activator;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDistributions(List<String> distributions) {
        this.distributions = distributions;
    }

    public void setJavadocsLinks(List<String> javadocsLinks) {
        this.javadocsLinks = javadocsLinks;
    }

    public void setMavenMisc(String mavenMisc) {
        this.mavenMisc = mavenMisc;
    }

    public void setMinorVersion(String minorVersion) {
        this.minorVersion = minorVersion;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNeededLibraries(List<LibraryDependency> neededLibraries) {
        this.neededLibraries = neededLibraries;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public void setVersionSuffix(String versionSuffix) {
        this.versionSuffix = versionSuffix;
    }
}
