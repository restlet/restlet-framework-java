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

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes a class of closely related resources.
 * 
 * @author Jerome Louvel
 */
public class ResourceInfo extends DocumentedInfo {

    /**
     * Returns a WADL description of the current resource.
     * 
     * @param applicationInfo
     *            The parent application.
     * @param resource
     *            The resource to describe.
     * @param path
     *            Path of the current resource.
     * @param info
     *            WADL description of the current resource to update.
     */
    @SuppressWarnings("deprecation")
    public static void describe(ApplicationInfo applicationInfo,
            ResourceInfo info, Object resource, String path) {
        if ((path != null) && path.startsWith("/")) {
            path = path.substring(1);
        }

        info.setPath(path);

        // Introspect the current resource to detect the allowed methods
        List<Method> methodsList = new ArrayList<Method>();

        if (resource instanceof ServerResource) {
            ((ServerResource) resource).updateAllowedMethods();
            methodsList.addAll(((ServerResource) resource).getAllowedMethods());

            if (resource instanceof WadlServerResource) {
                info.setParameters(((WadlServerResource) resource)
                        .describeParameters());

                if (applicationInfo != null) {
                    ((WadlServerResource) resource).describe(applicationInfo);
                }
            }
        } else if (resource instanceof org.restlet.resource.Resource) {
            methodsList.addAll(((org.restlet.resource.Resource) resource)
                    .getAllowedMethods());

            if (resource instanceof WadlResource) {
                info.setParameters(((WadlResource) resource)
                        .getParametersInfo());
            }
        } else if (resource instanceof Directory) {
            Directory directory = (Directory) resource;
            methodsList.add(Method.GET);

            if (directory.isModifiable()) {
                methodsList.add(Method.DELETE);
                methodsList.add(Method.PUT);
            }
        }

        Method.sort(methodsList);

        // Update the resource info with the description of the allowed methods
        List<MethodInfo> methods = info.getMethods();
        MethodInfo methodInfo;

        for (Method method : methodsList) {
            methodInfo = new MethodInfo();
            methods.add(methodInfo);
            methodInfo.setName(method);

            if (resource instanceof ServerResource) {
                if (resource instanceof WadlServerResource) {
                    WadlServerResource wsResource = (WadlServerResource) resource;

                    if (wsResource.canDescribe(method)) {
                        wsResource.describeMethod(method, methodInfo);
                    }
                } else {
                    MethodInfo.describeAnnotations(methodInfo,
                            (ServerResource) resource);
                }
            } else if (resource instanceof org.restlet.resource.Resource) {
                if (resource instanceof WadlResource) {
                    WadlResource wsResource = (WadlResource) resource;

                    if (wsResource.isDescribable(method)) {
                        wsResource.describeMethod(method, methodInfo);
                    }
                } else {
                    // Can document the list of supported variants.
                    if (Method.GET.equals(method)) {
                        ResponseInfo responseInfo = null;

                        for (Variant variant : ((org.restlet.resource.Resource) resource)
                                .getVariants()) {
                            RepresentationInfo representationInfo = new RepresentationInfo();
                            representationInfo.setMediaType(variant
                                    .getMediaType());

                            if (responseInfo == null) {
                                responseInfo = new ResponseInfo();
                                methodInfo.getResponses().add(responseInfo);
                            }

                            responseInfo.getRepresentations().add(
                                    representationInfo);
                        }
                    }
                }
            }
        }

        // Document the resource
        String title = null;
        String textContent = null;

        if (resource instanceof WadlServerResource) {
            title = ((WadlServerResource) resource).getName();
            textContent = ((WadlServerResource) resource).getDescription();
        } else if (resource instanceof WadlResource) {
            title = ((WadlResource) resource).getTitle();
        }

        if ((title != null) && !"".equals(title)) {
            DocumentationInfo doc = null;

            if (info.getDocumentations().isEmpty()) {
                doc = new DocumentationInfo();
                info.getDocumentations().add(doc);
            } else {
                info.getDocumentations().get(0);
            }

            doc.setTitle(title);
            doc.setTextContent(textContent);
        }
    }

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
     * Creates an application descriptor that wraps this resource descriptor.
     * The title of the resource, that is to say the title of its first
     * documentation tag is transfered to the title of the first documentation
     * tag of the main application tag.
     * 
     * @return The new application descriptor.
     */
    public ApplicationInfo createApplication() {
        ApplicationInfo result = new ApplicationInfo();

        if (!getDocumentations().isEmpty()) {
            String titleResource = getDocumentations().get(0).getTitle();
            if (titleResource != null && !"".equals(titleResource)) {
                DocumentationInfo doc = null;

                if (result.getDocumentations().isEmpty()) {
                    doc = new DocumentationInfo();
                    result.getDocumentations().add(doc);
                } else {
                    doc = result.getDocumentations().get(0);
                }

                doc.setTitle(titleResource);
            }
        }

        ResourcesInfo resources = new ResourcesInfo();
        result.setResources(resources);
        resources.getResources().add(this);
        return result;
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

            for (final ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            for (final MethodInfo methodInfo : getMethods()) {
                methodInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "resource");
        }
    }

}
