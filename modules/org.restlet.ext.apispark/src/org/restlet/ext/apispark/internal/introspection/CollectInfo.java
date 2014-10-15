package org.restlet.ext.apispark.internal.introspection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.ChallengeScheme;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Section;

/**
 * Created by manu on 11/10/2014.
 */
public class CollectInfo {

    private List<Resource> resources = new ArrayList<Resource>();

    private List<ChallengeScheme> schemes = new ArrayList<ChallengeScheme>();

    private Map<String, Representation> representations = new HashMap<String, Representation>();

    private Map<String, Section> sections = new HashMap<String, Section>();

    public List<Resource> getResources() {
        return new ArrayList<Resource>(resources);
    }

    public List<ChallengeScheme> getSchemes() {
        return new ArrayList<ChallengeScheme>(schemes);
    }

    public List<Representation> getRepresentations() {
        return new ArrayList<Representation>(representations.values());
    }

    public List<Section> getSections() {
        return new ArrayList<Section>(sections.values());
    }

    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /**
     * Add scheme if it does not already exist
     * 
     * @param scheme
     *            Scheme to add
     * @return true is the collection changed
     */
    public boolean addSchemeIfNotExists(ChallengeScheme scheme) {
        if (!schemes.contains(scheme)) {
            return schemes.add(scheme);
        } else {
            return false;
        }
    }

    public Representation getRepresentation(String identifier) {
        return representations.get(identifier);
    }

    public void addRepresentation(Representation representation) {
        representations.put(representation.getIdentifier(), representation);
    }

    public Section getSection(String identifier) {
        return sections.get(identifier);
    }

    public void addSection(Section section) {
        sections.put(section.getName(), section);
    }

    public void setSections(Map<String, Section> sections) {
        this.sections = sections;
    }
}
