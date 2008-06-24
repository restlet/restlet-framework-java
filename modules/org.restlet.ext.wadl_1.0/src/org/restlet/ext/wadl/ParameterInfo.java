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

import java.util.ArrayList;
import java.util.List;

import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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

    private String fixed;

    private String identifier;

    private LinkInfo link;

    private List<OptionInfo> options;

    private String path;

    private boolean repeating;

    private boolean required;

    private ParameterStyle style;

    private String name;

    private String type;

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<DocumentationInfo> getDocumentations() {
        // Lazy initialization with double-check.
        List<DocumentationInfo> d = this.documentations;
        if (d == null) {
            synchronized (this) {
                d = this.documentations;
                if (d == null)
                    this.documentations = d = new ArrayList<DocumentationInfo>();
            }
        }
        return d;
    }

    public String getFixed() {
        return fixed;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LinkInfo getLink() {
        return link;
    }

    public List<OptionInfo> getOptions() {
        // Lazy initialization with double-check.
        List<OptionInfo> o = this.options;
        if (o == null) {
            synchronized (this) {
                o = this.options;
                if (o == null)
                    this.options = o = new ArrayList<OptionInfo>();
            }
        }
        return o;
    }

    public String getPath() {
        return path;
    }

    public ParameterStyle getStyle() {
        return style;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
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

    public void setFixed(String fixed) {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();

        if (getDefaultValue() != null && !getDefaultValue().equals("")) {
            attributes.addAttribute("", "default", null, "xs:string",
                    getDefaultValue());
        }

        if (getFixed() != null && !getFixed().equals("")) {
            attributes.addAttribute("", "fixed", null, "xs:string", getFixed());
        }

        if (getIdentifier() != null && !getIdentifier().equals("")) {
            attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
        }

        if (getPath() != null && !getPath().equals("")) {
            attributes.addAttribute("", "path", null, "xs:string", getPath());
        }

        if (getStyle() != null) {
            attributes.addAttribute("", "style", null, "xs:string", getStyle()
                    .toString());
        }

        if (getName() != null && !getName().equals("")) {
            attributes.addAttribute("", "name", null, "xs:NMTOKEN", getName());
        }

        if (getType() != null && !getType().equals("")) {
            attributes.addAttribute("", "type", null, "xs:QName", getType());
        }

        if (isRepeating()) {
            attributes
                    .addAttribute("", "repeating", null, "xs:boolean", "true");
        }

        if (isRequired()) {
            attributes.addAttribute("", "required", null, "xs:boolean", "true");
        }

        writer.startElement("", "param", null, attributes);

        if (getLink() != null) {
            getLink().writeElement(writer);
        }

        if (getDocumentations() != null) {
            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }
        }

        if (getOptions() != null) {
            for (OptionInfo optionInfo : getOptions()) {
                optionInfo.writeElement(writer);
            }
        }
        writer.endElement("", "param");
    }

}
