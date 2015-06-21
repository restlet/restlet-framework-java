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

package org.restlet.ext.apispark.internal.introspection.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.Status;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
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
            property.setRequired(true);
            property.setList(true);
        }
    }

    /**
     * Adds data from the {@link ApiModelProperty} annotation to the operation.
     * 
     * @param apiOperation
     *            The {@link com.wordnik.swagger.annotations.ApiOperation} annotation.
     * @param resource
     *            The {@link org.restlet.ext.apispark.internal.model.Resource} to update.
     * @param operation
     *            The {@link org.restlet.ext.apispark.internal.model.Operation} to update.
     */
    public static void processApiOperation(ApiOperation apiOperation,
            Resource resource, Operation operation) {
        if (!StringUtils.isNullOrEmpty(apiOperation.nickname())) {
            operation.setName(apiOperation.nickname());
        }
        if (!StringUtils.isNullOrEmpty(apiOperation.value())) {
            operation.setDescription(apiOperation.value());
        }
        if (!StringUtils.isNullOrEmpty(apiOperation.httpMethod())) {
            operation.setMethod(apiOperation.httpMethod());
        }
        if (apiOperation.tags() != null) {
            for (String tag : apiOperation.tags()) {
                if (!resource.getSections().contains(tag)) {
                    resource.getSections().add(tag);
                }
            }
        }
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
     * Adds data from the {@link ApiResponse} annotation to the operation.
     * 
     * @param apiResponse
     *            The {@link ApiResponse} annotation.
     * @param operation
     *            The {@link Operation} to update.
     * @param representationsUsed
     *            The {@link java.lang.Class} of representation used.
     */
    public static void processApiResponse(ApiResponse apiResponse,
            Operation operation, List<Class<?>> representationsUsed) {
        List<Response> responses = operation.getResponses();
        if (responses == null) {
            responses = new ArrayList<>();
            operation.setResponses(responses);
        }
        final int code = apiResponse.code();

        Optional<Response> existingResponse = Iterables.tryFind(responses,
                new Predicate<Response>() {
                    @Override
                    public boolean apply(Response response) {
                        return response.getCode() == code;
                    }
                });
        boolean responseExists = existingResponse.isPresent();
        Response response;
        if (responseExists) {
            response = existingResponse.get();
        } else {
            response = new Response();
            response.setCode(code);
        }

        response.setCode(code);
        response.setName(Status.valueOf(code).getReasonPhrase());
        if (!StringUtils.isNullOrEmpty(apiResponse.message())) {
            response.setDescription(apiResponse.message());
        }
        Class<?> responseClazz = apiResponse.response();
        if (responseClazz != null && responseClazz != Void.TYPE
                && responseClazz != Void.class) {
            representationsUsed.add(responseClazz);
            PayLoad payLoad = new PayLoad();
            payLoad.setType(Types.convertPrimitiveType(responseClazz));
            response.setOutputPayLoad(payLoad);
        }

        if (!responseExists) {
            responses.add(response);
        }
    }

    /**
     * Adds data from the {@link ApiResponses} annotation to the operation.
     * 
     * @param apiResponses
     *            The {@link ApiResponses} annotation.
     * @param operation
     *            The {@link Operation} to update.
     * @param representationsUsed
     *            The {@link java.lang.Class} of representation used.
     */
    public static void processApiResponses(ApiResponses apiResponses,
            Operation operation, List<Class<?>> representationsUsed) {
        for (ApiResponse apiResponse : apiResponses.value()) {
            processApiResponse(apiResponse, operation, representationsUsed);
        }
    }
}
