/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
 * description can be customized by overriding the {@link #describe()} and
 * {@link #describeMethod(Method, MethodInfo)} methods.
 * 
 * When used to describe a class of resources in the context of a parent
 * application, a special instance will be created using the default constructor
 * (with no request, response associated). In this case, the resource should do
 * its best to return the generic information when the WADL description methods
 * are invoked, like {@link #describe()} and delegate methods.
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
     * The title of this documented resource. Is seen as the title of the first
     * "doc" tag of the "application" tag in a WADL document or as the title of
     * the HTML representation.
     */
    private volatile String title;

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
     * Describes the resource as a WADL document.
     * 
     * @return The WADL description.
     */
    protected Representation describe() {
        return describe(getPreferredWadlVariant());
    }

    /**
     * Returns a WADL description of the current resource, leveraging the
     * {@link #getResourcePath()} method.
     * 
     * @param info
     *            WADL description of the current resource to update.
     */
    private void describe(ResourceInfo info) {
        describe(getResourcePath(), info);
    }

    /**
     * Returns a WADL description of the current resource.
     * 
     * @param path
     *            Path of the current resource.
     * @param info
     *            WADL description of the current resource to update.
     */
    public void describe(String path, ResourceInfo info) {
        info.setPath(path);

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
        final List<MethodInfo> methods = info.getMethods();
        MethodInfo methodInfo;
        for (final Method method : methodsList) {
            if (isDescribable(method)) {
                methodInfo = new MethodInfo();
                describeMethod(method, methodInfo);
                methods.add(methodInfo);
            }
        }

        info.setParameters(getParametersInfo());
    }

    /**
     * Describes the resource as a WADL document for the given variant.
     * 
     * @param variant
     *            The WADL variant.
     * @return The WADL description.
     */
    protected Representation describe(Variant variant) {
        Representation result = null;

        if (variant != null) {
            ResourceInfo resourceInfo = new ResourceInfo();
            describe(resourceInfo);

            if (getTitle() != null && !"".equals(getTitle())) {
                DocumentationInfo doc = null;
                if (resourceInfo.getDocumentations().isEmpty()) {
                    doc = new DocumentationInfo();
                    resourceInfo.getDocumentations().add(doc);
                } else {
                    doc = resourceInfo.getDocumentations().get(0);
                }

                doc.setTitle(getTitle());
            }

            if (MediaType.APPLICATION_WADL.equals(variant.getMediaType())) {
                result = new WadlRepresentation(resourceInfo);
            } else if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
                result = new WadlRepresentation(resourceInfo)
                        .getHtmlRepresentation();
            }
        }

        return result;
    }

    /**
     * Describes the DELETE method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeDelete(MethodInfo info) {
    }

    /**
     * Describes the GET method.<br>
     * By default, it describes the response with the available variants based
     * on the {@link #getVariants()} method. Thus in the majority of cases, the
     * method of the super class must be called when overriden.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeGet(MethodInfo info) {
        // Describe each variant
        for (final Variant variant : getVariants()) {
            info.addResponseRepresentation(variant);
        }
    }

    /**
     * Returns a WADL description of the given method.
     * 
     * @param method
     *            The method to describe.
     * @param info
     *            The method description to update.
     */
    protected void describeMethod(Method method, MethodInfo info) {
        info.setName(method);
        info.setRequest(new RequestInfo());
        info.setResponse(new ResponseInfo());

        if (Method.GET.equals(method)) {
            describeGet(info);
        } else if (Method.POST.equals(method)) {
            describePost(info);
        } else if (Method.PUT.equals(method)) {
            describePut(info);
        } else if (Method.DELETE.equals(method)) {
            describeDelete(info);
        } else if (Method.OPTIONS.equals(method)) {
            describeOptions(info);
        }
    }

    /**
     * Describes the OPTIONS method.<br>
     * By default it describes the response with the available variants based on
     * the {@link #getWadlVariants()} method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describeOptions(MethodInfo info) {
        // Describe each variant
        for (final Variant variant : getWadlVariants()) {
            info.addResponseRepresentation(variant);
        }
    }

    /**
     * Describes the POST method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePost(MethodInfo info) {
    }

    /**
     * Describes the PUT method.
     * 
     * @param info
     *            The method description to update.
     */
    protected void describePut(MethodInfo info) {
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
     * Returns the title of this documented resource.
     * 
     * @return The title of this documented resource.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the available WADL variants.
     * 
     * @return The available WADL variants.
     */
    protected List<Variant> getWadlVariants() {
        final List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.APPLICATION_WADL));
        result.add(new Variant(MediaType.TEXT_HTML));
        return result;
    }

    @Override
    public void handleOptions() {
        if (isAutoDescribed()) {
            getResponse().setEntity(describe());
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
     * {@link #describe(String, ResourceInfo)}.
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
     * Sets the title of this documented resource.
     * 
     * @param title
     *            The title of this documented resource.
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
