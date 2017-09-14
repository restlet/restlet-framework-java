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

package org.restlet.ext.apispark.internal.introspection.jaxrs;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.util.BeanInfoUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.introspection.DocumentedApplication;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.introspection.util.TypeInfo;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.introspection.util.UnsupportedTypeException;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Publish the documentation of a Jaxrs-based Application to the APISpark
 * console.
 * 
 * @author Thierry Boileau
 */
public class JaxRsIntrospector extends IntrospectionUtils {

    private static class ClazzInfo {

        private Class<?> clazz;

        private Consumes consumes;

        // List of common annotations, defined at the level of the class, or
        // at the level of the fields.
        private Map<String, Header> headers = new LinkedHashMap<>();

        private Path path;

        private Map<String, PathVariable> pathVariables = new LinkedHashMap<>();

        private Produces produces;

        private Map<String, QueryParameter> queryParameters = new LinkedHashMap<>();

        private Resource resource;

        public void addHeader(Header header) {
            headers.put(header.getName(), header);
        }

        public void addPathVariable(PathVariable pathVariable) {
            pathVariables.put(pathVariable.getName(), pathVariable);
        }

        public void addQueryParameter(QueryParameter queryParameter) {
            queryParameters.put(queryParameter.getName(), queryParameter);
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public Consumes getConsumes() {
            return consumes;
        }

        public Map<String, Header> getHeadersCopy() {
            return new LinkedHashMap<>(headers);
        }

        public Path getPath() {
            return path;
        }

        public Map<String, PathVariable> getPathVariablesCopy() {
            return new LinkedHashMap<>(pathVariables);
        }

        public Produces getProduces() {
            return produces;
        }

        public Map<String, QueryParameter> getQueryParametersCopy() {
            return new LinkedHashMap<>(queryParameters);
        }

        @SuppressWarnings("unused")
        public Resource getResource() {
            return resource;
        }

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        public void setConsumes(Consumes consumes) {
            this.consumes = consumes;
        }

        public void setPath(Path path) {
            this.path = path;
        }

        public void setProduces(Produces produces) {
            this.produces = produces;
        }

        @SuppressWarnings("unused")
        public void setResource(Resource resource) {
            this.resource = resource;
        }
    }

    public static class CollectInfo {

        private String applicationName;

        private String applicationPath;

        private Map<String, Representation> representations = new HashMap<>();

        private Map<String, Resource> resourcesByPath = new LinkedHashMap<>();

        private Map<String, Section> sections = new HashMap<>();

        private boolean useSectionNamingPackageStrategy;

        public void addRepresentation(Representation representation) {
            representations.put(representation.getName(), representation);
        }

        public void addResource(Resource resource) {
            resourcesByPath.put(resource.getResourcePath(), resource);
        }

        public void addSection(Section section) {
            sections.put(section.getName(), section);
        }

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public String getApplicationPath() {
            return applicationPath;
        }

        public Representation getRepresentation(String identifier) {
            return representations.get(identifier);
        }

        public List<Representation> getRepresentations() {
            return new ArrayList<>(representations.values());
        }

        public Resource getResource(String operationPath) {
            return resourcesByPath.get(operationPath);
        }

        public List<Resource> getResources() {
            return new ArrayList<>(resourcesByPath.values());
        }

        public Section getSection(String identifier) {
            return sections.get(identifier);
        }

        public List<Section> getSections() {
            return new ArrayList<>(sections.values());
        }

        public boolean isUseSectionNamingPackageStrategy() {
            return useSectionNamingPackageStrategy;
        }

        public void setApplicationPath(String applicationPath) {
            this.applicationPath = applicationPath;
        }

        public void setSections(Map<String, Section> sections) {
            this.sections = sections;
        }

        public void setUseSectionNamingPackageStrategy(
                boolean useSectionNamingPackageStrategy) {
            this.useSectionNamingPackageStrategy = useSectionNamingPackageStrategy;
        }
    }

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(JaxRsIntrospector.class
            .getName());

    private static final String SUFFIX_RESOURCE = "Resource";

    private static final String SUFFIX_SERVER_RESOURCE = "ServerResource";

    private static void addEndpoints(String applicationPath,
            Definition definition) {
        if (applicationPath != null) {
            Endpoint endpoint = new Endpoint(applicationPath);
            definition.getEndpoints().add(endpoint);
        }
    }

