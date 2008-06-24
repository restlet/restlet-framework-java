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
import java.util.List;

import org.restlet.Application;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * 
 * 
 * @author Jerome Louvel
 */
public class WadlResource extends Resource {

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
     * Returns a WADL description of an application containing the current
     * resource as its only resource.
     * 
     * @return An application description.
     * @see WadlResource#getResourceInfo()
     */
    public ApplicationInfo getApplicationInfo() {
        ApplicationInfo result = new ApplicationInfo();
        ResourcesInfo resources = new ResourcesInfo();
        resources.setBaseRef(getResourcesBase());
        result.setResources(resources);
        resources.getResources().add(getResourceInfo());
        return result;
    }

    /**
     * Returns a WADL description of the given method.
     * 
     * @param method
     *                The method to describe.
     * @return A method description.
     */
    protected MethodInfo getMethodInfo(Method method) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setName(method);
        return methodInfo;
    }

    /**
     * Returns the preferred WADL variant according to the client preferences
     * specified in the request.
     * 
     * @return The preferred WADL variant.
     */
    private Variant getPreferredWadlVariant() {
        Variant result = null;
        List<Variant> variants = new ArrayList<Variant>();
        variants.add(new Variant(MediaType.APPLICATION_WADL_XML));
        variants.add(new Variant(MediaType.TEXT_HTML));

        // Compute the preferred variant. Get the default language
        // preference from the Application (if any).
        Application app = Application.getCurrent();
        Language language = null;

        if (app != null) {
            language = app.getMetadataService().getDefaultLanguage();
        }

        result = getRequest().getClientInfo().getPreferredVariant(variants,
                language);

        return result;
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
        for (Method name : getAllowedMethods()) {
            methods.add(getMethodInfo(name));
        }

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
        return ref.getRelativePart();
    }

    /**
     * Returns the application resources base URI.
     * 
     * @return The application resources base URI.
     */
    protected Reference getResourcesBase() {
        return getRequest().getRootRef();
    }

    @Override
    public void handleOptions() {
        getResponse().setEntity(representWadl());
    }

    /**
     * Represents the resource as a WADL description.
     * 
     * @return The WADL description.
     */
    private Representation representWadl() {
        return representWadl(getPreferredWadlVariant());
    }

    /**
     * Represents the resource as a WADL description for the given variant.
     * 
     * @param variant
     *                The WADL variant.
     * @return The WADL description.
     */
    private Representation representWadl(Variant variant) {
        Representation result = null;

        if (MediaType.APPLICATION_WADL_XML.equals(variant.getMediaType())) {
            result = new WadlRepresentation(getApplicationInfo());
        } else if (MediaType.APPLICATION_WADL_XML
                .equals(variant.getMediaType())) {
            result = new WadlRepresentation(getApplicationInfo())
                    .getHtmlRepresentation();
        }

        return result;
    }
}
