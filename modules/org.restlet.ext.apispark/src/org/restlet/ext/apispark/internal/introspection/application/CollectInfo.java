package org.restlet.ext.apispark.internal.introspection.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.ChallengeScheme;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Section;

/**
 * @author Manuel Boillod
 */
public class CollectInfo {

    private Map<String, Representation> representations = new HashMap<String, Representation>();

    private List<Resource> resources = new ArrayList<Resource>();

    private List<ChallengeScheme> schemes = new ArrayList<ChallengeScheme>();

    private Map<String, Section> sections = new HashMap<String, Section>();

    private boolean useSectionNamingPackageStrategy;

    public void addRepresentation(Representation representation) {
        representations.put(representation.getIdentifier(), representation);
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

    public void addSection(Section section) {
        sections.put(section.getName(), section);
    }

    public Representation getRepresentation(String identifier) {
        return representations.get(identifier);
    }

    public List<Representation> getRepresentations() {
        return new ArrayList<Representation>(representations.values());
    }

    public List<Resource> getResources() {
        return new ArrayList<Resource>(resources);
    }

    public List<ChallengeScheme> getSchemes() {
        return new ArrayList<ChallengeScheme>(schemes);
    }

    public Section getSection(String identifier) {
        return sections.get(identifier);
    }

    public List<Section> getSections() {
        return new ArrayList<Section>(sections.values());
    }

    public boolean isUseSectionNamingPackageStrategy() {
        return useSectionNamingPackageStrategy;
    }

    public void setSections(Map<String, Section> sections) {
        this.sections = sections;
    }

    public void setUseSectionNamingPackageStrategy(boolean useSectionNamingPackageStrategy) {
        this.useSectionNamingPackageStrategy = useSectionNamingPackageStrategy;
    }
}
