/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes a parameterized aspect of a parent {@link ResourceInfo},
 * {@link RequestInfo}, {@link ResponseInfo}, {@link RepresentationInfo} or
 * {@link FaultInfo} element.
 * 
 * @author Jerome Louvel
 */
public class ParameterInfo extends DocumentedInfo {

    /** Default value of this parameter. */
    private String defaultValue;

    /** Provides a fixed value for the parameter. */
    private String fixed;

    /** Identifier of this parameter element. */
    private String identifier;

    /** Link element. */
    private LinkInfo link;

    /** Name of this element. */
    private String name;

    /** List of option elements for that element. */
    private List<OptionInfo> options;

    /**
     * Path to the value of this parameter (within a parent representation).
     */
    private String path;

    /**
     * Indicates whether the parameter is single valued or may have multiple
     * values.
     */
    private boolean repeating;

    /**
     * Indicates whether the parameter is required.
     */
    private boolean required;

    /** Parameter style. */
    private ParameterStyle style;

    /** Parameter type. */
    private String type;

    /**
     * Constructor.
     */
    public ParameterInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param name
     *            The required name of the parameter.
     * @param style
     *            The required style of the parameter.
     * @param documentation
     *            A single documentation element.
     */
    public ParameterInfo(String name, ParameterStyle style,
            DocumentationInfo documentation) {
        super(documentation);
        this.name = name;
        this.style = style;
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param name
     *            The required name of the parameter.
     * @param style
     *            The required style of the parameter.
     * @param documentations
     *            The list of documentation elements.
     */
    public ParameterInfo(String name, ParameterStyle style,
            List<DocumentationInfo> documentations) {
        super(documentations);
        this.name = name;
        this.style = style;
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param name
     *            The required name of the parameter.
     * @param style
     *            The required style of the parameter.
     * @param documentation
     *            A single documentation element.
     */
    public ParameterInfo(String name, ParameterStyle style, String documentation) {
        super(documentation);
        this.name = name;
        this.style = style;
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the parameter.
     * @param required
     *            True if thes parameter is required.
     * @param type
     *            The type of the parameter.
     * @param style
     *            The style of the parameter.
     * @param documentation
     *            A single documentation element.
     */
    public ParameterInfo(String name, boolean required, String type,
            ParameterStyle style, String documentation) {
        super(documentation);
        this.name = name;
        this.required = required;
        this.style = style;
        this.type = type;
    }

    /**
     * Returns the default value of this parameter.
     * 
     * @return The default value of this parameter.
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns the fixed value for the parameter.
     * 
     * @return The fixed value for the parameter.
     */
    public String getFixed() {
        return this.fixed;
    }

    /**
     * Returns the identifier of this parameter element.
     * 
     * @return The identifier of this parameter element.
     */

    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the link element.
     * 
     * @return The link element.
     */

    public LinkInfo getLink() {
        return this.link;
    }

    /**
     * Returns the name of this element.
     * 
     * @return The name of this element.
     */

    public String getName() {
        return this.name;
    }

    /**
     * Returns the list of option elements for that element.
     * 
     * @return The list of option elements for that element.
     */

    public List<OptionInfo> getOptions() {
        // Lazy initialization with double-check.
        List<OptionInfo> o = this.options;
        if (o == null) {
            synchronized (this) {
                o = this.options;
                if (o == null) {
                    this.options = o = new ArrayList<OptionInfo>();
                }
            }
        }
        return o;
    }

    /**
     * Returns the path to the value of this parameter (within a parent
     * representation).
     * 
     * @return The path to the value of this parameter (within a parent
     *         representation).
     */

    public String getPath() {
        return this.path;
    }

    /**
     * Returns the parameter style.
     * 
     * @return The parameter style.
     */

    public ParameterStyle getStyle() {
        return this.style;
    }

    /**
     * Returns the parameter type.
     * 
     * @return The parameter type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns true if the parameter is single valued or may have multiple
     * values, false otherwise.
     * 
     * @return True if the parameter is single valued or may have multiple
     *         values, false otherwise.
     */

    public boolean isRepeating() {
        return this.repeating;
    }

    /**
     * Indicates whether the parameter is required.
     * 
     * @return True if the parameter is required, false otherwise.
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Sets the default value of this parameter.
     * 
     * @param defaultValue
     *            The default value of this parameter.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the fixed value for the parameter.
     * 
     * @param fixed
     *            The fixed value for the parameter.
     */
    public void setFixed(String fixed) {
        this.fixed = fixed;
    }

    /**
     * Sets the identifier of this parameter element.
     * 
     * @param identifier
     *            The identifier of this parameter element.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the link element.
     * 
     * @param link
     *            The link element.
     */
    public void setLink(LinkInfo link) {
        this.link = link;
    }

    /**
     * Sets the name of this element.
     * 
     * @param name
     *            The name of this element.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the list of option elements for that element.
     * 
     * @param options
     *            The list of option elements for that element.
     */
    public void setOptions(List<OptionInfo> options) {
        this.options = options;
    }

    /**
     * Sets the path to the value of this parameter (within a parent
     * representation).
     * 
     * @param path
     *            The path to the value of this parameter (within a parent
     *            representation).
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Indicates whether the parameter is single valued or may have multiple
     * values.
     * 
     * @param repeating
     *            True if the parameter is single valued or may have multiple
     *            values, false otherwise.
     */
    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    /**
     * Indicates whether the parameter is required.
     * 
     * @param required
     *            True if the parameter is required, false otherwise.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Sets the parameter style.
     * 
     * @param style
     *            The parameter style.
     */
    public void setStyle(ParameterStyle style) {
        this.style = style;
    }

    /**
     * Sets the parameter type.
     * 
     * @param type
     *            The parameter type.
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        if (getLink() != null) {
            getLink().updateNamespaces(namespaces);
        }

        for (final OptionInfo optionInfo : getOptions()) {
            optionInfo.updateNamespaces(namespaces);
        }
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        if ((getDefaultValue() != null) && !getDefaultValue().equals("")) {
            attributes.addAttribute("", "default", null, "xs:string",
                    getDefaultValue());
        }

        if ((getFixed() != null) && !getFixed().equals("")) {
            attributes.addAttribute("", "fixed", null, "xs:string", getFixed());
        }

        if ((getIdentifier() != null) && !getIdentifier().equals("")) {
            attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
        }

        if ((getPath() != null) && !getPath().equals("")) {
            attributes.addAttribute("", "path", null, "xs:string", getPath());
        }

        if (getStyle() != null) {
            attributes.addAttribute("", "style", null, "xs:string", getStyle()
                    .toString());
        }

        if ((getName() != null) && !getName().equals("")) {
            attributes.addAttribute("", "name", null, "xs:NMTOKEN", getName());
        }

        if ((getType() != null) && !getType().equals("")) {
            attributes.addAttribute("", "type", null, "xs:QName", getType());
        }

        if (isRepeating()) {
            attributes
                    .addAttribute("", "repeating", null, "xs:boolean", "true");
        }

        if (isRequired()) {
            attributes.addAttribute("", "required", null, "xs:boolean", "true");
        }

        if ((getLink() == null) && getDocumentations().isEmpty()
                && getOptions().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "param", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "param", null, attributes);

            if (getLink() != null) {
                getLink().writeElement(writer);
            }

            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (final OptionInfo optionInfo : getOptions()) {
                optionInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "param");
        }
    }
}
