package maven;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String id;

    private Map<String, Artifact> artifacts;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Artifact> getArtifacts() {
        if (this.artifacts == null) {
            this.artifacts = new HashMap<String, Artifact>();
        }
        return this.artifacts;
    }

    public void setArtifacts(Map<String, Artifact> artifacts) {
        this.artifacts = artifacts;
    }
}