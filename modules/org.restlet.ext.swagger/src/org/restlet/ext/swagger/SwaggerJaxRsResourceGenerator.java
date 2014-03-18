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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.restlet.ext.swagger.internal.SwaggerUtils;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Parses a JAX-RS class and generates DocumentationEndPoint for it.<br>
 * TODO To be refactored using Swagger 2.10.
 * 
 * @author Grzegorz Godlewski
 */
public class SwaggerJaxRsResourceGenerator {

    private Map<String, DocumentationEndPoint> apis;

    private Documentation documentation;

    private Map<String, DocumentationSchema> models;

    private Class<?> resourceClass;

    private String resourcePath;

    /**
     * Default constructor.
     */
    public SwaggerJaxRsResourceGenerator() {
        apis = new HashMap<String, DocumentationEndPoint>();
        documentation = new Documentation();
        models = new HashMap<String, DocumentationSchema>();
    }

    /**
     * Constructor.
     * 
     * @param resourceClass
     *            The root resource class.
     * @param resourcePath
     *            the root resource path.
     */
    public SwaggerJaxRsResourceGenerator(Class<?> resourceClass,
            String resourcePath) {
        this();
        this.resourcePath = resourcePath;
        this.resourceClass = resourceClass;
    }

    private void addApiParamImplicit(List<DocumentationParameter> retVal,
            ApiParamImplicit implicitParam) {
        if (null == implicitParam)
            return;

        DocumentationParameter documentationParameter = new DocumentationParameter();
        // implicitParam.allowableValues()
        // DocumentationAllowableValues allowableValues = new Do;
        // documentationParameter.setAllowableValues(allowableValues);
        documentationParameter.setAllowMultiple(implicitParam.allowMultiple());
        documentationParameter.setDataType(implicitParam.dataType());
        documentationParameter.setDefaultValue(implicitParam.defaultValue());
        // documentationParameter.setDescription();
        documentationParameter.setInternalDescription(implicitParam
                .internalDescription());
        documentationParameter.setName(implicitParam.name());
        // documentationParameter.setNotes(implicitParam.);
        // documentationParameter.setParamAccess(implicitParam.);
        documentationParameter.setParamType(implicitParam.paramType());
        documentationParameter.setRequired(implicitParam.required());
        // documentationParameter.setValueTypeInternal(implicitParam.);
        // documentationParameter.setWrapperName(implicitParam.);

        retVal.add(documentationParameter);
    }

    private void addParam(List<DocumentationParameter> retVal, String name,
            String type, Class<?> parameterType, ApiParam apiParam) {
        DocumentationParameter documentationParameter = new DocumentationParameter();

        if (apiParam != null) {
            // documentationParameter.setAllowableValues(allowableValues);
            documentationParameter.setAllowMultiple(apiParam.allowMultiple());
            documentationParameter.setDataType(parameterType.getSimpleName()); // TODO
            documentationParameter.setDefaultValue(apiParam.defaultValue());
            // documentationParameter.setDescription();
            documentationParameter.setInternalDescription(apiParam
                    .internalDescription());
            documentationParameter.setName(apiParam.name());
            // documentationParameter.setNotes(implicitParam.);
            // documentationParameter.setParamAccess(implicitParam.);
            // documentationParameter.setParamType(apiParam.paramType());
            documentationParameter.setRequired(apiParam.required());
            // documentationParameter.setValueTypeInternal(implicitParam.);
            // documentationParameter.setWrapperName(implicitParam.);
        }

        documentationParameter.setName(name);
        documentationParameter.setParamType(type);

        retVal.add(documentationParameter);
    }

    private DocumentationEndPoint getApi(String methodPath) {
        methodPath = SwaggerUtils.leaveOnlyPathParamNames(methodPath);

        DocumentationEndPoint endPoint = apis.get(methodPath);
        if (endPoint == null) {
            endPoint = new DocumentationEndPoint();
            endPoint.setPath(methodPath);
            apis.put(methodPath, endPoint);
        }
        return endPoint;
    }

    private DocumentationError getDocumentationError(ApiError apiErrorAnnotation) {
        if (apiErrorAnnotation == null)
            return null;

        DocumentationError error = new DocumentationError();
        error.setCode(apiErrorAnnotation.code());
        error.setReason(apiErrorAnnotation.reason());

        return error;
    }

    /**
     * Parses the resource class methods, checking for JAX-RS annotations.
     * 
     * @return The Swagger documentation.
     */
    public Documentation parse() {
        for (Method method : resourceClass.getMethods()) {
            processMethod(method, GET.class);
            processMethod(method, PUT.class);
            processMethod(method, POST.class);
            processMethod(method, DELETE.class);
            processMethod(method, HEAD.class);
            // TODO add custom HTTP Method
        }

        for (Entry<String, DocumentationEndPoint> entry : apis.entrySet()) {
            DocumentationEndPoint ep = entry.getValue();
            documentation.addApi(ep);
        }

        for (Entry<String, DocumentationSchema> entry : models.entrySet()) {
            documentation.addModel(entry.getKey(), entry.getValue());
        }

        return documentation;
    }

