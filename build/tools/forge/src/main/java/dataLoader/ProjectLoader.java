package dataLoader;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dataLoader.data.Distribution;
import dataLoader.data.Edition;
import dataLoader.data.Project;

/**
 * Loads a project from a project descriptor.
 * 
 */
public class ProjectLoader {

    /**
     * Inner SAX content handler.
     * 
     */
    private static class ProjectReader extends DefaultHandler {
        /** Stores text content. */
        private StringBuilder builder;

        /** The current detected edition. */
        private Edition edition;

        /** The current project. */
        private Project project;

        /**
         * Constructor.
         * 
         * @param project
         *            The current project to update.
         */
        public ProjectReader(Project project) {
            super();
            this.project = project;
            this.edition = null;
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            builder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String name)
                throws SAXException {
            String tag = LoaderUtils.getTagName(uri, localName, name);
            if ("description".equals(tag)) {
                this.edition.setDescription(LoaderUtils.trim(builder));
            } else if ("full".equals(tag)) {
                this.edition.setFullLabel(LoaderUtils.trim(builder));
            } else if ("medium".equals(tag)) {
                this.edition.setMediumLabel(LoaderUtils.trim(builder));
            } else if ("short".equals(tag)) {
                this.edition.setShortLabel(LoaderUtils.trim(builder));
            } else if ("postSource".equals(tag)) {
                this.edition.setPostSource(LoaderUtils.trim(builder));
            } else if ("core".equals(tag)) {
                this.edition.setPackageCore(LoaderUtils.trim(builder));
            } else if ("engine".equals(tag)) {
                this.edition.setPackageEngine(LoaderUtils.trim(builder));
            } else if ("extension".equals(tag)) {
                this.edition.setPackageExtension(LoaderUtils.trim(builder));
            } else if ("wikiUri".equals(tag)) {
                this.edition.setWikiUri(LoaderUtils.trim(builder));
            }
        }

        @Override
        public void startElement(String uri, String localName, String name,
                Attributes attributes) throws SAXException {
            String tag = LoaderUtils.getTagName(uri, localName, name);
            builder = new StringBuilder();
            if ("edition".equals(tag)) {
                this.edition = new Edition(attributes.getValue("id"));
                if (attributes.getValue("translate") != null) {
                    this.edition.setTranslatePackages(Boolean
                            .parseBoolean(attributes.getValue("translate")));
                }
                project.getEditions().put(this.edition.getId(), this.edition);
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
                this.edition.getJavadocsLinks().add(builder.toString());
            } else if ("distribution".equals(tag)) {
                Distribution d = new Distribution(attributes.getValue("id"));
                this.edition.getDistributions().add(d);
            }

        }
    }

    /** The current project. */
    private Project project;

    /** The path to the project descriptor. */
    private File projectDescriptor;

    /**
     * Constructor.
     * 
     * @param projectDescriptor
     *            The path to the project descriptor.
     */
    public ProjectLoader(File projectDescriptor) {
        super();
        this.project = new Project();
        this.projectDescriptor = projectDescriptor;
    }

    /**
     * Returns the project.
     * 
     * @return The project.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Loads the project from the project descriptor.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(this.projectDescriptor, new ProjectReader(
                    this.project));
        } catch (Exception e) {
            System.err.println("Error while parsing '"
                    + this.projectDescriptor.getAbsolutePath() + "' : "
                    + e.getMessage());
        }
    }

}
