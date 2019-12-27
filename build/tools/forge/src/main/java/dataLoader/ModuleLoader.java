package dataLoader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.Manifest;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import dataLoader.data.Edition;
import dataLoader.data.FileHandler;
import dataLoader.data.Library;
import dataLoader.data.LibraryDependency;
import dataLoader.data.Module;
import dataLoader.data.ModuleDependency;
import dataLoader.data.Project;

/**
 * Load modules descriptors.
 * 
 */
public class ModuleLoader {

    /**
     * Inner SAX content handler.
     * 
     */
    private static class ModuleReader extends DefaultHandler implements
            LexicalHandler {
        /** Stores text content. */
        private StringBuilder builder;

        /** The current edition (source or stage tag). */
        private String currentEdition;

        /** The current file handler. */
        private FileHandler currentFileHandler;

        /** The list of supported editions. */
        private Collection<Edition> editions;

        /** The current module. */
        private Module module;

        /** The current project. */
        private Project project;

        /** The project manifest. */
        private File manifestFile;

        /** The default root path of the module. */
        private String defaultRootPath;

        @Override
        public void endElement(String uri, String localName, String name)
                throws SAXException {
            String tag = LoaderUtils.getTagName(uri, localName, name);
            
            if ("name".equals(tag)) {
                module.setName(LoaderUtils.trim(builder));
            } else if ("description".equals(tag)) {
                module.setDescription(LoaderUtils.trim(builder));
            } else if ("files-sets".equals(tag)) {
                if (currentEdition != null) {
                    currentFileHandler.setSets(LoaderUtils.trim(builder));
                }
            } else if ("files-mappers".equals(tag)) {
                if (currentEdition != null) {
                    currentFileHandler.setMappers(LoaderUtils.trim(builder));
                }
            } else if ("files-filters".equals(tag)) {
                if (currentEdition != null) {
                    currentFileHandler.setFilters(LoaderUtils.trim(builder));
                }
            } else if ("maven-misc".equals(tag)) {
                module.setMavenMisc(LoaderUtils.trim(builder));
            } else if ("provider".equals(tag)) {
                module.setProvider(LoaderUtils.trim(builder));
            } else if ("wikiUri".equals(tag)) {
                module.setWikiUri(LoaderUtils.trim(builder));
            } else if ("bundleActivator".equals(tag)) {
                module.setActivator(LoaderUtils.trim(builder));
            } else if ("rootPath".equals(tag)) {
                module.setRootPath(LoaderUtils.trim(builder));
            }
        }

        /**
         * Constructor.
         * 
         * @param project
         *            The current project.
         * @param defaultRootPath
         *            The default root path of the module.
         */
        public ModuleReader(Project project, File moduleDescriptor,
                File manifestFile, String defaultRootPath) {
            super();
            this.project = project;
            this.manifestFile = manifestFile;
            this.defaultRootPath = defaultRootPath;
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            builder.append(ch, start, length);
        }

        public void comment(char[] ch, int start, int length)
                throws SAXException {
        }

        public void endCDATA() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
            if (module != null && module.getRootPath() == null) {
                module.setRootPath(this.defaultRootPath);
            }
            if (manifestFile.isFile() && manifestFile.exists()) {
                Manifest manifest;
                try {
                    manifest = new Manifest(new FileInputStream(manifestFile));
                    String str = manifest.getMainAttributes().getValue(
                            "Import-Package");
                    if (str != null) {
                        String[] entries = str.split(",");
                        for (int i = 0; i < entries.length; i++) {
                            module.getImportedPackages().add(entries[i]);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err
                            .println("Could not read manualy set imported packages from MANIFEST.MF file "
                                    + manifestFile.getAbsolutePath());
                }
            } else {
                // System.err
                // .println("Could not read manualy set imported packages from missing MANIFEST.MF file "
                // + manifestFile.getAbsolutePath());
            }
            if (editions != null) {
                for (Edition edition : editions) {
                    // register the library dependencies in the editions
                    for (LibraryDependency dep : module.getNeededLibraries()) {
                        if (dep.getEditions().contains(edition)) {
                            register(dep, edition);
                        }
                    }
                    // register the module dependencies in the editions
                    for (ModuleDependency dep : module.getNeededModules()) {
                        if (dep.getEditions().contains(edition)) {
                            register(dep, edition);
                        }
                    }
                    // Clone the module by edition
                    boolean found = false;
                    for (int i = 0; !found && i < edition.getModules().size(); i++) {
                        if (module.equals(edition.getModules().get(i))) {
                            // Already registered via dependencies
                            edition.getModules().remove(i);
                            found = true;
                        }
                    }
                    edition.getModules().add(new Module(module, edition));
                }
            }
        }

        public void endDTD() throws SAXException {
        }

        public void endEntity(String name) throws SAXException {
        }

        /**
         * Registers a library and its dependencies in the given edition.
         * 
         * @param libraryDependency
         *            The library dependency to register.
         * @param edition
         *            The edition to register in.
         */
        private void register(LibraryDependency libraryDependency,
                Edition edition) {
            Library library = libraryDependency.getLibrary();
            if (!edition.getLibraries().contains(library)) {
                edition.getLibraries().add(library);
                for (LibraryDependency dep : library.getNeededLibraries()) {
                    register(dep, edition);
                }
            }
        }

        /**
         * Registers a library and its dependencies in the given edition.
         * 
         * @param libraryDependency
         *            The library dependency to register.
         * @param edition
         *            The edition to register in.
         */
        private void register(ModuleDependency moduleDependency, Edition edition) {
            Module module = moduleDependency.getModule();
            boolean found = false;
            for (int i = 0; !found && i < edition.getModules().size(); i++) {
                if (edition.getModules().get(i).equals(module)) {
                    found = true;
                }
            }
            if (!found) {
                edition.getModules().add(new Module(module, edition));
                for (ModuleDependency dep : module.getNeededModules()) {
                    register(dep, edition);
                }
                for (LibraryDependency dep : module.getNeededLibraries()) {
                    register(dep, edition);
                }
            }
        }

        public void startCDATA() throws SAXException {
            builder = new StringBuilder();
        }

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String name,
                Attributes attributes) throws SAXException {
            String tag = LoaderUtils.getTagName(uri, localName, name);
            builder = new StringBuilder();
            if ("module".equals(tag)) {
                module = new Module(attributes.getValue("id"));
                module.setType(attributes.getValue("type"));
                module.setPackage(attributes.getValue("package"));
                module.setSymbolicName(module.getPackage());
                editions = LoaderUtils.filterEditions(project.getEditions()
                        .values(), attributes.getValue("includes"), attributes
                        .getValue("excludes"));
                project.getModules().put(module.getId(), module);
            } else if ("dependency".equals(tag)) {
                // Add dependencies prototypes.
                String id = attributes.getValue("id");
                String type = attributes.getValue("type");
                String mavenScope = attributes.getValue("maven-scope");

                Collection<Edition> e = LoaderUtils.filterEditions(editions,
                        attributes.getValue("includes"),
                        attributes.getValue("excludes"));

                if ("library".equals(type)) {
                    Library lib = project.getLibraries().get(id);
                    // should not be null
                    if (lib != null) {
                        module.getNeededLibraries().add(
                                new LibraryDependency(lib, mavenScope, Boolean
                                        .parseBoolean(attributes
                                                .getValue("primary")), Boolean
                                        .parseBoolean(attributes
                                                .getValue("optional")), e));
                    } else {
                        System.err
                                .println("The module "
                                        + module.getId()
                                        + " declares a dependency to an unknown library: "
                                        + id);
                    }
                } else if ("module".equals(type)) {
                    Module mod = new Module(id);
                    module.getNeededModules().add(
                            new ModuleDependency(mod, mavenScope, Boolean
                                    .parseBoolean(attributes
                                            .getValue("optional")), e));
                    // Dependencies have to be re-loaded once all modules will
                    // be loaded
                }
            } else if ("distribution".equals(tag)) {
                module.getDistributions().add(attributes.getValue("id"));
            } else if ("link".equals(tag)) {
                StringBuilder builder = new StringBuilder();
                builder.append("<link");
                for (int i = 0; i < attributes.getLength(); i++) {
                    builder.append(" ");
                    builder.append(LoaderUtils.getTagName(uri, attributes.getLocalName(i), attributes.getQName(i)));
                    builder.append("=\"");
                    builder.append(attributes.getValue(i));
                    builder.append("\"");
                }
                builder.append(" />");
                module.getJavadocsLinks().add(builder.toString());
            } else if ("source".equals(tag)) {
                currentEdition = attributes.getValue("edition");
                currentFileHandler = new FileHandler();
                module.getSource().put(currentEdition, currentFileHandler);
            } else if ("stage".equals(tag)) {
                currentEdition = attributes.getValue("edition");
                currentFileHandler = new FileHandler();
                module.getStage().put(currentEdition, currentFileHandler);
                if (attributes.getValue("includesource") != null) {
                    module.getIncludeSource().put(
                            currentEdition,
                            Boolean.valueOf(attributes
                                    .getValue("includesource")));
                }
            } else if ("compile".equals(tag)) {
                if (attributes.getValue("excludes") != null) {
                    currentEdition = attributes.getValue("edition");
                    module.getCompileExcludes().put(currentEdition,
                            attributes.getValue("excludes"));
                }
            } else if ("attribute".equals(tag)) {
                // Add module attribute.
                String key = attributes.getValue("key");
                String value = attributes.getValue("value");
                if (key != null && value != null) {
                    module.getAttributes().put(key, value);
                }
            } else if ("exportedPackage".equals(tag)) {
                if (attributes.getValue("name") != null) {
                    Collection<Edition> e = LoaderUtils.filterEditions(editions,
                            attributes.getValue("includes"),
                            attributes.getValue("excludes"));
                    for (Edition edition : e) {
                        if (!module.getExportedPackages().containsKey(
                                edition.getId())) {
                            module.getExportedPackages().put(edition.getId(),
                                    new ArrayList<String>());
                        }
                        module.getExportedPackages().get(edition.getId())
                                .add(attributes.getValue("name"));
                    }
                }
            }
        }

        public void startEntity(String name) throws SAXException {
        }
    }

