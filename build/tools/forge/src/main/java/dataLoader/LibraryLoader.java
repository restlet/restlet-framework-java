package dataLoader;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dataLoader.data.Library;
import dataLoader.data.LibraryDependency;
import dataLoader.data.LibraryPackage;
import dataLoader.data.MavenRepository;
import dataLoader.data.Project;

/**
 * Load libraries descriptors.
 * 
 */
public class LibraryLoader {

    /**
     * Inner SAX content handler.
     * 
     */
    private static class LibraryReader extends DefaultHandler {
        /** Stores text content. */
        private StringBuilder builder;

        /** The current library. */
        private Library library;

        /** The current package. */
        private LibraryPackage pack;

        /** The default root path of the library. */
        private String defaultRootPath;

        private boolean mavenReleaseRepository;

        private boolean mavenSnapshotRepository;

        @Override
        public void endElement(String uri, String localName, String name)
                throws SAXException {
            String tag = LoaderUtils.getTagName(uri, localName, name);
            if ("package".equals(tag)) {
                pack = null;
            } else if ("groupId".equals(tag)) {
                pack.setMavenGroupId(LoaderUtils.trim(builder));
            } else if ("artifactId".equals(tag)) {
                pack.setMavenArtifactId(LoaderUtils.trim(builder));
            } else if ("version".equals(tag)) {
                if (pack == null) {
                    library.setMinorVersion(LoaderUtils.trim(builder));
                } else {
                    pack.setMavenVersion(LoaderUtils.trim(builder));
                }
            } else if ("release".equals(tag)) {
                library.setVersionSuffix(LoaderUtils.trim(builder));
            } else if ("homeUri".equals(tag)) {
                library.setHomeUri(LoaderUtils.trim(builder));
            } else if ("downloadUri".equals(tag)) {
                library.setDownloadUri(LoaderUtils.trim(builder));
            } else if ("maven-misc".equals(tag)) {
                library.setMavenMisc(LoaderUtils.trim(builder));
            } else if ("name".equals(tag)) {
                if (mavenReleaseRepository) {
                    library.getMavenReleaseRepository().setName(
                            LoaderUtils.trim(builder));
                } else if (mavenSnapshotRepository) {
                    library.getMavenSnapshotRepository().setName(
                            LoaderUtils.trim(builder));
                } else {
                    library.setName(LoaderUtils.trim(builder));
                }
            } else if ("description".equals(tag)) {
                library.setDescription(LoaderUtils.trim(builder));
            } else if ("provider".equals(tag)) {
                library.setProvider(LoaderUtils.trim(builder));
            } else if ("bundleActivator".equals(tag)) {
                library.setActivator(LoaderUtils.trim(builder));
            } else if ("rootPath".equals(tag)) {
                library.setRootPath(LoaderUtils.trim(builder));
            } else if ("id".equals(tag)) {
                if (mavenReleaseRepository) {
                    library.getMavenReleaseRepository().setId(
                            LoaderUtils.trim(builder));
                } else if (mavenSnapshotRepository) {
                    library.getMavenSnapshotRepository().setId(
                            LoaderUtils.trim(builder));
                }
            } else if ("url".equals(tag)) {
                if (mavenReleaseRepository) {
                    library.getMavenReleaseRepository().setUrl(
                            LoaderUtils.trim(builder));
                } else if (mavenSnapshotRepository) {
                    library.getMavenSnapshotRepository().setUrl(
                            LoaderUtils.trim(builder));
                }
            } else if ("repository".equals(tag)) {
                mavenReleaseRepository = false;
            } else if ("snapshotRepository".equals(tag)) {
                mavenSnapshotRepository = false;
            }
        }

        @Override
        public void endDocument() throws SAXException {
            if (library != null && library.getRootPath() == null) {
                library.setRootPath(defaultRootPath);
            }
            if (library.getDistributions().contains("maven")) {
                for (LibraryPackage p : library.getPackages()) {
                    p.setMavenGroupId(null);
                    if (p.getMavenArtifactId() == null) {
                        p.setMavenArtifactId("org.restlet.lib." + p.getName());
                    } else {
                        p.setMavenArtifactId("org.restlet.lib."
                                + p.getMavenArtifactId());
                    }
                    if (p.getMavenVersion() == null) {
                        p.setMavenVersion(library.getMinorVersion());
                    }
                    if (library.getVersionSuffix() != null) {
                        p.setMavenVersion(p.getMavenVersion() + "."
                                + library.getVersionSuffix());
                    }
                }
            } else {
                for (LibraryPackage p : library.getPackages()) {
                    if (p.getMavenGroupId() == null) {
                        p.setMavenGroupId(p.getName());
                    }
                    if (p.getMavenArtifactId() == null) {
                        p.setMavenArtifactId(p.getName());
                    }
                    if (p.getMavenVersion() == null) {
                        if (library.getVersionSuffix() != null) {
                            p.setMavenVersion(library.getMinorVersion() + "."
                                    + library.getVersionSuffix());
                        } else {
                            p.setMavenVersion(library.getMinorVersion());
                        }
                    }
                }
            }
            super.endDocument();
        }

