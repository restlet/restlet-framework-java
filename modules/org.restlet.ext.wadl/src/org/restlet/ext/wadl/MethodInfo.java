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

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.service.MetadataService;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes the expected requests and responses of a resource method.
 * 
 * @author Jerome Louvel
 */
public class MethodInfo extends DocumentedInfo {

    /**
     * Automatically describe a method by discovering the resource's
     * annotations.
     * 
     * @param info
     *            The method description to update.
     * @param resource
     *            The server resource to describe.
     */
    public static void describeAnnotations(MethodInfo info,
            ServerResource resource) {
        // Loop over the annotated Java methods
        MetadataService metadataService = resource.getMetadataService();
        List<AnnotationInfo> annotations = resource.isAnnotated() ? AnnotationUtils
                .getAnnotations(resource.getClass())
                : null;

        if (annotations != null && metadataService != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                if (info.getName().equals(annotationInfo.getRestletMethod())) {
                    // Describe the request
                    Class<?>[] classes = annotationInfo.getJavaInputTypes();

                    List<Variant> requestVariants = annotationInfo
                            .getRequestVariants(resource.getMetadataService(),
                                    resource.getConverterService());

                    if (requestVariants != null) {
                        for (Variant variant : requestVariants) {
                            if ((variant.getMediaType() != null)
                                    && ((info.getRequest() == null) || !info
                                            .getRequest().getRepresentations()
                                            .contains(variant))) {
                                RepresentationInfo representationInfo = null;

                                if (info.getRequest() == null) {
                                    info.setRequest(new RequestInfo());
                                }

                                if (resource instanceof WadlServerResource) {
                                    representationInfo = ((WadlServerResource) resource)
                                            .describe(info, info.getRequest(),
                                                    classes[0], variant);
                                } else {
                                    representationInfo = new RepresentationInfo(
                                            variant);
                                }

                                info.getRequest().getRepresentations().add(
                                        representationInfo);
                            }
                        }
                    }

                    // Describe the response
                    Class<?> outputClass = annotationInfo.getJavaOutputType();

                    if (outputClass != null) {
                        List<Variant> responseVariants = annotationInfo
                                .getResponseVariants(resource
                                        .getMetadataService(), resource
                                        .getConverterService());

                        if (responseVariants != null) {
                            for (Variant variant : responseVariants) {
                                if ((variant.getMediaType() != null)
                                        && !info.getResponse()
                                                .getRepresentations().contains(
                                                        variant)) {
                                    RepresentationInfo representationInfo = null;

                                    if (resource instanceof WadlServerResource) {
                                        representationInfo = ((WadlServerResource) resource)
                                                .describe(info, info
                                                        .getResponse(),
                                                        outputClass, variant);
                                    } else {
                                        representationInfo = new RepresentationInfo(
                                                variant);
                                    }

                                    info.getResponse().getRepresentations()
                                            .add(representationInfo);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Identifier for the method. */
    private String identifier;

    /** Name of the method. */
    private Method name;

    /** Describes the input to the method. */
    private RequestInfo request;

    /** Describes the output of the method. */
    private List<ResponseInfo> responses;

    /** Reference to a method definition element. */
    private Reference targetRef;

    /**
     * Constructor.
     */
    public MethodInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public MethodInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public MethodInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public MethodInfo(String documentation) {
        super(documentation);
    }

    /**
     * Adds a new fault to the response.
     * 
     * @param status
     *            The associated status code.
     * @param mediaType
     *            The fault representation's media type.
     * @param documentation
     *            A single documentation element.
     * @return The created fault description.
     * @deprecated Use the {@link ResponseInfo#getRepresentations()} method
     *             instead.
     */
    @Deprecated
    public RepresentationInfo addFault(Status status, MediaType mediaType,
            String documentation) {
        RepresentationInfo result = new RepresentationInfo(documentation);
        result.setMediaType(mediaType);
        getResponse().getStatuses().add(status);
        getResponse().getRepresentations().add(result);
        return result;
    }

    /**
     * Adds a new request parameter.
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
     * @return The created parameter description.
     * @deprecated Use {@link RequestInfo#getParameters()} instead.
     */
    @Deprecated
    public ParameterInfo addRequestParameter(String name, boolean required,
            String type, ParameterStyle style, String documentation) {
        ParameterInfo result = new ParameterInfo(name, required, type, style,
                documentation);

        if (getRequest() == null) {
            setRequest(new RequestInfo());
        }

        getRequest().getParameters().add(result);
        return result;
    }

    /**
     * Adds a new request representation based on a given variant.
     * 
     * @param variant
     *            The variant to describe.
     * @return The created representation description.
     * @deprecated Use {@link RequestInfo#getRepresentations()} instead.
     */
    @Deprecated
    public RepresentationInfo addRequestRepresentation(Variant variant) {
        RepresentationInfo result = new RepresentationInfo(variant);

        if (getRequest() == null) {
            setRequest(new RequestInfo());
        }

        getRequest().getRepresentations().add(result);
        return result;
    }

    /**
     * Adds a new response parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param required
     *            True if the parameter is required.
     * @param type
     *            The type of the parameter.
     * @param style
     *            The style of the parameter.
     * @param documentation
     *            A single documentation element.
     * @return The created parameter description.
     * @deprecated Use the {@link ResponseInfo#getParameters()} method instead.
     */
    @Deprecated
    public ParameterInfo addResponseParameter(String name, boolean required,
            String type, ParameterStyle style, String documentation) {
        ParameterInfo result = new ParameterInfo(name, required, type, style,
                documentation);
        getResponse().getParameters().add(result);
        return result;
    }

    /**
     * Adds a new response representation based on a given variant.
     * 
     * @param variant
     *            The variant to describe.
     * @return The created representation description.
     * @deprecated Use {@link ResponseInfo#getRepresentations()} instead.
     */
    @Deprecated
    public RepresentationInfo addResponseRepresentation(Variant variant) {
        RepresentationInfo result = new RepresentationInfo(variant);
        getResponse().getRepresentations().add(result);
        return result;
    }

    /**
     * Returns the identifier for the method.
     * 
     * @return The identifier for the method.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the name of the method.
     * 
     * @return The name of the method.
     */

    public Method getName() {
        return this.name;
    }

    /**
     * Returns the input to the method.
     * 
     * @return The input to the method.
     */
    public RequestInfo getRequest() {
        return this.request;
    }

    /**
     * Returns the last added response of the method.
     * 
     * @return The last added response of the method.
     */
    public ResponseInfo getResponse() {
        if (getResponses().isEmpty()) {
            getResponses().add(new ResponseInfo());
        }

        return getResponses().get(getResponses().size() - 1);
    }

    /**
     * Returns the output of the method.
     * 
     * @return The output of the method.
     */
    public List<ResponseInfo> getResponses() {
        // Lazy initialization with double-check.
        List<ResponseInfo> r = this.responses;
        if (r == null) {
            synchronized (this) {
                r = this.responses;
                if (r == null) {
                    this.responses = r = new ArrayList<ResponseInfo>();
                }
            }
        }
        return r;
    }

    /**
     * Returns the reference to a method definition element.
     * 
     * @return The reference to a method definition element.
     */
    public Reference getTargetRef() {
        return this.targetRef;
    }

    /**
     * Sets the identifier for the method.
     * 
     * @param identifier
     *            The identifier for the method.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the name of the method.
     * 
     * @param name
     *            The name of the method.
     */
    public void setName(Method name) {
        this.name = name;
    }

    /**
     * Sets the input to the method.
     * 
     * @param request
     *            The input to the method.
     */
    public void setRequest(RequestInfo request) {
        this.request = request;
    }

    /**
     * Sets the output of the method.
     * 
     * @param response
     *            The output of the method.
     * @deprecated Use the {@link #getResponses()} or
     *             {@link #setResponses(List)} methods instead.
     */
    @Deprecated
    public void setResponsee(ResponseInfo response) {
        setResponses(new ArrayList<ResponseInfo>());
        getResponses().add(response);
    }

    /**
     * Sets the output of the method.
     * 
     * @param responses
     *            The output of the method.
     */
    public void setResponses(List<ResponseInfo> responses) {
        this.responses = responses;
    }

    /**
     * Sets the reference to a method definition element.
     * 
     * @param targetRef
     *            The reference to a method definition element.
     */
    public void setTargetRef(Reference targetRef) {
        this.targetRef = targetRef;
    }

    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        if (getRequest() != null) {
            getRequest().updateNamespaces(namespaces);
        }

        if (!getResponses().isEmpty()) {
            for (ResponseInfo response : getResponses()) {
                response.updateNamespaces(namespaces);
            }
        }
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    @SuppressWarnings("deprecation")
    public void writeElement(XmlWriter writer) throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        if ((getIdentifier() != null) && !getIdentifier().equals("")) {
            attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
        }

        if ((getName() != null) && (getName().toString() != null)) {
            attributes.addAttribute("", "name", null, "xs:NMTOKEN", getName()
                    .toString());
        }
        if ((getTargetRef() != null) && (getTargetRef().toString() != null)) {
            attributes.addAttribute("", "href", null, "xs:anyURI",
                    getTargetRef().toString());
        }

        if (getDocumentations().isEmpty() && (getRequest() == null)
                && (getResponses().isEmpty())) {
            writer.emptyElement(APP_NAMESPACE, "method", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "method", null, attributes);

            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            if (getRequest() != null) {
                getRequest().writeElement(writer);
            }

            if (!getResponses().isEmpty()) {
                for (ResponseInfo response : getResponses()) {
                    response.writeElement(writer);

                    // TODO to be removed with the FaultInfo class
                    // Each response's fault generates a new Response
                    if (!response.getFaults().isEmpty()) {
                        for (FaultInfo faultInfo : response.getFaults()) {
                            ResponseInfo r = new ResponseInfo();

                            // Get the statuses from the faults
                            for (Status status : faultInfo.getStatuses()) {
                                if (!r.getStatuses().contains(status)) {
                                    r.getStatuses().add(status);
                                }
                            }

                            r.getRepresentations().add(faultInfo);
                            r.writeElement(writer);
                        }
                    }
                }
            }

            writer.endElement(APP_NAMESPACE, "method");
        }
    }

}
