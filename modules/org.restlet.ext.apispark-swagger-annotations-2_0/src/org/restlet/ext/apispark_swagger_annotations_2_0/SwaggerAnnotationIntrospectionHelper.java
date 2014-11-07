package org.restlet.ext.apispark_swagger_annotations_2_0;

import java.lang.reflect.Method;

import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark_swagger_annotations_2_0.internal.util.SwaggerAnnotationUtils;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Fulfills API Definition from Swagger annotation 2.0.
 * 
 * @author Manuel Boillod
 */
public class SwaggerAnnotationIntrospectionHelper implements
        IntrospectionHelper {
    @Override
    public void processDefinition(Definition definition,
            Class<?> applicationClass) {
        // no annotation exists for root definition
    }

    @Override
    public void processOperation(Resource resource, Operation operation,
            Class<?> resourceClass, Method javaMethod) {
        ApiOperation apiOperation = javaMethod
                .getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            SwaggerAnnotationUtils.processApiOperation(apiOperation, operation);
        }
        ApiResponses apiResponses = javaMethod
                .getAnnotation(ApiResponses.class);
        if (apiResponses != null) {
            SwaggerAnnotationUtils.processApiResponses(apiResponses, operation);
        }
        ApiResponse apiResponse = javaMethod.getAnnotation(ApiResponse.class);
        if (apiResponse != null) {
            SwaggerAnnotationUtils.processApiResponse(apiResponse, operation);
        }
        ApiImplicitParams apiImplicitParams = javaMethod
                .getAnnotation(ApiImplicitParams.class);
        if (apiImplicitParams != null) {
            SwaggerAnnotationUtils.processApiImplicitParams(apiImplicitParams,
                    operation);
        }
        ApiImplicitParam apiImplicitParam = javaMethod
                .getAnnotation(ApiImplicitParam.class);
        if (apiImplicitParam != null) {
            SwaggerAnnotationUtils.processApiImplicitParam(apiImplicitParam,
                    operation);
        }
    }

    @Override
    public void processProperty(Property property, Method readMethod) {
        ApiModelProperty apiModelProperty = readMethod
                .getAnnotation(ApiModelProperty.class);
        if (apiModelProperty != null) {
            SwaggerAnnotationUtils.processApiModelProperty(apiModelProperty,
                    property);
        }
    }

    @Override
    public void processRepresentation(Representation representation,
            Class<?> representationClass) {
        ApiModel apiModel = representationClass.getAnnotation(ApiModel.class);
        if (apiModel != null) {
            SwaggerAnnotationUtils.processApiModel(apiModel, representation);
        }
    }

    @Override
    public void processResource(Resource resource, Class<?> resourceClass) {
        Api api = resourceClass.getAnnotation(Api.class);
        if (api != null) {
            SwaggerAnnotationUtils.processApi(api, resource);
        }
    }
}
