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

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;

/**
 * Describres a variant representation for a target resource.
 * 
 * @author Jerome Louvel
 */
public class RepresentationInfo {

    private List<DocumentationInfo> documentations;

    private String identifier;

    private MediaType mediaType;

    private List<ParameterInfo> parameters;

    private List<Reference> profiles;

    private List<Status> statuses;

    private String xmlElement;

    public List<DocumentationInfo> getDocumentations() {
        return documentations;
    }

    public String getIdentifier() {
        return identifier;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public List<Reference> getProfiles() {
        return profiles;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public String getXmlElement() {
        return xmlElement;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    public void setProfiles(List<Reference> profiles) {
        this.profiles = profiles;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public void setXmlElement(String xmlElement) {
        this.xmlElement = xmlElement;
    }

}
