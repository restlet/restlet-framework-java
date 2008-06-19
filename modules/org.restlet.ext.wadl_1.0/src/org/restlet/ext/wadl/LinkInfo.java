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
 * Allows description of links between representations and resources.
 * 
 * @author Jerome Louvel
 */
public class LinkInfo {

    private List<DocumentationInfo> documentations;

    private String relationship;

    private ResourceTypeInfo resourceType;

    private String reverseRelationship;

    public List<DocumentationInfo> getDocumentations() {
        return documentations;
    }

    public String getRelationship() {
        return relationship;
    }

    public ResourceTypeInfo getResourceType() {
        return resourceType;
    }

    public String getReverseRelationship() {
        return reverseRelationship;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setResourceType(ResourceTypeInfo resourceType) {
        this.resourceType = resourceType;
    }

    public void setReverseRelationship(String reverseRelationship) {
        this.reverseRelationship = reverseRelationship;
    }

}