    private static void addRepresentation(CollectInfo collectInfo,
            TypeInfo typeInfo,
            List<? extends IntrospectionHelper> introspectionHelper) {
        // Introspect the java class
        Representation representation = new Representation();
        representation.setDescription("");

        if (typeInfo.isList()) {
            // Collect generic type
            addRepresentation(collectInfo, typeInfo.getComponentTypeInfo(),
                    introspectionHelper);
            return;
        }

        if (typeInfo.isPrimitive() || typeInfo.isFile()) {
            // primitives and files are not collected
            return;
        }

        // Example: "java.util.Contact" or "String"
        representation.setDescription("Java type: "
                + typeInfo.getRepresentationClazz().getName());

        // Sections
        String packageName = typeInfo.getClazz().getPackage().getName();
        representation.getSections().add(packageName);
        if (collectInfo.getSection(packageName) == null) {
            collectInfo.addSection(new Section(packageName));
        }
        // Example: "Contact"
        JsonRootName jsonType = typeInfo.getClazz().getAnnotation(
                JsonRootName.class);
        String typeName = jsonType == null ? typeInfo.getRepresentationClazz()
                .getSimpleName() : jsonType.value();
        representation.setName(typeName);
        representation.setRaw(false);

        // at this point, identifier is known - we check if it exists in cache
        boolean notInCache = collectInfo.getRepresentation(representation
                .getName()) == null;

        if (notInCache) {

            // add representation in cache before complete it to avoid infinite
            // loop
            collectInfo.addRepresentation(representation);

            if (typeInfo.isPojo()) {
                // add properties definition
                BeanInfo beanInfo = BeanInfoUtils.getBeanInfo(typeInfo
                        .getRepresentationClazz());

                JsonIgnoreProperties jsonIgnorePropertiesAnnotation = AnnotatedClass
                        .construct(typeInfo.getRepresentationClazz(),
                                new JacksonAnnotationIntrospector(), null)
                        .getAnnotation(JsonIgnoreProperties.class);
                List<String> jsonIgnoreProperties = jsonIgnorePropertiesAnnotation == null ? null
                        : Arrays.asList(jsonIgnorePropertiesAnnotation.value());

                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {

                    if (jsonIgnoreProperties != null
                            && jsonIgnoreProperties.contains(pd.getName())) {
                        // ignore this field
                        continue;
                    }
                    JsonIgnore jsonIgnore = pd.getReadMethod().getAnnotation(
                            JsonIgnore.class);
                    if (jsonIgnore != null && jsonIgnore.value()) {
                        // ignore this field
                        continue;
                    }

                    TypeInfo propertyTypeInfo;
                    try {
                        propertyTypeInfo = Types.getTypeInfo(pd.getReadMethod()
                                .getReturnType(), pd.getReadMethod()
                                .getGenericReturnType());
                    } catch (UnsupportedTypeException e) {
                        LOGGER.warning("Could not add property " + pd.getName()
                                + " of representation "
                                + typeInfo.getRepresentationClazz().getName()
                                + ". " + e.getMessage());
                        continue;
                    }

                    JsonProperty jsonProperty = pd.getReadMethod()
                            .getAnnotation(JsonProperty.class);
                    String propertyName = jsonProperty != null
                            && !StringUtils.isNullOrEmpty(jsonProperty.value()) ? jsonProperty
                            .value() : pd.getName();

                    JsonPropertyDescription jsonPropertyDescription = pd
                            .getReadMethod().getAnnotation(
                                    JsonPropertyDescription.class);

                    Property property = new Property();
                    property.setName(propertyName);
                    property.setDescription(jsonPropertyDescription != null ? jsonPropertyDescription
                            .value() : "");
                    property.setType(propertyTypeInfo.getRepresentationName());
                    property.setRequired(jsonProperty != null && jsonProperty.required());
                    property.setList(propertyTypeInfo.isList());

                    addRepresentation(collectInfo, propertyTypeInfo,
                            introspectionHelper);

                    for (IntrospectionHelper helper : introspectionHelper) {
                        helper.processProperty(property, pd.getReadMethod());
                    }

                    representation.getProperties().add(property);
                }
            }

            for (IntrospectionHelper helper : introspectionHelper) {
                helper.processRepresentation(representation,
                        typeInfo.getRepresentationClazz());
            }
        }
    }

