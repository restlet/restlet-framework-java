package dataLoader.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An edition is a container of libraries, modules, and target distributions
 * (zip, exe, maven, etc).
 * 
 */
public class Edition extends Identified {
    /** Description of the edition. */
    private String description;

    /** The set of distributions. */
    private List<Distribution> distributions;

    /** The list of external documentation URIs. */
    private List<String> javadocsLinks;

    /** The label of an edition. */
    private Label label;

    /** The list of libraries. */
    private List<Library> libraries;

    /** The list of modules. */
    private List<Module> modules;

    /** The base name of the core package ("org.restlet" by default). */
    private String packageCore;

    /** The base name of the engine package ("org.restlet.engine" by default). */
    private String packageEngine;

    /** The base name of the extension package ("org.restlet.ext" by default). */
    private String packageExtension;

    /** Map of parameters. */
    private Map<String, String> parameters;

    /** The post source steps. */
    private String postSource;

    /** Translate packages? */
    private boolean translatePackages;

    /** The URI of the wiki page that describes this edition. */
    private String wikiUri;

    /**
     * Constructor with parameter.
     * 
     * @param id
     *            The identifiant of an edition.
     */
    public Edition(String id) {
        super(id);
        packageCore = "org.restlet";
        packageEngine = "org.restlet.engine";
        packageExtension = "org.restlet.ext";
    }

    public String getDescription() {
        return description;
    }

    public List<Distribution> getDistributions() {
        if (distributions == null) {
            distributions = new ArrayList<Distribution>();
        }
        return distributions;
    }

    public String getFullLabel() {
        return (label != null) ? label.getFull() : null;
    }

    public List<String> getJavadocsLinks() {
        if (javadocsLinks == null) {
            javadocsLinks = new ArrayList<String>();
        }
        return javadocsLinks;
    }

    private Label getLabel() {
        if (label == null) {
            label = new Label();
        }
        return label;
    }

    public List<Library> getLibraries() {
        if (libraries == null) {
            libraries = new ArrayList<Library>();
        }
        return libraries;
    }

    public String getMediumLabel() {
        return (label != null) ? label.getMedium() : null;
    }

    public List<Module> getModules() {
        if (modules == null) {
            modules = new ArrayList<Module>();
        }
        return modules;
    }

    public String getPackageCore() {
        return packageCore;
    }

    public String getPackageEngine() {
        return packageEngine;
    }

    public String getPackageExtension() {
        return packageExtension;
    }

    public Map<String, String> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        return parameters;
    }

    public String getPostSource() {
        return postSource;
    }

    public String getShortLabel() {
        return (label != null) ? label.getShort() : null;
    }

    public String getWikiUri() {
        return wikiUri;
    }

    public boolean isTranslatePackages() {
        return translatePackages;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFullLabel(String fullLabel) {
        getLabel().setFull(fullLabel);
    }

    public void setMediumLabel(String mediumLabel) {
        getLabel().setMedium(mediumLabel);
    }

    public void setPackageCore(String packageCore) {
        this.packageCore = packageCore;
    }

    public void setPackageEngine(String packageEngine) {
        this.packageEngine = packageEngine;
    }

    public void setPackageExtension(String packageExtension) {
        this.packageExtension = packageExtension;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setPostSource(String postSource) {
        this.postSource = postSource;
    }

    public void setShortLabel(String shortLabel) {
        getLabel().setShort(shortLabel);
    }

    public void setTranslatePackages(boolean translatePackages) {
        this.translatePackages = translatePackages;
    }

    public void setWikiUri(String wikiUri) {
        this.wikiUri = wikiUri;
    }
}