        /** The current project. */
        private Project project;

        /**
         * Constructor.
         * 
         * @param project
         *            The current project.
         * @param defaultRootPath
         *            The default root path of the library.
         */
        public LibraryReader(Project project, String defaultRootPath) {
            super();
            this.project = project;
            this.defaultRootPath = defaultRootPath;
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            builder.append(ch, start, length);
        }

        public Library getLibrary() {
            return library;
        }

        @Override
        public void startElement(String uri, String localName, String name,
                Attributes attributes) throws SAXException {
            builder = new StringBuilder();

            String tag = LoaderUtils.getTagName(uri, localName, name);

            if ("library".equals(tag)) {
                library = new Library(attributes.getValue("id"));
                if ("raml".equals(library.getId())) {
                    System.out.println("raml");
                }
                project.getLibraries().put(library.getId(), library);
                if (attributes.getValue("dir") != null) {
                    library.setSymbolicName(attributes.getValue("dir"));
                } else {
                    library.setSymbolicName(attributes.getValue("symbolicName"));
                }
                if (library.getSymbolicName() == null) {
                    throw new IllegalArgumentException("The library '"
                            + library.getId()
                            + "' must specify a 'symbolicName' attribute.");
                }
            } else if ("package".equals(tag)) {
                pack = new LibraryPackage(attributes.getValue("id"));
                if (pack.getId() == null || "".equals(pack.getId())) {
                    pack.setId(library.getId());
                }
                pack.setName(attributes.getValue("name"));
                library.getPackages().add(pack);
            } else if ("distribution".equals(tag)) {
                library.getDistributions().add(attributes.getValue("id"));
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
                library.getJavadocsLinks().add(builder.toString());
            } else if ("dependency".equals(tag)) {
                // Add dependencies prototypes.
                String id = attributes.getValue("id");
                String type = attributes.getValue("type");
                String mavenScope = attributes.getValue("mavenScope");
                if ("library".equals(type)) {
                    Library lib = new Library(id);
                    library.getNeededLibraries().add(
                            new LibraryDependency(lib, mavenScope, Boolean
                                    .parseBoolean(attributes
                                            .getValue("primary")), Boolean
                                    .parseBoolean(attributes
                                            .getValue("optional")), null));
                }
            } else if ("attribute".equals(tag)) {
                // Add library attribute.
                String key = attributes.getValue("key");
                String value = attributes.getValue("value");
                if (key != null && value != null) {
                    library.getAttributes().put(key, value);
                }
            } else if ("exportedPackage".equals(tag)) {
                if (attributes.getValue("name") != null) {
                    library.getExportedPackages().add(
                            attributes.getValue("name"));
                }
            } else if ("repository".equals(tag)) {
                mavenReleaseRepository = true;
                library.setMavenReleaseRepository(new MavenRepository());
            } else if ("snapshotRepository".equals(tag)) {
                mavenSnapshotRepository = true;
                library.setMavenSnapshotRepository(new MavenRepository());
            }
        }
    }

    /** The root directory of the libraries. */
    private File librariesDir;

    /** The current project. */
    private Project project;

    /**
     * Constructor.
     * 
     * @param project
     *            The current project.
     * @param librariesDir
     *            The root directory of the libraries.
     */
    public LibraryLoader(Project project, File librariesDir) {
        super();
        this.project = project;
        this.librariesDir = librariesDir;
    }

    /**
     * Loads each library descriptor.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        File[] dirs = librariesDir.listFiles();
        for (File file : dirs) {
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                Library library = null;
                File libraryFile = new File(file, "library.xml");
                if (libraryFile.isFile() && libraryFile.exists()) {
                    library = load(libraryFile, file.getAbsolutePath());
                    if (library == null) {
                        System.err.println("Cannot load library from "
                                + libraryFile.getCanonicalPath());
                    }
                } else {
                    System.err.println("Missing library.xml file inside: "
                            + file);
                }
            } else if (file.getName().endsWith(".xml")) {
                Library library = load(file, null);
                if (library != null && library.getRootPath() == null) {
                    System.err.println("Set proper library's root path from "
                            + file.getAbsolutePath());
                } else if (library == null) {
                    System.err.println("Cannot load library from "
                            + file.getCanonicalPath());
                }
            }
        }
    }

    /**
     * Load a single library.
     * 
     * @param libraryDescriptor
     *            The library descriptor.
     * @throws Exception
     */
    private Library load(File libraryDescriptor, String defaultRootPath)
            throws Exception {
        Library library = null;
        LibraryReader reader = new LibraryReader(project, defaultRootPath);

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(libraryDescriptor, reader);
            library = reader.getLibrary();
        } catch (Exception e) {
            System.err.println("Error while parsing '"
                    + libraryDescriptor.getAbsolutePath() + "' : "
                    + e.getMessage());
        }
        return library;
    }
}