    /**
     * Returns a clean path (especially variables are cleaned from routing
     * regexp).
     * 
     * @param path
     *            The path to clean.
     * @return The cleand path.
     */
    private static String cleanPath(String path) {
        if (path != null) {
            StringBuilder sb = new StringBuilder();
            char next;
            boolean inVariable = false;
            boolean endVariable = false;
            StringBuilder varBuffer = null;

            for (int i = 0; i < path.length(); i++) {
                next = path.charAt(i);

                if (inVariable) {
                    if (next == '}') {
                        // End of variable detected
                        if (varBuffer.length() == 0) {
                            LOGGER.warning("Empty pattern variables are not allowed : "
                                    + path);
                        } else {
                            sb.append(varBuffer.toString());

                            // Reset the variable name buffer
                            varBuffer = new StringBuilder();
                        }

                        endVariable = false;
                        inVariable = false;
                        sb.append(next);
                    } else if (endVariable) {
                        continue;
                    } else if (Reference.isUnreserved(next)) {
                        // Append to the variable name
                        varBuffer.append(next);
                    } else if (next == ':') {
                        // In this case, the following is the regexp that helps
                        // routing requests
                        // TODO in the future, use the following as roles for
                        // controlling the values of the variables.
                        endVariable = true;
                    }
                } else {
                    sb.append(next);
                    if (next == '{') {
                        inVariable = true;
                        varBuffer = new StringBuilder();
                    } else if (next == '}') {
                        LOGGER.warning("An invalid character was detected inside a pattern variable : "
                                + path);
                    }
                }
            }

            return sb.toString();
        }

        return null;
    }

    /**
     * Returns an instance of what must be a subclass of {@link Application}.
     * Returns null in case of errors.
     * 
     * @param className
     *            The name of the application class.
     * @return An instance of what must be a subclass of {@link Application}.
     */
    public static Application getApplication(String className) {
        return ReflectUtils.newInstance(className, Application.class);
    }

    /**
     * Constructor.
     * 
     * @param application
     *            An application to introspect.
     */
    public static Definition getDefinition(Application application,
            Reference baseRef, boolean useSectionNamingPackageStrategy) {
        // method kept for retro compatibility
        return getDefinition(application, null, null, baseRef,
                useSectionNamingPackageStrategy);
    }

    /**
     * Constructor.
     * 
     * @param application
     *            An application to introspect.
     */
    public static Definition getDefinition(Application application,
            String applicationName, List<Class> resources, Reference baseRef,
            boolean useSectionNamingPackageStrategy) {

        List<IntrospectionHelper> introspectionHelpers = IntrospectionUtils
                .getIntrospectionHelpers();
        Definition definition = new Definition();

        CollectInfo collectInfo = new CollectInfo();
        collectInfo
                .setUseSectionNamingPackageStrategy(useSectionNamingPackageStrategy);

        if (baseRef != null) {
            collectInfo.setApplicationPath(baseRef.getPath());
        } else if (application != null) {
            ApplicationPath applicationPath = application.getClass()
                    .getAnnotation(ApplicationPath.class);
            if (applicationPath != null) {
                collectInfo.setApplicationPath(applicationPath.value());
            }
        }

        List<Class> allResources = getAllResources(application, resources);
        scanResources(collectInfo, allResources, introspectionHelpers);

        if (applicationName != null) {
            collectInfo.setApplicationName(applicationName);
        } else if (application != null) {
            collectInfo.setApplicationName(application.getClass().getName());
        } else {
            collectInfo.setApplicationName("JAXRS-Application");
        }

        updateDefinitionContract(collectInfo, application, definition);

        Contract contract = definition.getContract();
        // add resources
        contract.setResources(collectInfo.getResources());
        // add representations
        contract.setRepresentations(collectInfo.getRepresentations());
        // add sections
        contract.setSections(collectInfo.getSections());

        addEndpoints(collectInfo.getApplicationPath(), definition);

        sortDefinition(definition);

        updateRepresentationsSectionsFromResources(definition);

        if (application != null) {
            for (IntrospectionHelper helper : introspectionHelpers) {
                helper.processDefinition(definition, application.getClass());
            }
        }

        return definition;
    }

