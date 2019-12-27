package maven;

import java.util.HashMap;
import java.util.Map;

public class Artifact {
    private String description;

    private Group group;

    private String id;

    private Version lastestVersion;

    private String name;

    private Artifact parent;

    private Map<String, Version> versions;

    public String getDescription() {
        return this.description;
    }

    public Group getGroup() {
        return this.group;
    }

    public String getId() {
        return this.id;
    }

    public Version getLastestVersion() {
        return this.lastestVersion;
    }

    public String getName() {
        return this.name;
    }

    public Artifact getParent() {
        return this.parent;
    }

    public Map<String, Version> getVersions() {
        if (this.versions == null) {
            this.versions = new HashMap<String, Version>();
        }
        return this.versions;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastestVersion(Version lastestVersion) {
        this.lastestVersion = lastestVersion;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(Artifact parent) {
        this.parent = parent;
    }
}