package maven;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Allow to parse POM files.
 * 
 * @author Thierry Boileau
 */
public class PomReader extends DefaultHandler {

    /** The parsed artifact. */
    private Artifact _artifact;

    /** The parsed group. */
    private Group _group;

    /** The parsed version. */
    private Version _version;

    /** The artifact id. */
    private String artifactId;

    /** The artifact's description. */
    private String description;

    /** The artifact's group id. */
    private String groupId;

    /** The map of Groups to complete. */
    Map<String, Group> groups;

    /** The artifact's name. */
    private String name;

    /** Indicates if we parse the artifact's parent. */
    boolean parent = false;

    /** Indicates if we parse the artifact's properties. */
    int depth = 0;

    /** The parent artifact id. */
    private String parentArtifactId;

    /** The parent group id. */
    private String parentGroupId;

    /** The parent version. */
    private String parentVersion;

    private StringBuilder sb;

    /** The artifact version. */
    private String version;

    /**
     * Constructor.
     * 
     * @param groups
     *            The map of groups to complete.
     */
    public PomReader(Map<String, Group> groups) {
        if (groups == null) {
            throw new IllegalArgumentException(
                    "\"groups\" argument cannot be null.");
        }
        this.groups = groups;
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (this.sb != null)
            this.sb.append(ch, start, length);
    }

    public void endDocument() throws SAXException {
        String groupId = this.groupId == null ? this.parentGroupId
                : this.groupId;
        String version = this.version == null ? this.parentVersion
                : this.version;

        // Look for the group
        if ((groupId != null) && (this.artifactId != null) && (version != null)) {
            this._group = this.groups.get(groupId);
            if (this._group == null) {
                this._group = new Group();
                this._group.setId(groupId);
                this.groups.put(groupId, this._group);
            }
            // Look for the artifact
            this._artifact = initArtifact(this._group, this.artifactId, version);
            this._artifact.setName(this.name);
            this._artifact.setDescription(this.description);
            // Look for the parent artifact
            if (this.parentArtifactId != null) {
                Artifact parent = initArtifact(this._group,
                        this.parentArtifactId, version);
                this._artifact.setParent(parent);
            }
            this._version = this._artifact.getVersions().get(version);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        String name = (localName == null) || (localName.length() == 0) ? qName
                : localName;
        if ("parent".equals(name))
            this.parent = false;
        else if (this.parent) {
            if ("groupId".equals(name))
                this.parentGroupId = format(this.sb.toString());
            else if ("artifactId".equals(name))
                this.parentArtifactId = format(this.sb.toString());
            else if ("version".equals(name))
                this.parentVersion = format(this.sb.toString());
        } else if (this.depth == 2) {
            if ("groupId".equals(name))
                this.groupId = format(this.sb.toString());
            else if ("artifactId".equals(name))
                this.artifactId = format(this.sb.toString());
            else if ("version".equals(name))
                this.version = format(this.sb.toString());
            else if ("name".equals(name))
                name = this.sb.toString();
            else if ("description".equals(name)) {
                this.description = this.sb.toString();
            }
        }
        this.depth -= 1;
        this.sb = null;
    }

    private String format(String string) {
        return string.replace("\n", "").replace("\t", "").replace(" ", "");
    }

    public Artifact getArtifact() {
        return this._artifact;
    }

    public Group getGroup() {
        return this._group;
    }

    public Version getVersion() {
        return this._version;
    }

    private Artifact initArtifact(Group group, String artifactId, String version) {
        Artifact artifact = group.getArtifacts().get(artifactId);
        if (artifact == null) {
            artifact = new Artifact();
            artifact.setId(artifactId);
            artifact.setGroup(group);
            group.getArtifacts().put(artifactId, artifact);
        }
        Version v = artifact.getVersions().get(version);
        if (v == null) {
            v = new Version();
            v.setVersion(version);
            artifact.getVersions().put(version, v);
        }

        return artifact;
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        String name = (localName == null) || (localName.length() == 0) ? qName
                : localName;
        if (this.sb == null) {
            this.sb = new StringBuilder();
        }
        if ("parent".equals(name)) {
            this.parent = true;
        }

        this.depth += 1;
    }
}