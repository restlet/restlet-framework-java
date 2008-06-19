/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.wadl;

import java.util.List;

/**
 * Root of a WADL description document.
 * 
 * @author Jerome Louvel
 */
public class ApplicationInfo {

    private List<DocumentationInfo> documentations;

    private List<FaultInfo> faults;

    private GrammarsInfo grammars;

    private List<MethodInfo> methods;

    private List<RepresentationInfo> representations;

    private ResourcesInfo resources;

    private List<ResourceTypeInfo> resourceTypes;

    public List<DocumentationInfo> getDocumentations() {
        return documentations;
    }

    public List<FaultInfo> getFaults() {
        return faults;
    }

    public GrammarsInfo getGrammars() {
        return grammars;
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public List<RepresentationInfo> getRepresentations() {
        return representations;
    }

    public ResourcesInfo getResources() {
        return resources;
    }

    public List<ResourceTypeInfo> getResourceTypes() {
        return resourceTypes;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setFaults(List<FaultInfo> faults) {
        this.faults = faults;
    }

    public void setGrammars(GrammarsInfo grammars) {
        this.grammars = grammars;
    }

    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
    }

    public void setRepresentations(List<RepresentationInfo> representations) {
        this.representations = representations;
    }

    public void setResources(ResourcesInfo resources) {
        this.resources = resources;
    }

    public void setResourceTypes(List<ResourceTypeInfo> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

}
