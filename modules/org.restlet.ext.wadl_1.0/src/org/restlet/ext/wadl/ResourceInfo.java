/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
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
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes a class of closely related resources.
 * 
 * @author Jerome Louvel
 */
public class ResourceInfo extends DocumentedInfo {
    /** List of child resources. */
    private List<ResourceInfo> childResources;

    /** Identifier for that element. */
    private String identifier;

    /** List of supported methods. */
    private List<MethodInfo> methods;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** URI template for the identifier of the resource. */
    private String path;

    /** Media type for the query component of the resource URI. */
    private MediaType queryType;

    /** List of references to resource type elements. */
    private List<Reference> type;

    /**
     * Constructor.
     */
    public ResourceInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ResourceInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public ResourceInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ResourceInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the list of child resources.
     * 
     * @return The list of child resources.
     */
    public List<ResourceInfo> getChildResources() {
        // Lazy initialization with double-check.
        List<ResourceInfo> r = this.childResources;
        if (r == null) {
            synchronized (this) {
                r = this.childResources;
                if (r == null) {
                    this.childResources = r = new ArrayList<ResourceInfo>();
                }
            }
        }
        return r;
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
     * Returns the list of supported methods.
     * 
     * @return The list of supported methods.
     */
    public List<MethodInfo> getMethods() {
        // Lazy initialization with double-check.
        List<MethodInfo> m = this.methods;
        if (m == null) {
            synchronized (this) {
                m = this.methods;

                if (m == null) {
                    this.methods = m = new ArrayList<MethodInfo>();
                }
            }
        }
        return m;
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
     * Returns the URI template for the identifier of the resource.
     * 
     * @return The URI template for the identifier of the resource.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Returns the media type for the query component of the resource URI.
     * 
     * @return The media type for the query component of the resource URI.
     */
    public MediaType getQueryType() {
        return this.queryType;
    }

    /**
     * Returns the list of references to resource type elements.
     * 
     * @return The list of references to resource type elements.
     */
    public List<Reference> getType() {
        // Lazy initialization with double-check.
        List<Reference> t = this.type;
        if (t == null) {
            synchronized (this) {
                t = this.type;
                if (t == null) {
                    this.type = t = new ArrayList<Reference>();
                }
            }
        }
        return t;
    }

    /**
     * Sets the list of child resources.
     * 
     * @param resources
     *            The list of child resources.
     */
    public void setChildResources(List<ResourceInfo> resources) {
        this.childResources = resources;
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
     * Sets the list of supported methods.
     * 
     * @param methods
     *            The list of supported methods.
     */
    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
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
     * Sets the URI template for the identifier of the resource.
     * 
     * @param path
     *            The URI template for the identifier of the resource.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the media type for the query component of the resource URI.
     * 
     * @param queryType
     *            The media type for the query component of the resource URI.
     */
    public void setQueryType(MediaType queryType) {
        this.queryType = queryType;
    }

    /**
     * Sets the list of references to resource type elements.
     * 
     * @param type
     *            The list of references to resource type elements.
     */
    public void setType(List<Reference> type) {
        this.type = type;
    }

    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        for (final ParameterInfo parameterInfo : getParameters()) {
            parameterInfo.updateNamespaces(namespaces);
        }
        for (final ResourceInfo resourceInfo : getChildResources()) {
            resourceInfo.updateNamespaces(namespaces);
        }
        for (final MethodInfo methodInfo : getMethods()) {
            methodInfo.updateNamespaces(namespaces);
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

        if ((getPath() != null) && !getPath().equals("")) {
            attributes.addAttribute("", "path", null, "xs:string", getPath());
        }

        if (getQueryType() != null) {
            attributes.addAttribute("", "queryType", null, "xs:string",
                    getQueryType().getMainType());
        }
        if ((getType() != null) && !getType().isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (final Iterator<Reference> iterator = getType().iterator(); iterator
                    .hasNext();) {
                final Reference reference = iterator.next();
                builder.append(reference.toString());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            attributes.addAttribute("", "type", null, "xs:string", builder
                    .toString());
        }

        if (getChildResources().isEmpty() && getDocumentations().isEmpty()
                && getMethods().isEmpty() && getParameters().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "resource", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "resource", null, attributes);

            for (final ResourceInfo resourceInfo : getChildResources()) {
                resourceInfo.writeElement(writer);
            }

            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (final MethodInfo methodInfo : getMethods()) {
                methodInfo.writeElement(writer);
            }

            for (final ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "resource");
        }
    }

}
