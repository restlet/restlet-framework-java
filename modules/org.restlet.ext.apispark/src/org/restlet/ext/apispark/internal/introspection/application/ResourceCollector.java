package org.restlet.ext.apispark.internal.introspection.application;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.engine.resource.StatusAnnotationInfo;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.introspection.DocumentedResource;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.model.Types;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.representation.StatusInfo;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Template;
import org.restlet.service.MetadataService;

/**
 * @author Manuel Boillod
 */
public class ResourceCollector {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(ResourceCollector.class
            .getName());

    private static final String SUFFIX_RESOURCE = "Resource";

    private static final String SUFFIX_SERVER_RESOURCE = "ServerResource";

    public static void collectResourceForDirectory(CollectInfo collectInfo,
            Directory directory, String basePath, ChallengeScheme scheme,
            List<? extends IntrospectionHelper> introspectionHelper) {
        Resource resource = getResource(collectInfo, directory, basePath,
                scheme);

        // add operations
        ArrayList<Operation> operations = new ArrayList<>();
        operations.add(getOperationFromMethod(Method.GET));
        if (directory.isModifiable()) {
            operations.add(getOperationFromMethod(Method.DELETE));
            operations.add(getOperationFromMethod(Method.PUT));
        }
        resource.setOperations(operations);

        for (IntrospectionHelper helper : introspectionHelper) {
            helper.processResource(resource, directory.getClass());
        }
        collectInfo.addResource(resource);
    }

