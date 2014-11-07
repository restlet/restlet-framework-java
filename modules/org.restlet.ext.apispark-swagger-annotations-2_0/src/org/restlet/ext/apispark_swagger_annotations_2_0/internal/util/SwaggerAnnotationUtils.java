package org.restlet.ext.apispark_swagger_annotations_2_0.internal.util;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Parameter;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Types;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Tools for Swagger annotations.
 * 
 * @author Manuel Boillod.
 */
public class SwaggerAnnotationUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(SwaggerAnnotationUtils.class.getName());

    /**
     * Adds data from the {@link Api} annotation to the resource.
     * 
     * @param api
     *            The {@link Api} annotation.
     * @param resource
     *            The {@link Resource} to update.
     */
    public static void processApi(Api api, Resource resource) {
        if (!StringUtils.isNullOrEmpty(api.value())) {
            resource.setName(api.value());
        }
        if (!StringUtils.isNullOrEmpty(api.description())) {
            resource.setDescription(api.description());
        }
    }

    /**
     * Adds data from the {@link ApiImplicitParam} annotation to the operation.
     * 
     * @param apiImplicitParam
     *            The {@link ApiImplicitParam} annotation.
     * @param operation
     *            The {@link Operation} to update.
     */
    public static void processApiImplicitParam(
            ApiImplicitParam apiImplicitParam, Operation operation) {
        QueryParameter parameter = new QueryParameter();
        if (!StringUtils.isNullOrEmpty(apiImplicitParam.name())) {
            parameter.setName(apiImplicitParam.name());
        }
        if (!StringUtils.isNullOrEmpty(apiImplicitParam.value())) {
            parameter.setDescription(apiImplicitParam.value());
        }
        if (!StringUtils.isNullOrEmpty(apiImplicitParam.defaultValue())) {
            parameter.setDefaultValue(apiImplicitParam.defaultValue());
        }
        parameter.setRequired(apiImplicitParam.required());
        parameter.setAllowMultiple(apiImplicitParam.allowMultiple());

        operation.getQueryParameters().add(parameter);
    }

    /**
     * Adds data from the {@link ApiImplicitParams} annotation to the operation.
     * 
     * @param apiImplicitParams
     *            The {@link ApiImplicitParams} annotation.
     * @param operation
     *            The {@link Operation} to update.
     */
    public static void processApiImplicitParams(
            ApiImplicitParams apiImplicitParams, Operation operation) {
        for (ApiImplicitParam apiImplicitParam : apiImplicitParams.value()) {
            processApiImplicitParam(apiImplicitParam, operation);
        }
    }

    /**
     * Adds data from the {@link ApiModel} annotation to the representation.
     * 
     * @param apiModel
     *            The {@link ApiModel} annotation.
     * @param representation
     *            The {@link Representation} to update.
     */
    public static void processApiModel(ApiModel apiModel,
            Representation representation) {
        if (!StringUtils.isNullOrEmpty(apiModel.value())) {
            representation.setName(apiModel.value());
        }
        if (!StringUtils.isNullOrEmpty(apiModel.description())) {
            representation.setDescription(apiModel.description());
        }
        if (apiModel.parent() != null) {
            representation.setExtendedType(Types.convertPrimitiveType(apiModel
                    .parent()));
        }
    }

    /**
     * Adds data from the {@link ApiModelProperty} annotation to the
     * representation property.
     * 
     * @param apiModelProperty
     *            The {@link ApiModelProperty} annotation.
     * @param property
     *            The {@link Property} to update.
     */
    public static void processApiModelProperty(
            ApiModelProperty apiModelProperty, Property property) {
        if (!StringUtils.isNullOrEmpty(apiModelProperty.value())) {
            property.setDescription(apiModelProperty.value());
        }
        if (!StringUtils.isNullOrEmpty(apiModelProperty.dataType())) {
            property.setType(apiModelProperty.dataType());
        }
        if (!StringUtils.isNullOrEmpty(apiModelProperty.allowableValues())) {
            property.setMinOccurs(1);
            property.setMaxOccurs(1);
        }
    }

    /**
     * Adds data from the {@link ApiModelProperty} annotation to the operation.
     * 
     * @param apiOperation
     *            The {@link ApiOperation} annotation.
     * @param operation
     *            The {@link Operation} to update.
     */
    public static void processApiOperation(ApiOperation apiOperation,
            Operation operation) {
        if (!StringUtils.isNullOrEmpty(apiOperation.value())) {
            operation.setName(apiOperation.value());
        }
        if (!StringUtils.isNullOrEmpty(apiOperation.notes())) {
            operation.setDescription(apiOperation.notes());
        }
        if (!StringUtils.isNullOrEmpty(apiOperation.httpMethod())) {
            operation.setMethod(apiOperation.httpMethod());
        }
        // not implemented
        // if (!StringUtils.isNullOrEmpty(apiOperation.tags())) {
        // operation.setSections(apiOperation.tags());
        // }
        if (!StringUtils.isNullOrEmpty(apiOperation.consumes())) {
            operation.setConsumes(StringUtils.splitAndTrim(apiOperation
                    .consumes()));
        }
        if (!StringUtils.isNullOrEmpty(apiOperation.produces())) {
            operation.setProduces(StringUtils.splitAndTrim(apiOperation
                    .produces()));
        }
    }

    /**
     * Adds data from the {@link ApiParam} annotation to the parameter.
     * 
     * @param apiParam
     *            The {@link ApiParam} annotation.
     * @param parameter
     *            The {@link Parameter} to update.
     */
    public static void processApiParameter(ApiParam apiParam,
            Parameter parameter) {
        if (!StringUtils.isNullOrEmpty(apiParam.name())) {
            parameter.setName(apiParam.name());
        }
        if (!StringUtils.isNullOrEmpty(apiParam.value())) {
            parameter.setDescription(apiParam.value());
        }
        if (!StringUtils.isNullOrEmpty(apiParam.defaultValue())) {
            parameter.setDefaultValue(apiParam.defaultValue());
        }
        parameter.setRequired(apiParam.required());
        parameter.setAllowMultiple(apiParam.allowMultiple());
    }

    /**
     * Adds data from the {@link ApiResponse} annotation to the operation.
     * 
     * @param apiResponse
     *            The {@link ApiResponse} annotation.
     * @param operation
     *            The {@link Operation} to update.
     */
    public static void processApiResponse(ApiResponse apiResponse,
            Operation operation) {
        if (operation.getResponses() == null) {
            operation.setResponses(new ArrayList<Response>());
        }
        Response response = new Response();
        response.setCode(apiResponse.code());
        if (!StringUtils.isNullOrEmpty(apiResponse.message())) {
            response.setDescription(apiResponse.message());
        }
        if (apiResponse.response() != null) {
            PayLoad payLoad = new PayLoad();
            payLoad.setType(Types.convertPrimitiveType(apiResponse.response()));
            response.setOutputPayLoad(payLoad);
        }
        operation.getResponses().add(response);
    }

    /**
     * Adds data from the {@link ApiResponses} annotation to the operation.
     * 
     * @param apiResponses
     *            The {@link ApiResponses} annotation.
     * @param operation
     *            The {@link Operation} to update.
     */
    public static void processApiResponses(ApiResponses apiResponses,
            Operation operation) {
        for (ApiResponse apiResponse : apiResponses.value()) {
            processApiResponse(apiResponse, operation);
        }
    }
}
