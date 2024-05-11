/**
 * Copyright 2005-2020 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.rebind;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

import org.restlet.client.Client;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.Form;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.data.Preference;
import org.restlet.client.data.Protocol;
import org.restlet.client.engine.resource.GwtClientProxy;
import org.restlet.client.representation.ObjectRepresentation;
import org.restlet.client.representation.Representation;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.ClientResource;
import org.restlet.client.resource.Delete;
import org.restlet.client.resource.ExceptionHandler;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Post;
import org.restlet.client.resource.Put;
import org.restlet.client.resource.ResourceException;
import org.restlet.client.resource.Result;
import org.restlet.client.resource.Status;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.rpc.RestletBlackListTypeFilter;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracle;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracleBuilder;
import com.google.gwt.user.rebind.rpc.TypeSerializerCreator;

/**
 * Deferred binding generator capable of creating a subclass of
 * {@link ClientProxy} implementing a custom Java interface, extending the
 * marker {@link ClientProxy} interface, and annotated with Restlet annotations
 * such as {@link Get}, {@link Post}, {@link Put} or {@link Delete}.
 * 
 * @author Jerome Louvel
 */
public class ClientProxyGenerator extends com.google.gwt.core.ext.Generator {

    /** The name of the proxy class that will be generated. */
    private String className;

    /** The parent type. */
    private JClassType classType;

    /** Provides metadata to defered binding generators. */
    private GeneratorContext context;

    /** The logger used by the deferred binding generators. */
    private TreeLogger logger;

    /** The package name of the parent type. */
    private String packageName;

    /** Used to write the content of the proxy class. */
    private SourceWriter sourceWriter;

    /** Represents the serializer class. */
    private SerializableTypeOracle sto;

    /** The builder of the serializer class. */
    private SerializableTypeOracleBuilder stob;

    /** The type oracle for the current generator context. */
    private TypeOracle typeOracle;

    /**
     * The parent type for which a default constructible subclass will be generated.
     */
    private String typeQName;

    /**
     * Default constructor.
     */
    public ClientProxyGenerator() {
        this.logger = null;
        this.classType = null;
        this.context = null;
        this.className = null;
        this.packageName = null;
        this.typeQName = null;
        this.sourceWriter = null;
        this.typeOracle = null;
        this.stob = null;
        this.sto = null;
    }

    /**
     * Add a new type to be serialized.
     * 
     * @param type
     *            The type to add.
     */
    private void addRootType(JType type) {
        try {
            if (!(type.isClass() != null
                    && (getTypeOracle().getType(Representation.class.getName()).isAssignableFrom(type.isClass())
                            || getTypeOracle().getType(Form.class.getName()).isAssignableFrom(type.isClass())))) {
                // Do not render Representation and Form instances
                getStob().addRootType(getLogger(), type);
            }
        } catch (Exception e) {
            getStob().addRootType(getLogger(), type);
        }
    }

    /**
     * Generates the name of the given parameter type.
     * 
     * @param parameterType
     *            the Java parameter type.
     * @param genericParameterType
     *            the Java formal parameter type
     * @return
     */
    private static String buildParameterTypeName(Class<?> parameterType, java.lang.reflect.Type genericParameterType) {
        java.lang.reflect.Type type = (genericParameterType instanceof ParameterizedType)
                ? ((ParameterizedType) genericParameterType).getActualTypeArguments()[0]
                : null;

        return getTypeName(type == null ? parameterType : genericParameterType);
    }

    /**
     * Generates the name of the given type into the given StringBuilder.
     * 
     * @param type
     *            The type.
     * @param sb
     *            The stringBuilder to complete.
     */
    private static void buildTypeName(java.lang.reflect.Type type, StringBuilder sb) {
        if (type instanceof Class<?>) {
            if (((Class<?>) type).isArray()) {
                buildTypeName(((Class<?>) type).getComponentType(), sb);
                sb.append("[]");
            } else {
                sb.append(((Class<?>) type).getName());
            }
        } else if (type instanceof GenericArrayType) {
            buildTypeName(((GenericArrayType) type).getGenericComponentType(), sb);
            sb.append("[]");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            buildTypeName(t.getRawType(), sb);
            sb.append("<");

            if (t.getActualTypeArguments().length >= 1) {
                buildTypeName(t.getActualTypeArguments()[0], sb);

                for (int i = 1; i < t.getActualTypeArguments().length; i++) {
                    sb.append(", ");
                    buildTypeName(t.getActualTypeArguments()[i], sb);
                }
            }

            sb.append(">");
        } else {
            sb.append(type.toString());
        }
    }