    public static List<Class> getAllResources(Application application,
            List<Class> resources) {
        List<Class> allResources = new ArrayList<>();
        if (application != null) {
            if (application.getClasses() != null) {
                allResources.addAll(application.getClasses());
            }
            if (application.getSingletons() != null) {
                for (Object singleton : application.getSingletons()) {
                    if (singleton != null) {
                        allResources.add(singleton.getClass());
                    }
                }
            }
        }
        if (resources != null) {
            allResources.addAll(resources);
        }
        return allResources;
    }

    private static Header getHeader(TypeInfo typeInfo, String defaultValue,
            HeaderParam headerParam) {
        Header header = new Header();
        header.setName(headerParam.value());
        header.setType(typeInfo.getRepresentationName());
        header.setAllowMultiple(typeInfo.isList());
        header.setRequired(false);
        header.setDescription(StringUtils.isNullOrEmpty(defaultValue) ? ""
                : "Value: " + defaultValue);
        header.setDefaultValue(defaultValue);
        return header;
    }

    private static String getPathOrNull(Path path) {
        if (path != null) {
            return path.value();
        } else {
            return null;
        }
    }

    private static PathVariable getPathVariable(TypeInfo typeInfo,
            PathParam pathParam) {
        PathVariable pathVariable = new PathVariable();
        pathVariable.setName(pathParam.value());
        pathVariable.setType(typeInfo.getRepresentationName());
        return pathVariable;
    }

    private static QueryParameter getQueryParameter(TypeInfo typeInfo,
            String defaultValue, QueryParam queryParam) {
        QueryParameter queryParameter = new QueryParameter();
        queryParameter.setName(queryParam.value());
        queryParameter.setType(typeInfo.getRepresentationName());
        queryParameter.setAllowMultiple(typeInfo.isList());
        queryParameter.setRequired(false);
        queryParameter
                .setDescription(StringUtils.isNullOrEmpty(defaultValue) ? ""
                        : "Value: " + defaultValue);
        queryParameter.setDefaultValue(defaultValue);
        return queryParameter;
    }

    private static String getResourceMethod(Method method) {
        if (method.getAnnotation(HEAD.class) != null) {
            return org.restlet.data.Method.HEAD.getName();
        }
        if (method.getAnnotation(OPTIONS.class) != null) {
            return org.restlet.data.Method.OPTIONS.getName();
        }
        if (method.getAnnotation(GET.class) != null) {
            return org.restlet.data.Method.GET.getName();
        }
        if (method.getAnnotation(PUT.class) != null) {
            return org.restlet.data.Method.PUT.getName();
        }
        if (method.getAnnotation(POST.class) != null) {
            return org.restlet.data.Method.POST.getName();
        }
        if (method.getAnnotation(DELETE.class) != null) {
            return org.restlet.data.Method.DELETE.getName();
        }
        if (method.getAnnotation(HttpMethod.class) != null) {
            return method.getAnnotation(HttpMethod.class).value();
        }
        // not a resource method
        return null;
    }

    private static boolean isResourceMethod(Method method) {
        return (method.getAnnotation(HEAD.class) != null
                || method.getAnnotation(OPTIONS.class) != null
                || method.getAnnotation(GET.class) != null
                || method.getAnnotation(PUT.class) != null
                || method.getAnnotation(POST.class) != null
                || method.getAnnotation(DELETE.class) != null || method
                    .getAnnotation(HttpMethod.class) != null);
    }

    private static String joinPaths(String... nullablePaths) {
        StringBuilder result = new StringBuilder();

        // keep only not null paths
        List<String> paths = new ArrayList<>();
        for (String path : nullablePaths) {
            if (!StringUtils.isNullOrEmpty(path)) {
                paths.add(path);
            }
        }

        // clean "/" and append paths
        int lastPathIndex = paths.size() - 1;
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);

            if (!path.startsWith("/")) {
                result.append("/");
            }
            if (i != lastPathIndex && path.endsWith("/")) {
                // remove last "/" if path is not the last one
                result.append(path.substring(0, path.length() - 1));
            } else {
                result.append(path);
            }
        }

        if (result.length() == 0) {
            result.append("/");
        }