    private List<DocumentationParameter> parseParameters(Method method) {
        List<DocumentationParameter> retVal = new ArrayList<DocumentationParameter>();

        ApiParamsImplicit implicitParams = method
                .getAnnotation(ApiParamsImplicit.class);
        if (implicitParams != null) {
            for (ApiParamImplicit implicitParam : implicitParams.value()) {
                addApiParamImplicit(retVal, implicitParam);
            }
        }
        ApiParamImplicit implicitParam = method
                .getAnnotation(ApiParamImplicit.class);
        addApiParamImplicit(retVal, implicitParam);

        Class<?>[] parameterTypes = method.getParameterTypes();
        int cnt = 0;
        for (Annotation[] annotation : method.getParameterAnnotations()) {
            processJaxRsParam(retVal, annotation, parameterTypes[cnt++]);
        }

        return retVal;
    }

    private void processJaxRsParam(List<DocumentationParameter> retVal,
            Annotation[] parameterAnnotations, Class<?> parameterType) {
        ApiParam apiParam = null;
        String name = null;
        String type = null;

        for (Annotation annotation : parameterAnnotations) {
            if (annotation instanceof ApiParam) {
                apiParam = (ApiParam) annotation;
            }

            if (annotation instanceof PathParam) {
                PathParam annoPath = (PathParam) annotation;
                type = "path";
                name = annoPath.value();
            }

            if (annotation instanceof HeaderParam) {
                HeaderParam annoHeader = (HeaderParam) annotation;
                type = "header";
                name = annoHeader.value();
            }

            if (annotation instanceof QueryParam) {
                QueryParam annoQuery = (QueryParam) annotation;
                type = "query";
                name = annoQuery.value();
            }

            if (annotation instanceof CookieParam) {
                CookieParam annoCookie = (CookieParam) annotation;
                type = "cookie";
                name = annoCookie.value();
            }

            if (annotation instanceof FormParam) {
                FormParam annoForm = (FormParam) annotation;
                type = "form";
                name = annoForm.value();
            }

            if (annotation instanceof MatrixParam) {
                MatrixParam annoMatrix = (MatrixParam) annotation;
                type = "matrix";
                name = annoMatrix.value();
            }
        }

        if (name != null) {
            addParam(retVal, name, type, parameterType, apiParam);
        }
    }

    /**
     * 
     * @param method
     * @param httpMethodClass
     */
    private void processMethod(Method method,
            Class<? extends Annotation> httpMethodClass) {
        String methodPath = resourcePath;
        Path pathAnnotation = method.getAnnotation(Path.class);
        if (pathAnnotation != null) {
            methodPath += pathAnnotation.value() + "/";
        }
        methodPath = SwaggerUtils.cleanSlashes(methodPath);

        Annotation httpMthodAnnotation = method.getAnnotation(httpMethodClass);
        ApiOperation apiOperationAnnotation = method
                .getAnnotation(ApiOperation.class);
        if (httpMthodAnnotation != null && apiOperationAnnotation != null) {
            DocumentationEndPoint endPoint = getApi(methodPath);

            DocumentationOperation op = new DocumentationOperation();
            op.setHttpMethod(httpMthodAnnotation.annotationType()
                    .getSimpleName());
            op.setSummary(apiOperationAnnotation.value());
            op.setNotes(apiOperationAnnotation.notes());
            op.setTags(SwaggerUtils.toObjectList(apiOperationAnnotation.tags()));
            op.setNickname(method.getName());

            ApiErrors apiErrors = method.getAnnotation(ApiErrors.class);
            if (apiErrors != null) {
                for (ApiError apiError : apiErrors.value()) {
                    DocumentationError error = getDocumentationError(apiError);
                    if (error != null) {
                        op.addErrorResponse(error);
                    }
                }
            }

            DocumentationError error = getDocumentationError(method
                    .getAnnotation(ApiError.class));
            if (error != null) {
                op.addErrorResponse(error);
            }

            Class<?> returnType = method.getReturnType();
            if (returnType != null) {
                op.setResponseClass(returnType.getSimpleName()); // TODO "List["
                                                                 // + name + "]"
                                                                 // for
                                                                 // Collections
                                                                 // as Arrays

                if (!returnType.isPrimitive()) { // TODO
                    DocumentationObject documentationObject = new DocumentationObject();
                    // documentationObject.addField(field);
                    // DocumentationParameter field;
                    // documentationObject.addField(field);
                    models.put(returnType.getSimpleName(),
                            documentationObject.toDocumentationSchema());
                }
            }

            op.setParameters(parseParameters(method));

            endPoint.addOperation(op);
            endPoint.setDescription(apiOperationAnnotation.value());
        }
    }

    /**
     * Sets both root resource class and path.
     * 
     * @param resourceClass
     *            The root resource class.
     * @param resourcePath
     *            the root resource path.
     */
    public void setup(Class<?> resourceClass, String resourcePath) {
        this.resourceClass = resourceClass;
        this.resourcePath = resourcePath;
    }

}
