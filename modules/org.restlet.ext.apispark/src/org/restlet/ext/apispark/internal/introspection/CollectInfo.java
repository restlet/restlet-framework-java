package org.restlet.ext.apispark.internal.introspection;

import org.restlet.data.ChallengeScheme;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;

import java.util.*;

/**
 * Created by manu on 11/10/2014.
 */
public class CollectInfo {

    private List<Resource> resources = new ArrayList<Resource>();

    private List<ChallengeScheme> schemes = new ArrayList<ChallengeScheme>();

    private Map<String, Representation> representations = new HashMap<String, Representation>();

    public List<Resource> getResources() {
        return new ArrayList<Resource>(resources);
    }

    public List<ChallengeScheme> getSchemes() {
        return new ArrayList<ChallengeScheme>(schemes);
    }

    public List<Representation> getRepresentations() {
        return new ArrayList<Representation>(representations.values());
    }

    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /**
     * Add scheme if it does not already exist
     * @param scheme
     *      Scheme to add
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

    public void addRepresentation(String identifier, Representation representation) {
        representations.put(identifier, representation);
    }
}
