package org.restlet.ext.swagger.v2_0.introspector;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.restlet.Application;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.ext.apispark.internal.introspection.IntrospectorPlugin;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.swagger.v2_0.SwaggerAnnotationUtils;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;

import java.lang.reflect.Field;

/**
 * Created by manu on 14/10/2014.
 */
public class SwaggerAnnotationIntrospectorPlugin implements IntrospectorPlugin {
    @Override
    public void processDefinition(Definition definition, Application application) {
        //no annotation exists for root definition
    }

    @Override
    public void processResource(Resource resource, ServerResource serverResource) {
        Api api = serverResource.getClass().getAnnotation(Api.class);
        if (api != null) {
            SwaggerAnnotationUtils.processApi(api, resource);
        }
    }

    @Override
    public void processResource(Resource resource, Directory directory) {
        Api api = directory.getClass().getAnnotation(Api.class);
        if (api != null) {
            SwaggerAnnotationUtils.processApi(api, resource);
        }
    }

    @Override
    public void processOperation(Operation operation, MethodAnnotationInfo methodAnnotationInfo) {
        ApiOperation apiOperation = methodAnnotationInfo.getJavaMethod().getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            SwaggerAnnotationUtils.processApiOperation(apiOperation, operation);
        }
        ApiResponses apiResponses = methodAnnotationInfo.getJavaMethod().getAnnotation(ApiResponses.class);
        if (apiResponses != null) {
            SwaggerAnnotationUtils.processApiResponses(apiResponses, operation);
        }
        ApiResponse apiResponse = methodAnnotationInfo.getJavaMethod().getAnnotation(ApiResponse.class);
        if (apiResponse != null) {
            SwaggerAnnotationUtils.processApiResponse(apiResponse, operation);
        }
        ApiImplicitParams apiImplicitParams = methodAnnotationInfo.getJavaMethod().getAnnotation(ApiImplicitParams.class);
        if (apiImplicitParams != null) {
            SwaggerAnnotationUtils.processApiImplicitParams(apiImplicitParams, operation);
        }
        ApiImplicitParam apiImplicitParam = methodAnnotationInfo.getJavaMethod().getAnnotation(ApiImplicitParam.class);
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
    public void processProperty(Property property, Field field) {
        ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
        if (apiModelProperty != null) {
            SwaggerAnnotationUtils.processApiModelProperty(apiModelProperty, property);
        }
    }
}
