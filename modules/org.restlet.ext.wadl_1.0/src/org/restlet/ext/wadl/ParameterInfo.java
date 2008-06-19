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
 * Describes a parameterized aspect of a parent {@link ResourceInfo},
 * {@link RequestInfo}, {@link ResponseInfo}, {@link RepresentationInfo} or
 * {@link FaultInfo} element.
 * 
 * @author Jerome Louvel
 */
public class ParameterInfo {

    private String defaultValue;

    private List<DocumentationInfo> documentations;

    private boolean fixed;

    private String identifier;

    private LinkInfo link;

    private List<OptionInfo> options;

    private String path;

    private boolean repeating;

    private boolean required;

    private ParameterStyle style;

    private String token;

    private String type;

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<DocumentationInfo> getDocumentations() {
        return documentations;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LinkInfo getLink() {
        return link;
    }

    public List<OptionInfo> getOptions() {
        return options;
    }

    public String getPath() {
        return path;
    }

    public ParameterStyle getStyle() {
        return style;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public boolean isRequired() {
        return required;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setLink(LinkInfo link) {
        this.link = link;
    }

    public void setOptions(List<OptionInfo> options) {
        this.options = options;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setStyle(ParameterStyle style) {
        this.style = style;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

}
