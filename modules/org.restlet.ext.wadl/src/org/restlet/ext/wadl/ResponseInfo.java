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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.data.Status;
import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes the properties of a response associated to a parent method.
 * 
 * @author Jerome Louvel
 */
public class ResponseInfo extends DocumentedInfo {

    /**
     * List of faults (representations that denote an error condition).
     * 
     * @deprecated According to new WADL specification, the fault element has
     *             been removed.
     */
    @Deprecated
    private List<FaultInfo> faults;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** List of representations. */
    private List<RepresentationInfo> representations;

    /**
     * List of statuses associated with this response representation.
     */
    private List<Status> statuses;

    /**
     * Constructor.
     */
    public ResponseInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ResponseInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public ResponseInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ResponseInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the list of faults (representations that denote an error
     * condition).
     * 
     * @return The list of faults (representations that denote an error
     *         condition).
     * @deprecated According to new WADL specification, the fault element has
     *             been removed.
     */
    @Deprecated
    public List<FaultInfo> getFaults() {
        // Lazy initialization with double-check.
        List<FaultInfo> f = this.faults;
        if (f == null) {
            synchronized (this) {
                f = this.faults;
                if (f == null) {
                    this.faults = f = new ArrayList<FaultInfo>();
                }
            }
        }
        return f;
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
     * Returns the list of representations
     * 
     * @return The list of representations
     */
    public List<RepresentationInfo> getRepresentations() {
        // Lazy initialization with double-check.
        List<RepresentationInfo> r = this.representations;
        if (r == null) {
            synchronized (this) {
                r = this.representations;
                if (r == null) {
                    this.representations = r = new ArrayList<RepresentationInfo>();
                }
            }
        }
        return r;
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
     * Sets the list of faults (representations that denote an error condition).
     * 
     * @param faults
     *            The list of faults (representations that denote an error
     *            condition).
     * @deprecated According to new WADL specification, the fault element has
     *             been removed.
     */
    @Deprecated
    public void setFaults(List<FaultInfo> faults) {
        this.faults = faults;
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
     * Sets the list of representations
     * 
     * @param representations
     *            The list of representations
     */
    public void setRepresentations(List<RepresentationInfo> representations) {
        this.representations = representations;
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

    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        for (final RepresentationInfo representationInfo : getRepresentations()) {
            representationInfo.updateNamespaces(namespaces);
        }

        for (final RepresentationInfo faultInfo : getFaults()) {
            faultInfo.updateNamespaces(namespaces);
        }

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
        AttributesImpl attributes = new AttributesImpl();

        if ((getStatuses() != null) && !getStatuses().isEmpty()) {
            StringBuilder builder = new StringBuilder();

            for (Iterator<Status> iterator = getStatuses().iterator(); iterator
                    .hasNext();) {
                Status status = iterator.next();
                builder.append(status.getCode());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }

            attributes.addAttribute("", "status", null, "xs:string", builder
                    .toString());
        }

        if (getDocumentations().isEmpty() && getParameters().isEmpty()
                && getRepresentations().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "response", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "response", null, attributes);

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            for (RepresentationInfo representationInfo : getRepresentations()) {
                representationInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "response");
        }
    }

}
