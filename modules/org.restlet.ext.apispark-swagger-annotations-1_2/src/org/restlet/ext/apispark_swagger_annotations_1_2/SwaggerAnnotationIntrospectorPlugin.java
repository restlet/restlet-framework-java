package org.restlet.ext.apispark_swagger_annotations_1_2;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.restlet.ext.apispark.internal.introspection.IntrospectorPlugin;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark_swagger_annotations_1_2.internal.util.SwaggerAnnotationUtils;

import java.lang.reflect.Method;

/**
 * Fulfill API Definition from swagger annotation 1.2.
 *
 * @author Manuel Boillod
 */
public class SwaggerAnnotationIntrospectorPlugin implements IntrospectorPlugin {

    @Override
    public void processDefinition(Definition definition, Class<?> applicationClazz) {
        //no annotation exists for root definition
    }

    @Override
    public void processResource(Resource resource, Class<?> resourceClazz) {
        Api api = resourceClazz.getAnnotation(Api.class);
        if (api != null) {
            SwaggerAnnotationUtils.processApi(api, resource);
        }
    }

    @Override
    public void processOperation(Resource resource, Operation operation, Class<?> clazz, Method method) {
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            SwaggerAnnotationUtils.processApiOperation(apiOperation, operation);
        }
        ApiResponses apiResponses = method.getAnnotation(ApiResponses.class);
        if (apiResponses != null) {
            SwaggerAnnotationUtils.processApiResponses(apiResponses, operation);
        }
        ApiResponse apiResponse = method.getAnnotation(ApiResponse.class);
        if (apiResponse != null) {
            SwaggerAnnotationUtils.processApiResponse(apiResponse, operation);
        }
        ApiImplicitParams apiImplicitParams = method.getAnnotation(ApiImplicitParams.class);
        if (apiImplicitParams != null) {
            SwaggerAnnotationUtils.processApiImplicitParams(apiImplicitParams, operation);
        }
        ApiImplicitParam apiImplicitParam = method.getAnnotation(ApiImplicitParam.class);
        if (apiImplicitParam != null) {
            SwaggerAnnotationUtils.processApiImplicitParam(apiImplicitParam, operation);
        }
    }

    @Override
    public void processRepresentation(Representation representation, Class<?> representationType) {
        ApiModel apiModel = representationType.getAnnotation(ApiModel.class);
        if (apiModel != null) {
            SwaggerAnnotationUtils.processApiModel(apiModel, representation);
        }
    }

    @Override
    public void processProperty(Property property, Method readMethod) {
        ApiModelProperty apiModelProperty = readMethod.getAnnotation(ApiModelProperty.class);
        if (apiModelProperty != null) {
            SwaggerAnnotationUtils.processApiModelProperty(apiModelProperty, property);
        }
    }
}
