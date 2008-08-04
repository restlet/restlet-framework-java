/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describres a variant representation for a target resource.
 * 
 * @author Jerome Louvel
 */
public class RepresentationInfo extends DocumentedInfo {

    /** Identifier for that element. */
    private String identifier;

    /** Media type of that element. */
    private MediaType mediaType;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** List of locations of one or more meta data profiles. */
    private List<Reference> profiles;

    /**
     * List of statuses associated with this response representation.
     */
    private List<Status> statuses;

    /** Qualified name of the root element for this XML-based representation. */
    private String xmlElement;

    /**
     * Constructor.
     */
    public RepresentationInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public RepresentationInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public RepresentationInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public RepresentationInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the identifier for that element.
     * 
     * @return The identifier for that element.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the media type of that element.
     * 
     * @return The media type of that element.
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }

    /**
     * Returns the list of parameters.
     * 
     * @return The list of parameters.
     */
    public List<ParameterInfo> getParameters() {
        // Lazy initialization with double-check.
        List<ParameterInfo> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null) {
                    this.parameters = p = new ArrayList<ParameterInfo>();
                }
            }
        }
        return p;
    }

    /**
     * Returns the list of locations of one or more meta data profiles.
     * 
     * @return The list of locations of one or more meta data profiles.
     */
    public List<Reference> getProfiles() {
        // Lazy initialization with double-check.
        List<Reference> p = this.profiles;
        if (p == null) {
            synchronized (this) {
                p = this.profiles;
                if (p == null) {
                    this.profiles = p = new ArrayList<Reference>();
                }
            }
        }
        return p;
    }

    /**
     * Returns the list of statuses associated with this response
     * representation.
     * 
     * @return The list of statuses associated with this response
     *         representation.
     */
    public List<Status> getStatuses() {
        // Lazy initialization with double-check.
        List<Status> s = this.statuses;
        if (s == null) {
            synchronized (this) {
                s = this.statuses;
                if (s == null) {
                    this.statuses = s = new ArrayList<Status>();
                }
            }
        }
        return s;
    }

    /**
     * Returns the qualified name of the root element for this XML-based
     * representation.
     * 
     * @return The qualified name of the root element for this XML-based
     *         representation.
     */
    public String getXmlElement() {
        return this.xmlElement;
    }

    /**
     * Sets the identifier for that element.
     * 
     * @param identifier
     *            The identifier for that element.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the media type of that element.
     * 
     * @param mediaType
     *            The media type of that element.
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *            The list of parameters.
     */
    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the list of locations of one or more meta data profiles.
     * 
     * @param profiles
     *            The list of locations of one or more meta data profiles.
     */
    public void setProfiles(List<Reference> profiles) {
        this.profiles = profiles;
    }

    /**
     * Sets the list of statuses associated with this response representation.
     * 
     * @param statuses
     *            The list of statuses associated with this response
     *            representation.
     */
    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    /**
     * Sets the qualified name of the root element for this XML-based
     * representation.
     * 
     * @param xmlElement
     *            The qualified name of the root element for this XML-based
     *            representation.
     */
    public void setXmlElement(String xmlElement) {
        this.xmlElement = xmlElement;
    }

    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        for (final ParameterInfo parameterInfo : getParameters()) {
            parameterInfo.updateNamespaces(namespaces);
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
        if ((getIdentifier() != null) && !getIdentifier().equals("")) {
            attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
        }
        if (getMediaType() != null) {
            attributes.addAttribute("", "mediaType", null, "xs:string",
                    getMediaType().toString());
        }
        if ((getProfiles() != null) && !getProfiles().isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (final Iterator<Reference> iterator = getProfiles().iterator(); iterator
                    .hasNext();) {
                final Reference reference = iterator.next();
                builder.append(reference.toString());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            attributes.addAttribute("", "profile", null, "xs:string", builder
                    .toString());
        }
        if ((getStatuses() != null) && !getStatuses().isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (final Iterator<Status> iterator = getStatuses().iterator(); iterator
                    .hasNext();) {
                final Status status = iterator.next();
                builder.append(status.getCode());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            attributes.addAttribute("", "status", null, "xs:string", builder
                    .toString());
        }
        if ((getXmlElement() != null) && !getXmlElement().equals("")) {
            attributes.addAttribute("", "element", null, "xs:QName",
                    getXmlElement());
        }

        if (getDocumentations().isEmpty() && getParameters().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "representation", null,
                    attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "representation", null,
                    attributes);

            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (final ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "representation");
        }
    }

}
