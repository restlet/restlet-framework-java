package dataLoader.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a module.
 */
public class Module extends Bundle {
    /**
     * The list of file patterns to exclude from the compilation phase, by
     * edition.
     */
    private Map<String, String> compileExcludes;

    /**
     * 0 means that no other module depends on it. 1 means that other modules
     * depends directly. 2 means that modules at level 1 depends on it. etc.
     */
    private int dependencyLevel;

    /** The list of exported packages by edition. */
    private Map<String, List<String>> exportedPackages;

    /** Imported packages as read in the MANIFEST.MF file. */
    private List<String> importedPackages;

    /** Include source by edition. */
    private Map<String, Boolean> includeSource;

    /** The list of modules this module depends on. */
    private List<ModuleDependency> neededModules;

    /** The root directory. */
    private String pack;

    /** Ant files handlers for source code generation by edition. */
    private Map<String, FileHandler> source;

    /** Ant files handlers for staging step by edition. */
    private Map<String, FileHandler> stage;

    /** The type of module (core, standard, connector, integration). */
    private String type;

    /** The URI of the wiki page that describes this module. */
    private String wikiUri;

    /**
     * Constructor for an edition.
     * 
     * @param module
     *            The module to clone.
     * @param edition
     *            The edition.
     */
    public Module(Module module, Edition edition) {
        super(module.getId());
        this.dependencyLevel = module.dependencyLevel;
        setActivator(module.getActivator());
        setDescription(module.getDescription());
        if (module.getIncludeSource().containsKey(edition.getId())) {
            getIncludeSource().put(edition.getId(),
                    module.getIncludeSource().get(edition.getId()));
        }
        setJavadocsLinks(module.getJavadocsLinks());
        setMavenMisc(module.getMavenMisc());
        setMinorVersion(module.getMinorVersion());
        setName(module.getName());
        setRootPath(module.getRootPath());

        for (LibraryDependency libDep : module.getNeededLibraries()) {
            if (libDep.getEditions().contains(edition)) {
                getNeededLibraries().add(libDep);
            }
        }

        this.neededModules = new ArrayList<ModuleDependency>();
        for (ModuleDependency modDep : module.getNeededModules()) {
            if (modDep.getEditions().contains(edition)) {
                getNeededModules().add(modDep);
            }
        }
        getCompileExcludes().putAll(module.getCompileExcludes());

        this.pack = module.pack;
        this.source = module.source;
        this.stage = module.stage;
        this.type = module.type;
        this.importedPackages = module.importedPackages;
        setVersionSuffix(module.getVersionSuffix());
        for (Distribution distribution : edition.getDistributions()) {
            if (module.getDistributions().contains(distribution.getId())) {
                this.getDistributions().add(distribution.getId());

            }
        }
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant of the module.
     */
    public Module(String id) {
        super(id);
    }

    /**
     * Returns true if the current module depends on the given one.
     * 
     * @param module
     *            The module to compare to.
     * @return true if the current module depends on the given one.
     */
    public boolean dependsOn(Module module) {
        boolean result = false;

        for (ModuleDependency modDep : getNeededModules()) {
            result = module.getId().equals(modDep.getModule().getId())
                    || modDep.getModule().dependsOn(module);
            if (result) {
                break;
            }
        }

        return result;
    }

    public Map<String, String> getCompileExcludes() {
        if (compileExcludes == null) {
            compileExcludes = new HashMap<String, String>();
        }
        return compileExcludes;
    }

    public int getDependencyLevel() {
        return dependencyLevel;
    }

    public Map<String, List<String>> getExportedPackages() {
        if (exportedPackages == null) {
            exportedPackages = new HashMap<String, List<String>>();
        }
        return exportedPackages;
    }

    public List<String> getImportedPackages() {
        if (importedPackages == null) {
            importedPackages = new ArrayList<String>();
        }
        return importedPackages;
    }

    public String getImportedPackagesAsOptional() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getImportedPackages().size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(getImportedPackages().get(i)).append(
                    ";resolution=optional");
        }
        return (sb.length() > 0) ? sb.toString() : null;
    }

    public Map<String, Boolean> getIncludeSource() {
        if (includeSource == null) {
            includeSource = new HashMap<String, Boolean>();
        }
        return includeSource;
    }

    public List<ModuleDependency> getNeededModules() {
        if (neededModules == null) {
            neededModules = new ArrayList<ModuleDependency>();
        }

        return neededModules;
    }

    @Override
    public Set<String> getNeededPackages() {
        Set<String> result = new TreeSet<String>();
        for (LibraryDependency ld : getNeededLibraries()) {
            result.addAll(ld.getLibrary().getExportedPackages());
            result.addAll(ld.getLibrary().getNeededPackages());
        }
        for (ModuleDependency ld : getNeededModules()) {
            for (Entry<String, List<String>> entry : ld.getModule()
                    .getExportedPackages().entrySet()) {
                result.addAll(entry.getValue());
            }

            result.addAll(ld.getModule().getNeededPackages());
        }
        return result;
    }

    public String getPackage() {
        return pack;
    }

    /**
     * Returns the primary library this module depends on (if any).
     * 
     * @return The primary library this module depends on (if any).
     */
    public Library getPrimaryLibrary() {
        Library result = null;

        if (!getNeededLibraries().isEmpty()) {
            if (getNeededLibraries().size() == 1) {
                result = getNeededLibraries().get(0).getLibrary();
            } else {
                for (LibraryDependency lib : getNeededLibraries()) {
                    if (lib.isPrimary()) {
                        result = lib.getLibrary();
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getRootDirectory() {
        return getPackage();
    }

    public Map<String, FileHandler> getSource() {
        if (source == null) {
            source = new HashMap<String, FileHandler>();
        }
        return source;
    }

    public Map<String, FileHandler> getStage() {
        if (stage == null) {
            stage = new HashMap<String, FileHandler>();
        }
        return stage;
    }

    public String getType() {
        return type;
    }

    public String getWikiUri() {
        return wikiUri;
    }

    public void setDependencyLevel(int dependencyLevel) {
        this.dependencyLevel = dependencyLevel;
    }

    public void setExportedPackages(Map<String, List<String>> exportedPackages) {
        this.exportedPackages = exportedPackages;
    }

    public void setImportedPackages(List<String> importedPackages) {
        this.importedPackages = importedPackages;
    }

    public void setPackage(String pack) {
        this.pack = pack;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setWikiUri(String wikiUri) {
        this.wikiUri = wikiUri;
    }
}
