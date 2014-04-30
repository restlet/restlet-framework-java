/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.ext.apispark.internal.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.service.MetadataService;

/**
 * Describes the expected requests and responses of a resource method.
 * 
 * @author Jerome Louvel
 */
public class MethodInfo extends DocumentedInfo {

    /**
     * Automatically describes a method by discovering the resource's
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
            for (AnnotationInfo annotationInfo : annotations) {
                try {
                    if (info.getMethod()
                            .equals(annotationInfo.getRestletMethod())) {
                        // Describe the request
                        Class<?>[] classes = annotationInfo.getJavaInputTypes();
                        Class<?> inputClass = (classes != null && classes.length > 0) ? classes[0]
                                : null;

                        List<Variant> requestVariants = annotationInfo
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
                                    if (info.getRequest() == null) {
                                        info.setRequest(new RequestInfo());
                                    }

                                    RepresentationInfo representationInfo = RepresentationInfo
                                            .describe(info, inputClass, variant);

                                    info.getRequest().getRepresentations()
                                            .add(representationInfo);
                                }
                            }
                        }

                        // Describe the response
                        Class<?> outputClass = annotationInfo
                                .getJavaOutputType();

                        if (outputClass != null) {
                            List<Variant> responseVariants = annotationInfo
                                    .getResponseVariants(
                                            resource.getMetadataService(),
                                            resource.getConverterService());

                            if (responseVariants != null) {
                                for (Variant variant : responseVariants) {
                                    if ((variant.getMediaType() != null)
                                            && !info.getResponse()
                                                    .getRepresentations()
                                                    .contains(variant)) {
                                        RepresentationInfo representationInfo = RepresentationInfo
                                                .describe(info, outputClass,
                                                        variant);

                                        info.getResponse().getRepresentations()
                                                .add(representationInfo);
                                    }
                                }
                            }
                        }

                        if (info.getResponse().getStatuses().isEmpty()) {
                            info.getResponse().getStatuses()
                                    .add(Status.SUCCESS_OK);
                            info.getResponse().setDocumentation("Success");
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
    private Method method;

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
     * Returns the method.
     * 
     * @return The method.
     */

    public Method getMethod() {
        return this.method;
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
    public void setMethod(Method name) {
        this.method = name;
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

}