    private static String getTypeName(java.lang.reflect.Type type) {
        StringBuilder sb = new StringBuilder();
        buildTypeName(type, sb);
        return sb.toString();
    }

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeQName)
            throws UnableToCompleteException {
        String result = null;

        try {
            this.logger = logger;
            this.context = context;
            this.typeQName = typeQName;
            this.sourceWriter = null;
            this.typeOracle = context.getTypeOracle();
            this.classType = typeOracle.getType(typeQName);
            this.packageName = getClassType().getPackage().getName();
            this.className = getClassType().getSimpleSourceName() + "Impl";
            generateProxy();
            result = getPackageName() + "." + getClassName();
        } catch (Exception e) {
            e.printStackTrace();
            UnableToCompleteException utce = new UnableToCompleteException();
            utce.initCause(e);
            throw utce;
        }

        return result;
    }

    /**
     * Generates the default constructor.
     */
    protected void generateContructor() {
        println("public " + getClassName() + "() {");
        indent();
        println("super(GWT.getModuleBaseURL(),");
        indent();
        println("SERIALIZATION_POLICY, ");
        println("SERIALIZER);");
        outdent();
        outdent();
        println("}");
    }

    /**
     * Generates the private members of the proxy class.
     * 
     * @param serializableTypeOracle
     *            The type of the object serializer/deserializer.
     */
    protected void generateFields(SerializableTypeOracle serializableTypeOracle) {
        String tsn = getTypeSerializerQualifiedName(getClassType());

        println("private static final String SERIALIZATION_POLICY =\"null\";");
        println("private static final " + tsn + " SERIALIZER = new " + tsn + "();");
    }

    /**
     * Generates the code of a given Java method (must be correctly annotated using
     * Restlet annotation).
     * 
     * @param method
     *            The Java method.
     * @throws Exception
     */
    protected void generateMethod(java.lang.reflect.Method method) throws Exception {
        AnnotationInfo info = AnnotationUtils.getAnnotation(method);

        if (info == null) {
            if (method.getAnnotations() != null && method.getAnnotations().length > 0) {
                // Try to detect anomalies in the annotations definition
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().getName().startsWith("org.restlet.client.resource")) {
                        throw new IllegalArgumentException("The " + getTypeQName() + " interface contains a \""
                                + method.getName()
                                + "\" method that refers to an annotation that does not come from the Restlet edition for GWT: "
                                + annotation.annotationType().getName()
                                + ". The correct package name must be \"org.restlet.client.resource\".");
                    }
                }
            }
            getLogger().log(Type.WARN, "The " + getTypeQName() + " interface contains a \"" + method.getName()
                    + "\" method without a proper annotation taken from the Restlet edition for GWT.");
        } else {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                print("public ");
            } else if (Modifier.isPrivate(modifiers)) {
                print("private ");
            } else if (Modifier.isProtected(modifiers)) {
                print("protected ");
            }

            print("void " + method.getName() + "(");

            Class<?>[] parameterTypes = method.getParameterTypes();
            java.lang.reflect.Type[] genericParameterTypes = method.getGenericParameterTypes();

            ParameterType callbackParameterType = null;
            ParameterType requestEntityParameterType = null;
            java.util.Map<Integer, ExceptionHandlerParameter> exceptionHandlerParameterByStatusCode = new java.util.HashMap<>();

            // write the list of parameters
            for (int i = 0; i < parameterTypes.length; i++) {
                ParameterType parameterType = new ParameterType(parameterTypes[i], genericParameterTypes[i]);

                if (i > 0) {
                    print(", ");
                }
                if (parameterType.isA(AsyncCallback.class) || parameterType.isA(Result.class)) {
                    callbackParameterType = parameterType;
                    print("final " + parameterType.asString + " callback");

                } else if (parameterType.isA(ExceptionHandler.class)) {
                    if (parameterType.genericType instanceof ParameterizedType) {
                        java.lang.reflect.Type exceptionType = ((ParameterizedType) parameterType.genericType)
                                .getActualTypeArguments()[0];
                        Class<?> exceptionClass = Class.forName(getTypeName(exceptionType));
                        Status statusAnnotation = exceptionClass.getAnnotation(Status.class);

                        String parameterName = "exceptionHandler" + i;
                        if (statusAnnotation != null) {
                            int code = statusAnnotation.value();
                            exceptionHandlerParameterByStatusCode.put(code,
                                    new ExceptionHandlerParameter(getTypeName(exceptionType), parameterName));
                        }

                        print("final " + parameterType.asString + " " + parameterName);

                    } else {
                        getLogger().log(Type.WARN,
                                "The " + getTypeQName() + " interface contains a \"" + method.getName()
                                        + "\" method that declares an exception handler without Exception: "
                                        + parameterType.asString);
                        print("final " + parameterType.asString + " param" + i);
                    }
                } else if (i == 0) {
                    // By convention, only the first parameter can be passed as single parameter of
                    // the callback method.
                    requestEntityParameterType = parameterType;
                    print("final " + parameterType.asString + " requestEntity");

                } else {
                    print("final " + parameterType.asString + " param" + i);
                }
            }

            println(") {");
            indent();

            println("final SerializationStreamFactory serializationStreamFactory = (SerializationStreamFactory) "
                    + getClassName() + ".this;");
            println("final ClientResource clientResource = getClientResource();");

            if (requestEntityParameterType != null) {

                if (requestEntityParameterType.isA(Representation.class)) {
                    println("clientResource.getRequest().setEntity(requestEntity);");
                } else if (requestEntityParameterType.isA(Form.class)) {
                    println("clientResource.getRequest().setEntity((requestEntity == null) ? null : requestEntity.getWebRepresentation());");
                } else {
                    println("Representation requestRepresentation = new ObjectRepresentation<"
                            + requestEntityParameterType.asString + ">(serializationStreamFactory, requestEntity);");
                    println("clientResource.getRequest().setEntity(requestRepresentation);");
                }
            } else {
                println("clientResource.getRequest().setEntity(null);");
            }

            println();
            println("if (clientResource.getClientInfo().getAcceptedMediaTypes().isEmpty()) {");
            indent();
            println("clientResource.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT_GWT));");
            outdent();
            println("}");
            println();
            println("clientResource.setMethod(Method." + info.getRestletMethod().toString() + ");");

            // Generate the callback
            println("clientResource.setOnResponse(new Uniform() {");
            indent();
            println("@Override");
            println("public void handle(final Request request, final Response response) {");
            indent();
            println("if (clientResource.getStatus().isError()) {");
            indent();
            generateErrorResponseCallback(exceptionHandlerParameterByStatusCode);
            outdent();
            println("} else {");
            indent();

            if (callbackParameterType != null) {
                generateSuccessResponseCallback(callbackParameterType);
            } else {
                getLogger().log(Type.WARN, "The " + getTypeQName() + " interface contains a \"" + method.getName()
                        + "\" method without a callback.");
            }

            outdent();
            println("}");
            outdent();
            println("}");
            outdent();
            println("});");

            println("clientResource.handle();");

            outdent();
            println("}");

        }
    }

    private void generateSuccessResponseCallback(ParameterType callbackParameterType) {
        java.lang.reflect.Type callbackType = (callbackParameterType.genericType instanceof ParameterizedType)
                ? ((ParameterizedType) callbackParameterType.genericType).getActualTypeArguments()[0]
                : null;

        if (callbackType == null || Void.class.equals(callbackType)) {
            println("callback.onSuccess(null);");
        } else {
            String callbackTypeAsString = getTypeName(callbackType);
            boolean callbackHandlesAString = String.class.getName().equals(callbackTypeAsString);

            println(callbackTypeAsString + " result = null;");
            println("boolean serializationError = false;");
            println();
            println("try {");
            indent();
            println("if(response.isEntityAvailable()){");
            indent();
            println("if (MediaType.APPLICATION_JAVA_OBJECT_GWT.equals(response.getEntity().getMediaType())) {");
            indent();
            println("result = new ObjectRepresentation<" + callbackTypeAsString + ">(");
            indent();
            println("response.getEntity().getText(), serializationStreamFactory, " + callbackHandlesAString + ")");
            println(".getObject();");
            outdent();
            outdent();
            println("} else {");
            indent();
            if (callbackHandlesAString) {
                println("result = response.getEntity().getText();");
            } else {
                println("callback.onFailure(new ResourceException(new IOException(\"Can't parse the enclosed entity because of its media type. Expected <\" + MediaType.APPLICATION_JAVA_OBJECT_GWT + \"> but was <\" + response.getEntity().getMediaType() + \">. Make sure you have added the org.restlet.client.ext.gwt.jar file to your server.\")));");
            }
            outdent();
            println("}");
            outdent();
            println("}");
            outdent();
            println("} catch (Throwable e) {");
            indent();
            println("// Serialization error, considered as a communication error.");
            println("serializationError = true;");
            println("callback.onFailure(new ResourceException(e));");
            outdent();
            println("}");
            println();
            println("if (!serializationError) {");
            indent();
            println("callback.onSuccess(result);");
            outdent();
            println("}");
        }
    }

    private void generateErrorResponseCallback(
            java.util.Map<Integer, ExceptionHandlerParameter> exceptionHandlerParameterByStatusCode) {
        println("if (!response.isEntityAvailable()) {");
        indent();
        println("callback.onFailure(new ResourceException(getClientResource().getStatus()));");
        outdent();
        println("} else {");
        indent();
        println("Representation lRepresentation = response.getEntity();");
        println("MediaType lMediaType = lRepresentation.getMediaType();");

        println("if (!MediaType.APPLICATION_JAVA_OBJECT_GWT.equals(lMediaType)) {");
        indent();
        println("callback.onFailure(new IOException(\"Can't parse the enclosed entity because of its media type. Expected <\" + MediaType.APPLICATION_JAVA_OBJECT_GWT + \"> but was <\" + response.getEntity().getMediaType() + \">. Make sure you have added the org.restlet.client.ext.gwt.jar file to your server.\"));");
        outdent();
        println("} else {");
        indent();
        println("try {");
        indent();
        for (int code : exceptionHandlerParameterByStatusCode.keySet()) {
            ExceptionHandlerParameter exceptionHandlerParameter = exceptionHandlerParameterByStatusCode.get(code);

            println("if (" + code + " == clientResource.getStatus().getCode()) {");
            indent();
            println("ObjectRepresentation<" + exceptionHandlerParameter.handledExceptionTypeName
                    + "> lExceptionRepresentation = new ObjectRepresentation<>(lRepresentation.getText(), serializationStreamFactory, false);");
            println(exceptionHandlerParameter.handlerParameterName + ".handle(lExceptionRepresentation.getObject());");
            println("return;");
            outdent();
            println("}");
        }
        println("callback.onFailure(new ResourceException(getClientResource().getStatus()));");
        outdent();
        println("} catch (Throwable e) {");
        indent();
        println("com.google.gwt.core.client.GWT.log(\"representation => \", e);");
        println("callback.onFailure(new ResourceException(e));");
        outdent();
        println("}");
        outdent();
        println("}");
        outdent();
        println("}");
    }

    /**
     * Generates the code of the proxy class.
     * 
     * @throws Exception
     */
    protected void generateProxy() throws Exception {
        PrintWriter printWriter = getContext().tryCreate(getLogger(), getPackageName(), getClassName());

        if (printWriter != null) {
            generateSerializers();

            ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(getPackageName(),
                    getClassName());
            composer.setSuperclass(GwtClientProxy.class.getCanonicalName());
            composer.addImplementedInterface(getTypeQName());
            composer.addImport(GWT.class.getCanonicalName());
            composer.addImport(IOException.class.getCanonicalName());
            composer.addImport(Client.class.getCanonicalName());
            composer.addImport(Protocol.class.getCanonicalName());
            composer.addImport(Preference.class.getCanonicalName());
            composer.addImport(Request.class.getCanonicalName());
            composer.addImport(Response.class.getCanonicalName());
            composer.addImport(Uniform.class.getCanonicalName());
            composer.addImport(Method.class.getCanonicalName());
            composer.addImport(Representation.class.getCanonicalName());
            composer.addImport(ObjectRepresentation.class.getCanonicalName());
            composer.addImport(ResourceException.class.getCanonicalName());
            composer.addImport(AsyncCallback.class.getCanonicalName());
            composer.addImport(Result.class.getCanonicalName());
            composer.addImport(SerializationStreamFactory.class.getCanonicalName());
            composer.addImport(MediaType.class.getCanonicalName());
            composer.addImport(GwtClientProxy.class.getCanonicalName());
            composer.addImport(ClientResource.class.getCanonicalName());
            composer.addImport(ExceptionHandler.class.getCanonicalName());

            this.sourceWriter = composer.createSourceWriter(getContext(), printWriter);
            println();
            generateFields(getSto());
            println();
            generateContructor();
            println();

            // Generate the proxy methods
            Class<?> interfaceClass = Class.forName(getTypeQName());

            for (java.lang.reflect.Method method : interfaceClass.getMethods()) {
                if ("getClientResource".equals(method.getName())) {
                    continue;
                }
                if ("void".equals(method.getReturnType().getName())) {
                    generateMethod(method);
                    println();
                } else {
                    getLogger().log(Type.WARN, "The " + getTypeQName() + " interface contains a \"" + method.getName()
                            + "\" method without a void return type.");
                }
            }

            outdent();
            println("}");
            getContext().commit(getLogger(), printWriter);
        }
    }

    /**
     * Generates the code of the necessary object serializers.
     * 
     * @throws UnableToCompleteException
     */
    protected void generateSerializers() throws UnableToCompleteException {
        try {
            try {
                this.stob = getSerializableTypeOracleBuilder2_7();
            } catch (Exception e2_7) {
                try {
                    this.stob = getSerializableTypeOracleBuilder2_5();
                } catch (Exception e2_5) {
                    try {
                        this.stob = getSerializableTypeOracleBuilder2_3();
                    } catch (Exception e2_3) {
                        try {
                            this.stob = getSerializableTypeOracleBuilder2_2();
                        } catch (Exception e2_2) {
                            getLogger().log(TreeLogger.ERROR, "", e2_2);
                            throw new UnableToCompleteException();
                        }
                    }
                }
            }

            this.stob.setTypeFilter(new RestletBlackListTypeFilter(getLogger(), getContext().getPropertyOracle()));

            // Discover and add serializable types
            getStob().addRootType(getLogger(), getTypeOracle().getType(String.class.getName()));
            JMethod[] methods = getClassType().getOverridableMethods();
            JClassType exceptionClass = getTypeOracle().getType(Exception.class.getName());

            List<JType> undiscoveredTypes = Arrays.asList(getTypeOracle().getType(AsyncCallback.class.getName()),
                    getTypeOracle().getType(Result.class.getName()),
                    getTypeOracle().getType(ExceptionHandler.class.getName()));

            for (JMethod method : methods) {
                if ("getClientResource".equals(method.getName())) {
                    continue;
                }

                JType returnType = method.getReturnType();

                if (returnType != JPrimitiveType.VOID) {
                    getStob().addRootType(getLogger(), returnType);
                }

                JParameter[] parameters = method.getParameters();
                for (JParameter parameter : parameters) {
                    JType parameterType = parameter.getType();
                    JParameterizedType parameterizedType = parameterType.isParameterized();
                    if (parameterizedType == null) {
                        // Non generic type.
                        addRootType(parameterType);
                    } else if (!undiscoveredTypes.contains(parameterizedType.getBaseType())) {
                        // Generic type but not a callback.
                        addRootType(parameterType);
                    } else {
                        // Callback type, inspect the "inner" type.
                        if (parameterizedType.getTypeArgs().length > 0) {
                            JClassType t = parameterizedType.getTypeArgs()[0];
                            // Add the enclosed type if it is not equals to java.lang.Void.
                            if (t != null
                                    && !("Void".equals(t.getName()) && "java.lang".equals(t.getPackage().getName()))) {
                                addRootType(t);
                            }
                        }
                    }
                }

                JType[] exceptions = method.getThrows();
                if (exceptions.length > 0) {
                    for (JType exception : exceptions) {
                        if (!exceptionClass.isAssignableFrom(exception.isClass())) {
                            getLogger().log(Type.WARN, "Only checked exceptions are supported");
                        }
                        addRootType(exception);
                    }
                }
            }

            // Log serialization information
            OutputStream los = getContext().tryCreateResource(getLogger(),
                    getClassType().getQualifiedSourceName() + ".restlet.log");
            java.lang.reflect.Method setLogOs = null;
            Object setLogOsParameter = null;
            try {
                setLogOs = getStob().getClass().getDeclaredMethod("setLogOutputStream", OutputStream.class);
                setLogOsParameter = los;
            } catch (SecurityException e1) {
            } catch (NoSuchMethodException e) {
                try {
                    setLogOs = getStob().getClass().getDeclaredMethod("setLogOutputWriter", PrintWriter.class);
                    setLogOsParameter = new PrintWriter(los);
                } catch (SecurityException e1) {
                } catch (NoSuchMethodException e1) {
                }
            }
            if (setLogOs != null) {
                try {
                    setLogOs.invoke(getStob(), setLogOsParameter);
                } catch (Throwable e) {
                    getLogger().log(Type.WARN, "Cannot set the log writer " + setLogOs.getName());
                }
            } else {
                getLogger().log(Type.WARN, "Cannot set the log writer for the compilation phase.");
            }
            this.sto = getStob().build(getLogger());

            if (los != null) {
                getContext().commitResource(getLogger(), los).setPrivate(true);
            }

            TypeSerializerCreator tsc = null;
            try {
                tsc = getTypeSerializerCreatorGwt2_1();
            } catch (Exception e2_1) {
                try {
                    tsc = getTypeSerializerCreatorGwt2_2();
                } catch (Exception e2_2) {
                    try {
                        tsc = getTypeSerializerCreatorGwt2_0();
                    } catch (Exception e2_0) {
                        try {
                            tsc = getTypeSerializerCreatorGwt1_7();
                        } catch (Exception e1_7) {
                            getLogger().log(TreeLogger.ERROR, "", e1_7);
                            throw new UnableToCompleteException();
                        }
                    }
                }
            }
            if (tsc != null) {
                tsc.realize(getLogger());
            } else {
                getLogger().log(TreeLogger.ERROR, "Cannot create a TypeSerializerCreator instance.");
                throw new UnableToCompleteException();
            }
        } catch (NotFoundException e) {
            getLogger().log(TreeLogger.ERROR, "", e);
            throw new UnableToCompleteException();
        }
    }

    /**
     * Returns the name of the proxy class that will be generated.
     * 
     * @return The name of the proxy class that will be generated.
     */
    protected String getClassName() {
        return className;
    }

    /**
     * Returns the parent type.
     * 
     * @return The parent type.
     */
    protected JClassType getClassType() {
        return classType;
    }

    /**
     * Returns the context object that provides metadata to defered binding
     * generators.
     * 
     * @return The context object that provides metadata to defered binding
     *         generators.
     */
    protected GeneratorContext getContext() {
        return context;
    }

    /**
     * Returns the logger used by the deferred binding generator.
     * 
     * @return The logger used by the deferred binding generator.
     */
    protected TreeLogger getLogger() {
        return logger;
    }

    /**
     * Returns the package name of the parent type.
     * 
     * @return The package name of the parent type.
     */
    protected String getPackageName() {
        return packageName;
    }

    /**
     * Instantiates a SerializableTypeOracleBuilder instance using the GWT2.2 and
     * before constructor.
     * 
     * @return An instance of a {@link SerializableTypeOracleBuilder}.
     * @throws Exception
     */
    private SerializableTypeOracleBuilder getSerializableTypeOracleBuilder2_2() throws Exception {
        SerializableTypeOracleBuilder result = null;

        Constructor<SerializableTypeOracleBuilder> c = null;
        c = SerializableTypeOracleBuilder.class.getDeclaredConstructor(TreeLogger.class, PropertyOracle.class,
                TypeOracle.class);
        result = c.newInstance(getLogger(), getContext().getPropertyOracle(), getTypeOracle());

        return result;
    }

    /**
     * Instantiates a SerializableTypeOracleBuilder instance using the GWT2.3
     * constructor.
     * 
     * @return An instance of a {@link SerializableTypeOracleBuilder}.
     * @throws Exception
     */
    private SerializableTypeOracleBuilder getSerializableTypeOracleBuilder2_3() throws Exception {
        SerializableTypeOracleBuilder result = null;

        // At the date of 04 may 2011, the GWT GeneratorContextExt class
        // is considered as experimental and surely to be removed from gwt
        // package.
        Class<?> genClass = Class.forName("com.google.gwt.core.ext.GeneratorContextExt");

        Constructor<SerializableTypeOracleBuilder> c = null;
        c = SerializableTypeOracleBuilder.class.getDeclaredConstructor(TreeLogger.class, PropertyOracle.class,
                genClass);
        result = c.newInstance(getLogger(), getContext().getPropertyOracle(), getContext());

        return result;
    }

    /**
     * Instantiates a SerializableTypeOracleBuilder instance using the GWT2.5
     * constructor.
     * 
     * @return An instance of a {@link SerializableTypeOracleBuilder}.
     * @throws Exception
     */
    private SerializableTypeOracleBuilder getSerializableTypeOracleBuilder2_5() throws Exception {
        SerializableTypeOracleBuilder result = null;

        Constructor<SerializableTypeOracleBuilder> c = null;
        c = SerializableTypeOracleBuilder.class.getDeclaredConstructor(TreeLogger.class, PropertyOracle.class,
                GeneratorContext.class);
        result = c.newInstance(getLogger(), getContext().getPropertyOracle(), getContext());

        return result;
    }

    /**
     * Instantiates a SerializableTypeOracleBuilder instance using the GWT2.7
     * constructor.
     * 
     * @return An instance of a {@link SerializableTypeOracleBuilder}.
     * @throws Exception
     */
    private SerializableTypeOracleBuilder getSerializableTypeOracleBuilder2_7() throws Exception {
        SerializableTypeOracleBuilder result = null;
        Constructor<SerializableTypeOracleBuilder> c = null;
        c = SerializableTypeOracleBuilder.class.getDeclaredConstructor(TreeLogger.class, GeneratorContext.class);
        result = c.newInstance(getLogger(), getContext());
        return result;
    }

    /**
     * Returns the writer used for the content of the proxy class.
     * 
     * @return The writer used for the content of the proxy class.
     */
    protected SourceWriter getSourceWriter() {
        return sourceWriter;
    }

    /**
     * Returns the serializer class.
     * 
     * @return The serializer class.
     */
    protected SerializableTypeOracle getSto() {
        return sto;
    }

    /**
     * Returns the builder of the serializer class.
     * 
     * @return The builder of the serializer class.
     */
    protected SerializableTypeOracleBuilder getStob() {
        return stob;
    }

    /**
     * Returns the type oracle for the current generator context.
     * 
     * @return The type oracle for the current generator context.
     */
    protected TypeOracle getTypeOracle() {
        return typeOracle;
    }

    /**
     * Returns the parent type for which a default constructible subclass will be
     * generated.
     * 
     * @return The parent type for which a default constructible subclass will be
     *         generated.
     */
    protected String getTypeQName() {
        return typeQName;
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT1.7 constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt1_7() throws Exception {
        Constructor<TypeSerializerCreator> c = TypeSerializerCreator.class.getDeclaredConstructor(TreeLogger.class,
                SerializableTypeOracle.class, GeneratorContext.class, String.class);

        return c.newInstance(getLogger(), getSto(), getContext(), getTypeSerializerQualifiedName(getClassType()));
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT2.0 constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt2_0() throws Exception {
        Constructor<TypeSerializerCreator> c = TypeSerializerCreator.class.getDeclaredConstructor(TreeLogger.class,
                SerializableTypeOracle.class, SerializableTypeOracle.class, GeneratorContext.class, String.class);

        return c.newInstance(getLogger(), getSto(), getSto(), getContext(),
                getTypeSerializerQualifiedName(getClassType()));
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT2.1 constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt2_1() throws Exception {
        Constructor<TypeSerializerCreator> c = TypeSerializerCreator.class.getDeclaredConstructor(TreeLogger.class,
                SerializableTypeOracle.class, SerializableTypeOracle.class, GeneratorContext.class, String.class,
                String.class);

        return c.newInstance(getLogger(), getSto(), getSto(), getContext(),
                getTypeSerializerQualifiedName(getClassType()), getTypeSerializerSimpleName(getClassType()));
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT2.2 constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt2_2() throws Exception {
        // At the date of 14 february 2011, the GWT GeneratorContextExt class
        // is considered as experimental and surely to be removed from gwt
        // package.

        try {
            Class<?> genClass = Class.forName("com.google.gwt.core.ext.GeneratorContextExt");
            Constructor<TypeSerializerCreator> c = TypeSerializerCreator.class.getDeclaredConstructor(TreeLogger.class,
                    SerializableTypeOracle.class, SerializableTypeOracle.class, genClass, String.class, String.class);
            return c.newInstance(getLogger(), getSto(), getSto(), genClass.cast(getContext()),
                    getTypeSerializerQualifiedName(getClassType()), getTypeSerializerSimpleName(getClassType()));
        } catch (ClassNotFoundException e) {
            Constructor<TypeSerializerCreator> c = TypeSerializerCreator.class.getDeclaredConstructor(TreeLogger.class,
                    SerializableTypeOracle.class, SerializableTypeOracle.class, GeneratorContext.class, String.class,
                    String.class);
            return c.newInstance(getLogger(), getSto(), getSto(), getContext(),
                    getTypeSerializerQualifiedName(getClassType()), getTypeSerializerSimpleName(getClassType()));
        }
    }

    /**
     * Returns the qualified name of the given type serializer.
     * 
     * @param type
     *            The type serializer's type object.
     * @return The qualified name of the given type serializer.
     */
    private String getTypeSerializerQualifiedName(JClassType type) {
        StringBuilder sb = new StringBuilder();

        JType leafType = type.getLeafType();
        JClassType classOrInterface = leafType.isClassOrInterface();
        // Add the package name.
        String packageName = classOrInterface.getPackage().getName();
        if (packageName != null && packageName.length() > 0) {
            sb.append(packageName).append(".");
        }
        // Add the class name
        String className = classOrInterface.getName();
        className = className.replace('.', '_');
        sb.append(className).append("_TypeSerializer");
        return sb.toString();
    }

    /**
     * Returns the simple name of the given type serializer.
     * 
     * @param type
     *            The type serializer's type object.
     * @return The simple name of the given type serializer.
     */
    private String getTypeSerializerSimpleName(JClassType type) {
        StringBuilder sb = new StringBuilder();
        JType leafType = type.getLeafType();
        JClassType classOrInterface = leafType.isClassOrInterface();
        // Add the class name
        String className = classOrInterface.getName();
        className = className.replace('.', '_');
        sb.append(className).append("_TypeSerializer");
        return sb.toString();
    }

    /**
     * Increments the indentation of the generated source code.
     */
    protected void indent() {
        getSourceWriter().indent();
    }

    /**
     * Decrements the indentation of the generated source code.
     */
    protected void outdent() {
        getSourceWriter().outdent();
    }

    /**
     * Prints the given value in the generated source code.
     * 
     * @param value
     *            The value to write.
     */
    protected void print(String value) {
        getSourceWriter().print(value);
    }

    /**
     * Terminates the current line with a new line separator.
     */
    protected void println() {
        getSourceWriter().println();
    }

    /**
     * Prints the given value in the generated source code and terminates the
     * current line with a new line separator.
     * 
     * @param value
     *            The value to write.
     */
    protected void println(String value) {
        getSourceWriter().println(value);
    }

    private static class ParameterType {
        final Class<?> type;
        final java.lang.reflect.Type genericType;
        final String asString;

        ParameterType(final Class<?> type, final java.lang.reflect.Type genericType) {
            this.type = type;
            this.genericType = genericType;
            asString = buildParameterTypeName(type, genericType);
        }

        boolean isA(Class<?> clazz) {
            return clazz.isAssignableFrom(type);
        }
    }

    private static class ExceptionHandlerParameter {
        final String handledExceptionTypeName;
        final String handlerParameterName;

        ExceptionHandlerParameter(final String handledExceptionTypeName, final String handlerParameterName) {
            this.handledExceptionTypeName = handledExceptionTypeName;
            this.handlerParameterName = handlerParameterName;
        }
    }

}
