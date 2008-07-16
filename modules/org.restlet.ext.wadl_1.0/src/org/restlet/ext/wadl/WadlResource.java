/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.wadl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * Resource that is able to automatically describe itself with WADL. This
 * description can be customized by overriding the {@link #getResourceInfo()}
 * and {@link #getMethodInfo(Method)} methods.
 * 
 * When used to describe a class of resources in the context of a parent
 * application, a special instance will be created using the default constructor
 * (with no request, response associated). In this case, the resource should do
 * its best to return the generic information when the WADL description methods
 * are invoked, like {@link #getResourceInfo()} and delegate methods.
 * 
 * @author Jerome Louvel
 */
public class WadlResource extends Resource {

    /**
     * Constructor.
     */
    public WadlResource() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public WadlResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    /**
     * Indicates if OPTIONS calls are allowed by checking the "readable"
     * property.
     * 
     * @return True if the method is allowed.
     */
    @Override
    public boolean allowOptions() {
        return isReadable();
    }

    /**
     * Returns a WADL description of the given method.
     * 
     * @param method
     *            The method to describe.
     * @return A method description.
     */
    protected MethodInfo getMethodInfo(Method method) {
        MethodInfo methodInfo = new MethodInfo();
        if (isDescribable(method)) {
            methodInfo.setName(method);
            methodInfo.setRequest(getRequestInfo(method));
            methodInfo.setResponse(getResponseInfo(method));
        }
        return methodInfo;
    }

    /**
     * Returns the description of the parameters of this resource. Returns null
     * by default.
     * 
     * @return The description of the parameters.
     */
    protected List<ParameterInfo> getParametersInfo() {
        List<ParameterInfo> result = null;
        return result;
    }

    /**
     * Returns the description of the parameters of the given representation.
     * Returns null by default.
     * 
     * @param representation
     *            The parent representation.
     * @return The description of the parameters.
     */
    protected List<ParameterInfo> getParametersInfo(
            RepresentationInfo representation) {
        List<ParameterInfo> result = null;
        return result;
    }

    /**
     * Returns the description of the parameters of the given request. Returns
     * null by default.
     * 
     * @param request
     *            The parent request.
     * @return The description of the parameters.
     */
    protected List<ParameterInfo> getParametersInfo(RequestInfo request) {
        List<ParameterInfo> result = null;
        return result;
    }

    /**
     * Returns the description of the parameters of the given response. Returns
     * null by default.
     * 
     * @param response
     *            The parent response.
     * @return The description of the parameters.
     */
    protected List<ParameterInfo> getParametersInfo(ResponseInfo response) {
        List<ParameterInfo> result = null;
        return result;
    }

    /**
     * Returns the preferred WADL variant according to the client preferences
     * specified in the request.
     * 
     * @return The preferred WADL variant.
     */
    protected Variant getPreferredWadlVariant() {
        Variant result = null;

        // Compute the preferred variant. Get the default language
        // preference from the Application (if any).
        Application app = Application.getCurrent();
        Language language = null;

        if (app != null) {
            language = app.getMetadataService().getDefaultLanguage();
        }

        result = getRequest().getClientInfo().getPreferredVariant(
                getWadlVariants(), language);

        return result;
    }

    protected RepresentationInfo getRepresentationInfo(Variant variant) {
        RepresentationInfo result = new RepresentationInfo();
        result.setMediaType(variant.getMediaType());
        result.setParameters(getParametersInfo(result));
        return result;
    }

    /**
     * Returns a WADL description of the request to the given method. Returns
     * null by default.
     * 
     * @param method
     *            The method to describe.
     * @return A request description.
     */
    protected RequestInfo getRequestInfo(Method method) {
        return null;
    }

    /**
     * Returns a WADL description of the current resource.
     * 
     * @return A WADL description of the current resource.
     */
    public ResourceInfo getResourceInfo() {
        ResourceInfo result = new ResourceInfo();
        result.setPath(getResourcePath());

        // Introspect the current resource to detect the allowed methods
        List<MethodInfo> methods = result.getMethods();
        // The set of allowed methods
        List<Method> methodsList = new ArrayList<Method>();
        methodsList.addAll(getAllowedMethods());

        Collections.sort(methodsList, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });

        for (Method name : methodsList) {
            methods.add(getMethodInfo(name));
        }

        result.setParameters(getParametersInfo());
        return result;
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
     * Returns a WADL description of the response to the given method. By
     * default it will describe the available variants for the GET and the
     * OPTIONS methods based on the {@link #getVariants()} and
     * {@link #getWadlVariants()} methods.
     * 
     * @param method
     *            The method to describe.
     * @return A response description.
     */
    protected ResponseInfo getResponseInfo(Method method) {
        ResponseInfo result = null;

        if (Method.GET.equals(method)) {
            result = new ResponseInfo();

            // Describe each variant
            for (Variant variant : getVariants()) {
                result.getRepresentations().add(getRepresentationInfo(variant));
            }
        } else if (Method.OPTIONS.equals(method)) {
            result = new ResponseInfo();

            // Describe each variant
            for (Variant variant : getWadlVariants()) {
                result.getRepresentations().add(getRepresentationInfo(variant));
            }
        }

        return result;
    }

    /**
     * Returns the available WADL variants.
     * 
     * @return The available WADL variants.
     */
    protected List<Variant> getWadlVariants() {
        List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.APPLICATION_WADL_XML));
        result.add(new Variant(MediaType.TEXT_HTML));
        return result;
    }

    @Override
    public void handleOptions() {
        getResponse().setEntity(wadlRepresent());
    }

    /**
     * Indicates if the given method exposes its WADL description. By default,
     * HEAD and OPTIONS are not exposed.
     * 
     * @param method
     *            The method
     * @return True if the method exposes its description, false otherwise.
     */
    public boolean isDescribable(Method method) {
        return !(Method.HEAD.equals(method) || Method.OPTIONS.equals(method));
    }

    /**
     * Represents the resource as a WADL description.
     * 
     * @return The WADL description.
     */
    protected Representation wadlRepresent() {
        return wadlRepresent(getPreferredWadlVariant());
    }

    /**
     * Represents the resource as a WADL description for the given variant.
     * 
     * @param variant
     *            The WADL variant.
     * @return The WADL description.
     */
    protected Representation wadlRepresent(Variant variant) {
        Representation result = null;

        if (MediaType.APPLICATION_WADL_XML.equals(variant.getMediaType())) {
            result = new WadlRepresentation(getResourceInfo());
        } else if (MediaType.APPLICATION_WADL_XML
                .equals(variant.getMediaType())) {
            result = new WadlRepresentation(getResourceInfo())
                    .getHtmlRepresentation();
        }

        return result;
    }

}