    public static void collectResourceForServletResource(
            CollectInfo collectInfo, ServerResource sr, String basePath,
            ChallengeScheme scheme,
            List<? extends IntrospectionHelper> introspectionHelper) {
        Resource resource = getResource(collectInfo, sr, basePath, scheme);

        // add operations
        ArrayList<Operation> operations = new ArrayList<>();

        List<AnnotationInfo> annotations = sr.isAnnotated() ? AnnotationUtils
                .getInstance().getAnnotations(sr.getClass()) : null;

        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                if (annotationInfo instanceof MethodAnnotationInfo) {
                    MethodAnnotationInfo methodAnnotationInfo = (MethodAnnotationInfo) annotationInfo;

                    Method method = methodAnnotationInfo.getRestletMethod();

                    if ("OPTIONS".equals(method.getName())
                            || "PATCH".equals(method.getName())) {
                        LOGGER.fine("Method " + method.getName() + " ignored.");
                        continue;
                    }

                    Operation operation = getOperationFromMethod(method);

                    if (StringUtils.isNullOrEmpty(operation.getName())) {
                        LOGGER.warning("Java method "
                                + methodAnnotationInfo.getJavaMethod()
                                        .getName() + " has no Method name.");
                        operation.setName(methodAnnotationInfo.getJavaMethod()
                                .getName());
                    }

                    completeOperation(collectInfo, operation,
                            methodAnnotationInfo, sr, introspectionHelper);

                    for (IntrospectionHelper helper : introspectionHelper) {
                        helper.processOperation(resource, operation,
                                sr.getClass(),
                                methodAnnotationInfo.getJavaMethod());
                    }
                    operations.add(operation);
                }
            }
            if (!operations.isEmpty()) {
                sortOperationsByMethod(operations);
                resource.setOperations(operations);
                collectInfo.addResource(resource);
            } else {
                LOGGER.warning("Resource " + resource.getName()
                        + " has no methods.");
            }
        } else {
            LOGGER.warning("Resource " + resource.getName()
                    + " has no methods.");
        }

        for (IntrospectionHelper helper : introspectionHelper) {
            helper.processResource(resource, sr.getClass());
        }
    }

    /**
     * Automatically describes a method by discovering the resource's
     * annotations.
     * 
     */
    private static void completeOperation(CollectInfo collectInfo,
            Operation operation, MethodAnnotationInfo mai, ServerResource sr,
            List<? extends IntrospectionHelper> introspectionHelper) {
        // Loop over the annotated Java methods
        MetadataService metadataService = sr.getMetadataService();

        // Retrieve thrown classes
        Class<?>[] thrownClasses = mai.getJavaMethod().getExceptionTypes();
        if (thrownClasses != null) {
            for (Class<?> thrownClass : thrownClasses) {
                StatusAnnotationInfo statusAnnotation = AnnotationUtils
                        .getInstance().getStatusAnnotationInfo(thrownClass);
                if (statusAnnotation != null) {
                    int statusCode = statusAnnotation.getStatus().getCode();
                    Response response = new Response();
                    response.setCode(statusCode);
                    response.setMessage("Status " + statusCode);

                    Class<?> outputPayloadType = statusAnnotation
                            .isSerializable() ? thrownClass : StatusInfo.class;

                    RepresentationCollector.addRepresentation(collectInfo,
                            outputPayloadType, null, introspectionHelper);

                    PayLoad outputPayLoad = new PayLoad();
                    outputPayLoad.setType(outputPayloadType.getName());
                    response.setOutputPayLoad(outputPayLoad);
                    operation.getResponses().add(response);

                }
            }
        }

        // Describe the input
        Class<?>[] inputClasses = mai.getJavaMethod().getParameterTypes();
        if (inputClasses != null && inputClasses.length > 0) {

            // Input representation
            // Handles only the first method parameter
            Class<?> inputClass = inputClasses[0];
            Type inputType = mai.getJavaMethod().getGenericParameterTypes()[0];

            RepresentationCollector.addRepresentation(collectInfo, inputClass,
                    inputType, introspectionHelper);

            PayLoad inputEntity = new PayLoad();
            inputEntity.setType(Types.convertPrimitiveType(ReflectUtils
                    .getSimpleClass(inputType)));
            inputEntity.setArray(ReflectUtils.isListType(inputClass));
            operation.setInputPayLoad(inputEntity);

            // Consumes
            if (metadataService != null) {

                try {
                    List<Variant> requestVariants = mai.getRequestVariants(
                            metadataService, sr.getConverterService());

                    if (requestVariants == null || requestVariants.isEmpty()) {
                        LOGGER.warning("Method has no requested variant: "
                                + mai);
                        return;
                    }

                    // une representation per variant ?
                    for (Variant variant : requestVariants) {

                        if (variant.getMediaType() == null) {
                            LOGGER.warning("Variant has no media type: "
                                    + variant);
                            continue;
                        }

                        operation.getConsumes().add(
                                variant.getMediaType().getName());
                    }
                } catch (IOException e) {
                    throw new ResourceException(e);
                }
            }
        }

        // Describe query parameters, if any.
        if (mai.getQuery() != null) {
            Form form = new Form(mai.getQuery());
            for (org.restlet.data.Parameter parameter : form) {
                QueryParameter queryParameter = new QueryParameter();
                queryParameter.setName(parameter.getName());
                queryParameter.setRequired(true);
                queryParameter.setDescription(StringUtils
                        .isNullOrEmpty(parameter.getValue()) ? "" : "Value: "
                        + parameter.getValue());
                queryParameter.setDefaultValue(parameter.getValue());
                queryParameter.setAllowMultiple(false);
                operation.getQueryParameters().add(queryParameter);
            }
        }

        // Describe the success response

        Response response = new Response();

        Class<?> outputClass = mai.getJavaMethod().getReturnType();
        Type outputType = mai.getJavaMethod().getGenericReturnType();

        if (outputClass != Void.TYPE) {
            // Output representation
            RepresentationCollector.addRepresentation(collectInfo, outputClass,
                    outputType, introspectionHelper);

            PayLoad outputEntity = new PayLoad();
            outputEntity.setType(Types.convertPrimitiveType(ReflectUtils
                    .getSimpleClass(outputType)));
            outputEntity.setArray(ReflectUtils.isListType(outputClass));

            response.setOutputPayLoad(outputEntity);
        }

        response.setCode(Status.SUCCESS_OK.getCode());
        response.setName("Success");
        response.setDescription("");
        response.setMessage(Status.SUCCESS_OK.getDescription());
        operation.getResponses().add(response);

        // Produces
        if (metadataService != null) {
            try {
                List<Variant> responseVariants = mai.getResponseVariants(
                        metadataService, sr.getConverterService());

                if (responseVariants == null || responseVariants.isEmpty()) {
                    LOGGER.warning("Method has no response variant: " + mai);
                    return;
                }

                // une representation per variant ?
                for (Variant variant : responseVariants) {

                    if (variant.getMediaType() == null) {
                        LOGGER.warning("Variant has no media type: " + variant);
                        continue;
                    }

                    operation.getProduces().add(
                            variant.getMediaType().getName());
                }
            } catch (IOException e) {
                throw new ResourceException(e);
            }
        }
    }

    private static Operation getOperationFromMethod(Method method) {
        Operation operation = new Operation();
        operation.setMethod(method.getName());
        return operation;
    }

    private static Resource getResource(CollectInfo collectInfo,
            Object restlet, String basePath, ChallengeScheme scheme) {
        Resource resource = new Resource();
        resource.setResourcePath(basePath);

        if (restlet instanceof Directory) {
            Directory directory = (Directory) restlet;
            resource.setName(directory.getName());
            resource.setDescription(directory.getDescription());
        }
        if (restlet instanceof ServerResource) {
            ServerResource serverResource = (ServerResource) restlet;
            resource.setName(serverResource.getName());
            resource.setDescription(serverResource.getDescription());
        }
        if (restlet instanceof DocumentedResource) {
            DocumentedResource documentedServerResource = (DocumentedResource) restlet;
            resource.setSections(documentedServerResource.getSections());
        } else {
            String sectionName = restlet.getClass().getPackage().getName();
            resource.getSections().add(sectionName);
        }

        if (StringUtils.isNullOrEmpty(resource.getName())) {
            String name = restlet.getClass().getSimpleName();
            if (name.endsWith(SUFFIX_SERVER_RESOURCE)
                    && name.length() > SUFFIX_SERVER_RESOURCE.length()) {
                name = name.substring(0,
                        name.length() - SUFFIX_SERVER_RESOURCE.length());
            }
            if (name.endsWith(SUFFIX_RESOURCE)
                    && name.length() > SUFFIX_RESOURCE.length()) {
                name = name.substring(0,
                        name.length() - SUFFIX_RESOURCE.length());
            }
            resource.setName(name);
        }

        // add sections in collect info
        for (String section : resource.getSections()) {
            if (collectInfo.getSection(section) == null) {
                collectInfo.addSection(new Section(section));
            }
        }

        Template template = new Template(basePath);
        for (String variable : template.getVariableNames()) {
            PathVariable pathVariable = new PathVariable();
            pathVariable.setName(variable);
            resource.getPathVariables().add(pathVariable);
        }

        if (scheme != null) {
            resource.setAuthenticationProtocol(scheme.getName());
        }

        return resource;
    }

    private static void sortOperationsByMethod(ArrayList<Operation> operations) {
        Collections.sort(operations, new Comparator<Operation>() {
            public int compare(Operation o1, Operation o2) {
                int c = o1.getMethod().compareTo(o2.getMethod());
                if (c == 0) {
                    c = o1.getName().compareTo(o2.getName());
                }
                return c;
            }
        });
    }
}
