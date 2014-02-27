/**
 * Copyright 2005-2013 Restlet S.A.S.
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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.omg.PortableInterceptor.RequestInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.service.MetadataService;
import org.restlet.util.NamedValue;
import org.restlet.util.Series;

import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationParameter;
import com.wordnik.swagger.core.DocumentationResponse;
import com.wordnik.swagger.core.DocumentationSchema;
import com.wordnik.swagger.jsonschema.ApiModelParser;

/**
 * Resource that is able to automatically describe itself with Swagger. This
 * description can be customized by overriding the {@link #describe()} and
 * {@link #describeMethod(Method, DocumentationOperation)} methods.<br>
 * <br>
 * When used to describe a class of resources in the context of a parent
 * application, a special instance will be created using the default constructor
 * (with no request, response associated). In this case, the resource should do
 * its best to return the generic information when the WADL description methods
 * are invoked, like {@link #describe()} and delegate methods.
 * 
 * @author Jerome Louvel
 */
public class SwaggerServerResource extends ServerResource {

    /**
     * Automatically describe a method by discovering the resource's
     * annotations.
     * 
     * @param info
     *            The method description to update.
     * @param resource
     *            The server resource to describe.
     */
    public static void describeAnnotations(Documentation documentation,
            DocumentationOperation info, ServerResource resource) {
        // Loop over the annotated Java methods
        MetadataService metadataService = resource.getMetadataService();
        List<AnnotationInfo> annotations = resource.isAnnotated() ? AnnotationUtils
                .getInstance().getAnnotations(resource.getClass()) : null;

        if (annotations != null && metadataService != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                try {
                    // TODO I wonder if we should rely on http method instead.
                    if (info.getNickname().equals(
                            annotationInfo.getRestletMethod().getName())) {
                        // Describe the request
                        Class<?>[] classes = annotationInfo.getJavaInputTypes();

                        List<Variant> requestVariants = annotationInfo
                                .getRequestVariants(
                                        resource.getMetadataService(),
                                        resource.getConverterService());

                        if (requestVariants != null) {
                            for (Variant variant : requestVariants) {
                                // TODO Use response'class and documentation
                                // schema.
                                // TODO work only if classs[0] is not null
                                if (variant.getMediaType() != null
                                        && (documentation.getModels() == null || !documentation
                                                .getModels()
                                                .containsKey(
                                                        classes[0]
                                                                .getCanonicalName()))) {
                                    DocumentationSchema schema = null;

                                    // if (DocumentationOperation == null) {
                                    // info.setRequest(new RequestInfo());
                                    // }

                                    if (resource instanceof SwaggerServerResource) {
                                        schema = ((SwaggerServerResource) resource)
                                                .describe(documentation, info,
                                                        classes[0], variant);
                                    } else {
                                        // TODO describe the schema from the
                                        // variant.
                                        schema = new DocumentationSchema();
                                        schema.setId(classes[0]
                                                .getCanonicalName());
                                    }
                                    documentation.addModel(schema.getId(),
                                            schema);
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
                                    // TODO Use response'class and documentation
                                    // schema.
                                    // if ((variant.getMediaType() != null)
                                    // && !info.getResponse()
                                    // .getRepresentations()
                                    // .contains(variant)) {
                                    // RepresentationInfo representationInfo =
                                    // null;
                                    //
                                    // if (resource instanceof
                                    // WadlServerResource) {
                                    // representationInfo =
                                    // ((WadlServerResource) resource)
                                    // .describe(info,
                                    // info.getResponse(),
                                    // outputClass,
                                    // variant);
                                    // } else {
                                    // representationInfo = new
                                    // RepresentationInfo(
                                    // variant);
                                    // }
                                    //
                                    // info.getResponse().getRepresentations()
                                    // .add(representationInfo);
                                    // }
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

    /**
     * Returns a Swagger description of the current resource.
     * 
     * @param documentation
     *            The parent application.
     * @param info
     *            Swagger description of the current resource to update.
     * @param resource
     *            The resource to describe.
     * @param path
     *            Path of the current resource.
     */
    public static void describe(Documentation documentation,
            DocumentationEndPoint info, Object resource, String path) {
        if ((path != null) && path.startsWith("/")) {
            path = path.substring(1);
        }

        info.setPath(path);

        // Introspect the current resource to detect the allowed methods
        List<Method> methodsList = new ArrayList<Method>();

        if (resource instanceof ServerResource) {
            ((ServerResource) resource).updateAllowedMethods();
            methodsList.addAll(((ServerResource) resource).getAllowedMethods());

            if (resource instanceof SwaggerServerResource) {
                // TODO done by operation...
                // info.setParameters(((SwaggerServerResource)
                // resource).describeParameters());

                if (documentation != null) {
                    ((SwaggerServerResource) resource).describe(documentation);
                }
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
        DocumentationOperation methodInfo;

        for (Method method : methodsList) {
            methodInfo = new DocumentationOperation();
            info.addOperation(methodInfo);
            methodInfo.setNickname(method.getName());

            if (resource instanceof ServerResource) {
                if (resource instanceof SwaggerServerResource) {
                    SwaggerServerResource wsResource = (SwaggerServerResource) resource;

                    if (wsResource.canDescribe(method)) {
                        wsResource.describeMethod(documentation, method,
                                methodInfo);
                    }
                } else {
                    describeAnnotations(documentation, methodInfo,
                            (ServerResource) resource);
                }
            }
        }

        // Document the resource
        String title = null;
        String textContent = null;

        if (resource instanceof SwaggerServerResource) {
            title = ((SwaggerServerResource) resource).getName();
            textContent = ((SwaggerServerResource) resource).getDescription();
        }

        if ((title != null) && !title.isEmpty()) {
            info.setDescription(title);
        } else {
            info.setDescription(textContent);
        }
    }

    /**
     * Indicates if the resource should be automatically described via Swagger
     * when an OPTIONS request is handled.
     */
    private volatile boolean autoDescribing;

    /**
     * The description of this documented resource.
     */
    private volatile String description;

    /**
     * The name of this documented resource.
     */
    private volatile String name;

    /**
     * Constructor.
     */
    public SwaggerServerResource() {
        this.autoDescribing = true;
    }

    /**
     * Indicates if the given method exposes its Swagger description. By
     * default, HEAD and OPTIONS are not exposed. This method is called by
     * {@link #describe(String, ResourceInfo)}.
     * 
     * @param method
     *            The method
     * @return True if the method exposes its description, false otherwise.
     */
    public boolean canDescribe(Method method) {
        return !(Method.HEAD.equals(method) || Method.OPTIONS.equals(method));
    }

    /**
     * Creates a new HTML representation for a given {@link Documentation}
     * instance describing an application.
     * 
     * @param documentation
     *            The application description.
     * @return The created {@link SwaggerRepresentation}.
     */
    protected Representation createHtmlRepresentation(
            Documentation documentation) {
        return new SwaggerRepresentation(documentation).getHtmlRepresentation();
    }

    /**
     * Creates a new Swagger representation for a given {@link Documentation}
     * instance describing an application.
     * 
     * @param documentation
     *            The application description.
     * @return The created {@link SwaggerRepresentation}.
     */
    protected Representation createSwaggerRepresentation(
            Documentation documentation) {
        return new SwaggerRepresentation(documentation);
    }

    /**
     * Describes the resource as a standalone Swagger document.
     * 
     * @return The Swagger description.
     */
    protected Representation describe() {
        Documentation documentation = new Documentation();
        // TODO set path, etc.
        return describe(documentation, getPreferredSwaggerVariant());
    }

    /**
     * Updates the description of the parent application. This is typically used
     * to add documentation on global representations used by several methods or
     * resources. Does nothing by default.
     * 
     * @param documentation
     *            The parent application.
     */
    protected void describe(Documentation documentation) {
    }

    /**
     * Describes a representation class and variant couple as Swagger
     * information. The variant contains the target media type that can be
     * converted to by one of the available Restlet converters.
     * 
     * @param documentation
     *            The root documentation.
     * @param methodInfo
     *            The parent method description.
     * @param representationClass
     *            The representation bean class.
     * @param variant
     *            The target variant.
     * @return The WADL representation information.
     */
    protected DocumentationSchema describe(Documentation documentation,
            DocumentationOperation methodInfo, Class<?> representationClass,
            Variant variant) {
        DocumentationSchema result = new ApiModelParser(representationClass).parse().toDocumentationSchema();
        
        
        // TODO describe the representation
        return result;
    }

    /**
     * Describes a representation class and variant couple as WADL information
     * for the given method and request. The variant contains the target media
     * type that can be converted to by one of the available Restlet converters.<br>
     * <br>
     * By default, it calls
     * {@link #describe(DocumentationOperation, Class, Variant)}.
     * 
     * @param documentation
     *            The root documentation.
     * @param methodInfo
     *            The parent method description.
     * @param requestInfo
     *            The parent request description.
     * @param representationClass
     *            The representation bean class.
     * @param variant
     *            The target variant.
     * @return The WADL representation information.
     */
    protected DocumentationSchema describe(Documentation documentation,
            DocumentationOperation methodInfo, RequestInfo requestInfo,
            Class<?> representationClass, Variant variant) {
        return describe(documentation, methodInfo, representationClass, variant);
    }

    /**
     * Describes a representation class and variant couple as WADL information
     * for the given method and response. The variant contains the target media
     * type that can be converted to by one of the available Restlet converters.<br>
     * <br>
     * By default, it calls
     * {@link #describe(DocumentationOperation, Class, Variant)}.
     * 
     * @param documentation
     *            The root documentation.
     * @param methodInfo
     *            The parent method description.
     * @param responseInfo
     *            The parent response description.
     * @param representationClass
     *            The representation bean class.
     * @param variant
     *            The target variant.
     * @return The WADL representation information.
     */
    protected DocumentationSchema describe(Documentation documentation,
            DocumentationOperation methodInfo,
            DocumentationResponse responseInfo, Class<?> representationClass,
            Variant variant) {
        return describe(documentation, methodInfo, representationClass, variant);
    }

    /**
     * Returns a WADL description of the current resource, leveraging the
     * {@link #getResourcePath()} method.
     * 
     * @param documentation
     *            The root documentation.
     * @param info
     *            WADL description of the current resource to update.
     */
    public void describe(Documentation documentation, DocumentationEndPoint info) {
        describe(documentation, getResourcePath(), info);
    }

    /**
     * Returns a WADL description of the current resource.
     * 
     * @param path
     *            Path of the current resource.
     * @param info
     *            WADL description of the current resource to update.
     */
    public void describe(Documentation documentation, String path,
            DocumentationEndPoint info) {
        describe(documentation, info, this, path);
    }

    /**
     * Describes the resource as a WADL document for the given variant.
     * 
     * @param variant
     *            The WADL variant.
     * @return The WADL description.
     */
    protected Representation describe(Documentation documentation,
            Variant variant) {
        Representation result = null;

        if (variant != null) {
            DocumentationEndPoint resource = new DocumentationEndPoint();
            describe(documentation, resource);
            // TODO initiate application.
            // Documentation application = resource.createApplication();
            Documentation application = new Documentation();
            describe(application);

            if (MediaType.APPLICATION_JSON.equals(variant.getMediaType())) {
                result = SwaggerApplication
                        .createJsonRepresentation(application);
            } else if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
                result = SwaggerApplication
                        .createHtmlRepresentation(application);
            } else if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
                result = SwaggerApplication
                        .createHtmlRepresentation(application);
            }
        }

        return result;
    }

    /**
     * Describes the DELETE method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeDelete(Documentation documentation,
            DocumentationOperation info) {
        describeAnnotations(documentation, info, this);
    }

    /**
     * Describes the GET method.<br>
     * By default, it describes the response with the available variants based
     * on the {@link #getVariants()} method. Thus in the majority of cases, the
     * method of the super class must be called when overridden.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeGet(Documentation documentation,
            DocumentationOperation info) {
        describeAnnotations(documentation, info, this);
    }

    /**
     * Returns a WADL description of the current method.
     * 
     * @return A WADL description of the current method.
     */
    protected DocumentationOperation describeMethod(Documentation documentation) {
        DocumentationOperation result = new DocumentationOperation();
        describeMethod(documentation, getMethod(), result);
        return result;
    }

    /**
     * Returns a WADL description of the given method.
     * 
     * @param method
     *            The method to describe.
     * @param info
     *            The method description to update.
     */
    protected void describeMethod(Documentation documentation, Method method,
            DocumentationOperation info) {
        info.setNickname(method.getName());

        if (Method.GET.equals(method)) {
            describeGet(documentation, info);
        } else if (Method.POST.equals(method)) {
            describePost(documentation, info);
        } else if (Method.PUT.equals(method)) {
            describePut(documentation, info);
        } else if (Method.DELETE.equals(method)) {
            describeDelete(documentation, info);
        } else if (Method.OPTIONS.equals(method)) {
            describeOptions(documentation, info);
        } else if (Method.PATCH.equals(method)) {
            describePatch(documentation, info);
        }
    }

    /**
     * Describes the OPTIONS method.<br>
     * By default it describes the response with the available variants based on
     * the {@link #getWadlVariants()} method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeOptions(Documentation documentation,
            DocumentationOperation info) {
        // Describe each variant
        for (Variant variant : getSwaggerVariants()) {
            // TODO handle via the DocumentationSchema.
            // RepresentationInfo result = new RepresentationInfo(variant);
            // info.getResponse().getRepresentations().add(result);
        }
    }

    /**
     * Returns the description of the parameters of this resource. Returns null
     * by default.
     * 
     * @return The description of the parameters.
     */
    protected List<DocumentationParameter> describeParameters() {
        return null;
    }

    /**
     * Describes the PATCH method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePatch(Documentation documentation,
            DocumentationOperation info) {
        describeAnnotations(documentation, info, this);
    }

    /**
     * Describes the POST method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePost(Documentation documentation,
            DocumentationOperation info) {
        describeAnnotations(documentation, info, this);
    }

    /**
     * Describes the PUT method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePut(Documentation documentation,
            DocumentationOperation info) {
        describeAnnotations(documentation, info, this);
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        this.autoDescribing = true;
    }

    /**
     * Returns the description of this documented resource. Is seen as the text
     * content of the "doc" tag of the "resource" element in a WADL document.
     * 
     * @return The description of this documented resource.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the set of headers as a collection of {@link Parameter} objects.
     * 
     * @return The set of headers as a collection of {@link Parameter} objects.
     */
    @SuppressWarnings("unchecked")
    private Series<Header> getHeaders() {
        return (Series<Header>) getRequestAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
    }

    /**
     * Returns the name of this documented resource. Is seen as the title of the
     * "doc" tag of the "resource" element in a WADL document.
     * 
     * @return The name of this documented resource.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the first parameter found in the current context (entity, query,
     * headers, etc) with the given name.
     * 
     * @param name
     *            The parameter name.
     * @return The first parameter found with the given name.
     */
    protected NamedValue<String> getParameter(String name) {
        NamedValue<String> result = null;
        Series<? extends NamedValue<String>> set = getParameters(name);

        if (set != null) {
            result = set.getFirst(name);
        }

        return result;
    }

    /**
     * Returns a collection of parameters objects contained in the current
     * context (entity, query, headers, etc) given a DocumentationParameter
     * instance.
     * 
     * @param parameterInfo
     *            The DocumentationParameter instance.
     * @return A collection of parameters objects
     */
    private Series<? extends NamedValue<String>> getParameters(
            DocumentationParameter parameterInfo) {
        Series<? extends NamedValue<String>> result = null;

        if ("header".equals(parameterInfo.dataType())) {
            result = getHeaders().subList(parameterInfo.getName());
        } else if ("path".equals(parameterInfo.dataType())) {
            Object parameter = getRequest().getAttributes().get(
                    parameterInfo.getName());

            if (parameter != null) {
                result = new Series<Parameter>(Parameter.class);
                result.add(parameterInfo.getName(),
                        Reference.decode((String) parameter));
            }
        } else if ("query".equals(parameterInfo.getDataType())) {
            result = getQuery().subList(parameterInfo.getName());
        } else if ("body".equals(parameterInfo.getDataType())) {
            // TODO not yet implemented.
        } else if ("form".equals(parameterInfo.getDataType())) {
            // TODO not yet implemented.
        }

        if (result == null && parameterInfo.getDefaultValue() != null) {
            result = new Series<Parameter>(Parameter.class);
            result.add(parameterInfo.getName(), parameterInfo.getDefaultValue());
        }

        return result;
    }

    /**
     * Returns a collection of parameters found in the current context (entity,
     * query, headers, etc) given a parameter name. It returns null if the
     * parameter name is unknown.
     * 
     * @param name
     *            The name of the parameter.
     * @return A collection of parameters.
     */
    protected Series<? extends NamedValue<String>> getParameters(String name) {
        Series<? extends NamedValue<String>> result = null;

        if (describeParameters() != null) {
            for (DocumentationParameter parameter : describeParameters()) {
                if (name.equals(parameter.getName())) {
                    result = getParameters(parameter);
                }
            }
        }

        return result;
    }

    /**
     * Returns the preferred WADL variant according to the client preferences
     * specified in the request.
     * 
     * @return The preferred WADL variant.
     */
    protected Variant getPreferredSwaggerVariant() {
        return getConnegService().getPreferredVariant(getSwaggerVariants(),
                getRequest(), getMetadataService());
    }

    /**
     * Returns the resource's relative path.
     * 
     * @return The resource's relative path.
     */
    protected String getResourcePath() {
        Reference ref = new Reference(getRequest().getRootRef(), getRequest()
                .getResourceRef());
        return ref.getRemainingPart();
    }

    /**
     * Returns the application resources base URI.
     * 
     * @return The application resources base URI.
     */
    protected Reference getResourcesBase() {
        return getRequest().getRootRef();
    }

    /**
     * Returns the available Swagger variants.
     * 
     * @return The available Swagger variants.
     */
    protected List<Variant> getSwaggerVariants() {
        List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.APPLICATION_JSON));
        result.add(new Variant(MediaType.APPLICATION_XML));
        result.add(new Variant(MediaType.TEXT_XML));
        return result;
    }

    /**
     * Indicates if the resource should be automatically described via WADL when
     * an OPTIONS request is handled.
     * 
     * @return True if the resource should be automatically described via WADL.
     */
    public boolean isAutoDescribing() {
        return this.autoDescribing;
    }

    @Override
    public Representation options() {
        if (isAutoDescribing()) {
            return describe();
        }

        return null;
    }

    /**
     * Indicates if the resource should be automatically described via WADL when
     * an OPTIONS request is handled.
     * 
     * @param autoDescribed
     *            True if the resource should be automatically described via
     *            WADL.
     */
    public void setAutoDescribing(boolean autoDescribed) {
        this.autoDescribing = autoDescribed;
    }

    /**
     * Sets the description of this documented resource. Is seen as the text
     * content of the "doc" tag of the "resource" element in a WADL document.
     * 
     * @param description
     *            The description of this documented resource.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name of this documented resource. Is seen as the title of the
     * "doc" tag of the "resource" element in a WADL document.
     * 
     * @param name
     *            The name of this documented resource.
     */
    public void setName(String name) {
        this.name = name;
    }

}
