/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

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

    private Map<String, Representation> representations = new HashMap<>();

    private List<Resource> resources = new ArrayList<>();

    private List<ChallengeScheme> schemes = new ArrayList<>();

    private Map<String, Section> sections = new HashMap<>();

    private boolean useSectionNamingPackageStrategy;

    public void addRepresentation(Representation representation) {
        representations.put(representation.getName(), representation);
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
        return new ArrayList<>(representations.values());
    }

    public List<Resource> getResources() {
        return new ArrayList<>(resources);
    }

    public List<ChallengeScheme> getSchemes() {
        return new ArrayList<>(schemes);
    }

    public Section getSection(String identifier) {
        return sections.get(identifier);
    }

    public List<Section> getSections() {
        return new ArrayList<>(sections.values());
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
