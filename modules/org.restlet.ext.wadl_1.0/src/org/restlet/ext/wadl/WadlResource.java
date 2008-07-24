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
     * Indicates if the resource should be automatically described via WADL when
     * an OPTIONS request is handled.
     */
    private volatile boolean autoDescribed;

    /**
     * Constructor.
     */
    public WadlResource() {
        this.autoDescribed = true;
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
        this.autoDescribed = true;
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
        final MethodInfo methodInfo = new MethodInfo();
        methodInfo.setName(method);
        methodInfo.setRequest(getRequestInfo(method));
        methodInfo.setResponse(getResponseInfo(method));
        return methodInfo;
    }

    /**
     * Returns the description of the parameters of this resource. Returns null
     * by default.
     * 
     * @return The description of the parameters.
     */
    protected List<ParameterInfo> getParametersInfo() {
        final List<ParameterInfo> result = null;
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
        final List<ParameterInfo> result = null;
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
        final List<ParameterInfo> result = null;
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
        final List<ParameterInfo> result = null;
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
        final Application app = Application.getCurrent();
        Language language = null;

        if (app != null) {
            language = app.getMetadataService().getDefaultLanguage();
        }

        result = getRequest().getClientInfo().getPreferredVariant(
                getWadlVariants(), language);

        return result;
    }

    protected RepresentationInfo getRepresentationInfo(Variant variant) {
        final RepresentationInfo result = new RepresentationInfo();
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
     * Returns a WADL description of the current resource, leveraging the
     * {@link #getResourcePath()} method.
     * 
     * @return A WADL description of the current resource.
     */
    private ResourceInfo getResourceInfo() {
        return getResourceInfo(getResourcePath());
    }

    /**
     * Returns a WADL description of the current resource.
     * 
     * @return A WADL description of the current resource.
     */
    public ResourceInfo getResourceInfo(String path) {
        final ResourceInfo result = new ResourceInfo();
        result.setPath(path);

        // Introspect the current resource to detect the allowed methods
        final List<Method> methodsList = new ArrayList<Method>();
        methodsList.addAll(getAllowedMethods());

        // Sort the allowed methods alphabetically
        Collections.sort(methodsList, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });

        // Update the resource info with the description
        // of the allowed methods
        final List<MethodInfo> methods = result.getMethods();
        for (final Method method : methodsList) {
            if (isDescribable(method)) {
                methods.add(getMethodInfo(method));
            }
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
        final Reference ref = new Reference(getRequest().getRootRef(),
                getRequest().getResourceRef());
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
            for (final Variant variant : getVariants()) {
                result.getRepresentations().add(getRepresentationInfo(variant));
            }
        } else if (Method.OPTIONS.equals(method)) {
            result = new ResponseInfo();

            // Describe each variant
            for (final Variant variant : getWadlVariants()) {
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
        final List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.APPLICATION_WADL_XML));
        result.add(new Variant(MediaType.TEXT_HTML));
        return result;
    }

    @Override
    public void handleOptions() {
        if (isAutoDescribed()) {
            getResponse().setEntity(wadlRepresent());
        }
    }

    /**
     * Indicates if the resource should be automatically described via WADL when
     * an OPTIONS request is handled.
     * 
     * @return True if the resource should be automatically described via WADL.
     */
    public boolean isAutoDescribed() {
        return this.autoDescribed;
    }

    /**
     * Indicates if the given method exposes its WADL description. By default,
     * HEAD and OPTIONS are not exposed. This method is called by
     * {@link #getMethodInfo(Method)}.
     * 
     * @param method
     *            The method
     * @return True if the method exposes its description, false otherwise.
     */
    public boolean isDescribable(Method method) {
        return !(Method.HEAD.equals(method) || Method.OPTIONS.equals(method));
    }

    /**
     * Indicates if the resource should be automatically described via WADL when
     * an OPTIONS request is handled.
     * 
     * @param autoDescribed
     *            True if the resource should be automatically described via
     *            WADL.
     */
    public void setAutoDescribed(boolean autoDescribed) {
        this.autoDescribed = autoDescribed;
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