    /** The root directory of the modules. */
    private File modulesDir;

    /** The current project. */
    private Project project;

    /**
     * Constructor.
     * 
     * @param project
     *            The root directory of the modules.
     * @param modulesDir
     *            The root directory of the modules.
     */
    public ModuleLoader(Project project, File modulesDir) {
        super();
        this.project = project;
        this.modulesDir = modulesDir;
    }

    /**
     * Loads each module descriptor.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        File[] dirs = modulesDir.listFiles();
        for (File moduleDir : dirs) {
            if (moduleDir.isDirectory() && !moduleDir.getName().startsWith(".")) {
                File moduleFile = new File(moduleDir, "module.xml");
                if (moduleFile.exists() && moduleFile.isFile()) {
                    load(moduleFile,
                            new File(moduleDir, "META-INF/MANIFEST.MF"),
                            moduleDir.getAbsolutePath());
                } else {
                    System.err.println("Missing module.xml file inside: "
                            + moduleDir);
                }
            }
        }
    }

    /**
     * Loads a single module.
     * 
     * @param moduleDescriptor
     *            The module descriptor.
     * @throws Exception
     */
    private Module load(File moduleDescriptor, File manifest,
            String defaultRootPath) throws Exception {
        Module module = null;

        ModuleReader mr = new ModuleReader(project, moduleDescriptor, manifest,
                defaultRootPath);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(moduleDescriptor, mr);
            module = mr.module;
        } catch (Exception e) {
            System.err.println("Error while parsing '"
                    + moduleDescriptor.getAbsolutePath() + "' : "
                    + e.getMessage());
        }
        return module;
    }
}