        return result.toString();
    }

    private static void scanClazz(CollectInfo collectInfo, Class<?> clazz,
            List<? extends IntrospectionHelper> introspectionHelper) {
        ClazzInfo clazzInfo = new ClazzInfo();

        // Introduced by Jax-rs 2.0
        // ConstrainedTo ct = clazz.getAnnotation(ConstrainedTo.class);
        // value = RuntimeType.SERVER

        clazzInfo.setClazz(clazz);

        Path path = clazz.getAnnotation(Path.class);
        clazzInfo.setPath(path);

        Consumes consumes = clazz.getAnnotation(Consumes.class);
        clazzInfo.setConsumes(consumes);

        Produces produces = clazz.getAnnotation(Produces.class);
        clazzInfo.setProduces(produces);

        // TODO Do we support encoded annotation?
        // Encoded e = clazz.getAnnotation(Encoded.class);

        // Scan constructor
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length == 1) {
            scanConstructor(constructors[0], clazzInfo);
        } else if (constructors.length > 1) {
            Constructor<?> selectedConstructor = null;
            int fieldsCount = -1;
            // should select the constructor with the most fields (jaxrs
            // specification)
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterTypes().length > fieldsCount) {
                    selectedConstructor = constructor;
                    fieldsCount = constructor.getParameterTypes().length;
                }
            }
            scanConstructor(selectedConstructor, clazzInfo);
        }

        // Scan Fields
        Field[] fields = ReflectUtils.getAllDeclaredFields(clazz);
        if (fields != null) {
            for (Field field : fields) {
                scanField(field, clazzInfo);
            }
        }

        // todo authentication protocol

        // First scan bean properties methods ("simple"), then scan resource
        // methods
        List<Method> resourceMethods = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (isResourceMethod(method)) {
                    resourceMethods.add(method);
                } else {
                    scanSimpleMethod(collectInfo, method, clazzInfo);
                }
            }
        }

        for (Method resourceMethod : resourceMethods) {
            scanResourceMethod(collectInfo, clazzInfo, resourceMethod,
                    introspectionHelper);
        }

    }

    private static void scanConstructor(Constructor<?> constructor,
            ClazzInfo clazzInfo) {

        // Scan parameters
        Annotation[][] parameterAnnotations = constructor
                .getParameterAnnotations();
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();

        scanParameters(clazzInfo, parameterAnnotations, parameterTypes,
                genericParameterTypes);
    }

    private static void scanField(Field field, ClazzInfo clazzInfo) {
        TypeInfo typeInfo;
        try {
            typeInfo = Types.getTypeInfo(field.getType(),
                    field.getGenericType());
        } catch (UnsupportedTypeException e) {
            LOGGER.warning("Could not add field " + field + ". "
                    + e.getMessage());
            return;
        } // Introduced by Jax-rs 2.0
          // BeanParam beanparam = field.getAnnotation(BeanParam.class);

        DefaultValue defaultvalue = field.getAnnotation(DefaultValue.class);
        String defaultValueString = defaultvalue != null ? defaultvalue.value()
                : null;

        // TODO Do we support encoded annotation?
        // Encoded encoded = field.getAnnotation(Encoded.class);

        // TODO Do we support cookie params?
        // CookieParam cookieParam = field.getAnnotation(CookieParam.class);

        // TODO Do we support matrix params?
        // MatrixParam matrixParam = field.getAnnotation(MatrixParam.class);

        HeaderParam headerParam = field.getAnnotation(HeaderParam.class);
        if (headerParam != null) {
            Header header = getHeader(typeInfo, defaultValueString, headerParam);
            clazzInfo.addHeader(header);
        }

        PathParam pathParam = field.getAnnotation(PathParam.class);
        if (pathParam != null) {
            PathVariable pathVariable = getPathVariable(typeInfo, pathParam);
            clazzInfo.addPathVariable(pathVariable);
        }
        QueryParam queryParam = field.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            QueryParameter queryParameter = getQueryParameter(typeInfo,
                    defaultValueString, queryParam);
            clazzInfo.addQueryParameter(queryParameter);
        }
    }

    private static void scanParameters(ClazzInfo clazzInfo,
            Annotation[][] parameterAnnotations, Class<?>[] parameterTypes,
            Type[] genericParameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            TypeInfo typeInfo;
            try {
                typeInfo = Types.getTypeInfo(parameterTypes[i],
                        genericParameterTypes[i]);
            } catch (UnsupportedTypeException e) {
                LOGGER.warning("Could not scan parameter "
                        + Types.toString(parameterTypes[i],
                                genericParameterTypes[i]) + ". "
                        + e.getMessage());
                continue;
            }

            for (Annotation annotation : annotations) {
                String defaultValue = null;

                if (annotation instanceof DefaultValue) {
                    defaultValue = ((DefaultValue) annotation).value();
                }
                if (annotation instanceof HeaderParam) {
                    Header header = getHeader(typeInfo, defaultValue,
                            (HeaderParam) annotation);
                    clazzInfo.addHeader(header);
                }
                if (annotation instanceof PathParam) {
                    PathVariable pathVariable = getPathVariable(typeInfo,
                            (PathParam) annotation);
                    clazzInfo.addPathVariable(pathVariable);
                }
                if (annotation instanceof QueryParam) {
                    QueryParameter queryParameter = getQueryParameter(typeInfo,
                            defaultValue, (QueryParam) annotation);
                    clazzInfo.addQueryParameter(queryParameter);
                }
            }
        }
    }

    private static void scanResourceMethod(CollectInfo collectInfo,
            ClazzInfo clazzInfo, Method method,
            List<? extends IntrospectionHelper> introspectionHelper) {
        // "Path" decides on which resource to put this method
        Path path = method.getAnnotation(Path.class);

        String fullPath = joinPaths(collectInfo.getApplicationPath(),
                getPathOrNull(clazzInfo.getPath()), getPathOrNull(path));

        String cleanPath = cleanPath(fullPath);

        // add operation
        Operation operation = new Operation();

        operation.setMethod(getResourceMethod(method));

        if (StringUtils.isNullOrEmpty(operation.getName())) {
            LOGGER.warning("Java method " + method.getName()
                    + " has no Method name.");
            operation.setName(method.getName());
        }

        Consumes consumes = method.getAnnotation(Consumes.class);
        if (consumes != null) {
            operation.setConsumes(Arrays.asList(consumes.value()));
        } else if (clazzInfo.getConsumes() != null) {
            operation.setConsumes(Arrays
                    .asList(clazzInfo.getConsumes().value()));
        }

        Produces produces = method.getAnnotation(Produces.class);
        if (produces != null) {
            operation.setProduces(Arrays.asList(produces.value()));
        } else if (clazzInfo.getProduces() != null) {
            operation.setProduces(Arrays
                    .asList(clazzInfo.getProduces().value()));
        }

        // Retrieve a copy of header parameters declared at class level before
        // adding header parameters declared at method level
        Map<String, Header> headers = clazzInfo.getHeadersCopy();
        // Retrieve a copy of path variables declared at class level before
        // adding path variables declared at method level
        Map<String, PathVariable> pathVariables = clazzInfo
                .getPathVariablesCopy();
        // Retrieve a copy of query parameters declared at class level before
        // adding query parameters declared at method level
        Map<String, QueryParameter> queryParameters = clazzInfo
                .getQueryParametersCopy();

        // Scan method parameters
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            TypeInfo typeInfo;
            try {
                typeInfo = Types.getTypeInfo(parameterTypes[i],
                        genericParameterTypes[i]);
            } catch (UnsupportedTypeException e) {
                LOGGER.warning("Could not scan parameter "
                        + Types.toString(parameterTypes[i],
                                genericParameterTypes[i]) + " of method "
                        + method + ". " + e.getMessage());
                continue;
            }

            for (Annotation annotation : annotations) {
                String defaultValue = null;

                boolean isEntity = true;

                if (annotation instanceof DefaultValue) {
                    defaultValue = ((DefaultValue) annotation).value();
                }
                if (annotation instanceof FormParam) {
                    isEntity = false;
                    addRepresentation(collectInfo, typeInfo,
                            introspectionHelper);
                }
                if (annotation instanceof HeaderParam) {
                    isEntity = false;
                    Header header = getHeader(typeInfo, defaultValue,
                            (HeaderParam) annotation);
                    headers.put(header.getName(), header);
                }
                if (annotation instanceof PathParam) {
                    isEntity = false;
                    PathVariable pathVariable = getPathVariable(typeInfo,
                            (PathParam) annotation);
                    pathVariables.put(pathVariable.getName(), pathVariable);
                }
                if (annotation instanceof QueryParam) {
                    isEntity = false;
                    QueryParameter queryParameter = getQueryParameter(typeInfo,
                            defaultValue, (QueryParam) annotation);
                    queryParameters.put(queryParameter.getName(),
                            queryParameter);
                }
                if (annotation instanceof MatrixParam) {
                    // not supported
                    isEntity = false;
                }
                if (annotation instanceof CookieParam) {
                    // not supported
                    isEntity = false;
                }
                if (annotation instanceof Context) {
                    // not supported
                    isEntity = false;
                }

                // check if the parameter is an entity (no annotation)
                if (isEntity) {
                    addRepresentation(collectInfo, typeInfo,
                            introspectionHelper);

                    PayLoad inputEntity = new PayLoad();
                    inputEntity.setType(typeInfo.getRepresentationName());
                    inputEntity.setArray(ReflectUtils
                            .isListType(parameterTypes[i]));
                    operation.setInputPayLoad(inputEntity);

                }
            }

        }
        operation.getQueryParameters().addAll(queryParameters.values());

        // Describe the success response

        Response response = new Response();

        if (method.getReturnType() != Void.TYPE) {
            TypeInfo outputTypeInfo = Types.getTypeInfo(method.getReturnType(),
                    method.getGenericReturnType());
            // Output representation
            addRepresentation(collectInfo, outputTypeInfo, introspectionHelper);

            PayLoad outputEntity = new PayLoad();
            if (javax.ws.rs.core.Response.class.isAssignableFrom(outputTypeInfo
                    .getRepresentationClazz())) {
                outputEntity.setType("file");
            } else {
                outputEntity.setType(outputTypeInfo.getRepresentationName());
            }
            outputEntity.setArray(outputTypeInfo.isList());

            response.setOutputPayLoad(outputEntity);
        }

        response.setCode(Status.SUCCESS_OK.getCode());
        response.setName("Success");
        response.setDescription("");
        response.setMessage(Status.SUCCESS_OK.getDescription());
        operation.getResponses().add(response);

        Resource resource = collectInfo.getResource(cleanPath);
        if (resource == null) {
            resource = new Resource();
            resource.setResourcePath(cleanPath);

            // set name from class
            String name = clazzInfo.getClazz().getSimpleName();
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
            resource.getPathVariables().addAll(pathVariables.values());

            // set section from package
            if (collectInfo.isUseSectionNamingPackageStrategy()) {
                String sectionName = clazzInfo.getClazz().getPackage()
                        .getName();
                resource.getSections().add(sectionName);
            }

            collectInfo.addResource(resource);

            for (IntrospectionHelper helper : introspectionHelper) {
                helper.processResource(resource, clazzInfo.getClazz());
            }
        }

        resource.getOperations().add(operation);

        for (IntrospectionHelper helper : introspectionHelper) {
            helper.processOperation(resource, operation, clazzInfo.getClazz(),
                    method);
        }
    }

    /**
     * Returns a APISpark description of the current application. By default,
     * this method discovers all the resources attached to this application. It
     * can be overridden to add documentation, list of representations, etc.
     * 
     * 
     * @param collectInfo
     *            The collect info bean
     * @param resources
     *            The resources.
     * @param introspectionHelper
     *            Optional list of introspection helpers
     */
    public static void scanResources(CollectInfo collectInfo,
            List<Class> resources,
            List<? extends IntrospectionHelper> introspectionHelper) {
        for (Class<?> clazz : resources) {
            scanClazz(collectInfo, clazz, introspectionHelper);
        }
    }

    private static void scanSimpleMethod(CollectInfo collectInfo,
            Method method, ClazzInfo clazzInfo) {

        // Scan parameters
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        scanParameters(clazzInfo, parameterAnnotations, parameterTypes,
                genericParameterTypes);
    }

    private static void updateDefinitionContract(CollectInfo collectInfo,
            Application application, Definition definition) {
        // Contract
        Contract contract = new Contract();
        contract.setName(collectInfo.getApplicationName());

        // Sections
        if (application instanceof DocumentedApplication) {
            DocumentedApplication documentedApplication = (DocumentedApplication) application;
            if (documentedApplication.getSections() != null) {
                collectInfo.setSections(documentedApplication.getSections());
            }
        }
        definition.setContract(contract);
    }
}