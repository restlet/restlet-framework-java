package org.restlet.ext.swagger;

import java.util.List;

public class ApplicationInfo {

    private List<RepresentationInfo> models;
    
    public List<RepresentationInfo> getModels() {
        return models;
    }

    public void setModels(List<RepresentationInfo> models) {
        this.models = models;
    }

    public List<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(List<ResourceInfo> resources) {
        this.resources = resources;
    }

    private List<ResourceInfo> resources;
    
}
