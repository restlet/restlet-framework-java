/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
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
                .getInstance().getAnnotations(resource.getClass()) : null;

        if (annotations != null && metadataService != null) {
            for (AnnotationInfo ai : annotations) {
                try {
                    if (ai instanceof MethodAnnotationInfo) {
                        MethodAnnotationInfo mai = (MethodAnnotationInfo) ai;

                        if (info.getName().equals(mai.getRestletMethod())) {
                            // Describe the request
                            Class<?>[] classes = mai.getJavaInputTypes();

                            List<Variant> requestVariants = mai
                                    .getRequestVariants(
                                            resource.getMetadataService(),
                                            resource.getConverterService());

                            if (requestVariants != null) {
                                for (Variant variant : requestVariants) {
                                    if ((variant.getMediaType() != null)
                                            && ((info.getRequest() == null) || !info
                                                    .getRequest()
                                                    .getRepresentations()
                                                    .contains(variant))) {
                                        RepresentationInfo representationInfo = null;

                                        if (info.getRequest() == null) {
                                            info.setRequest(new RequestInfo());
                                        }

                                        if (resource instanceof WadlServerResource) {
                                            representationInfo = ((WadlServerResource) resource)
                                                    .describe(info,
                                                            info.getRequest(),
                                                            classes[0], variant);
                                        } else {
                                            representationInfo = new RepresentationInfo(
                                                    variant);
                                        }

                                        info.getRequest().getRepresentations()
                                                .add(representationInfo);
                                    }
                                }
                            }

                            // Describe the response
                            Class<?> outputClass = mai.getJavaOutputType();

                            if (outputClass != null) {
                                List<Variant> responseVariants = mai
                                        .getResponseVariants(
                                                resource.getMetadataService(),
                                                resource.getConverterService());

                                if (responseVariants != null) {
                                    for (Variant variant : responseVariants) {
                                        if ((variant.getMediaType() != null)
                                                && !info.getResponse()
                                                        .getRepresentations()
                                                        .contains(variant)) {
                                            RepresentationInfo representationInfo = null;

                                            if (resource instanceof WadlServerResource) {
                                                representationInfo = ((WadlServerResource) resource)
                                                        .describe(info, info
                                                                .getResponse(),
                                                                outputClass,
                                                                variant);
                                            } else {
                                                representationInfo = new RepresentationInfo(
                                                        variant);
                                            }

                                            info.getResponse()
                                                    .getRepresentations()
                                                    .add(representationInfo);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new ResourceException(e);
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
                }
            }

            writer.endElement(APP_NAMESPACE, "method");
        }
    }

}
