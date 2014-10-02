/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.info;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.ext.apispark.DocumentedServerResource;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Template;

/**
 * Describes a class of closely related resources.
 * 
 * @author Jerome Louvel
 */
public class ResourceInfo extends DocumentedInfo {

    /**
     * Returns a APISpark description of the current resource.
     * 
     * @param applicationInfo
     *            The parent application.
     * @param resource
     *            The resource to describe.
     * @param path
     *            Path of the current resource.
     * @param info
     *            APISpark description of the current resource to update.
     */
    public static void describe(ApplicationInfo applicationInfo,
            ResourceInfo info, Object resource, String path) {
        if ((path != null) && path.startsWith("/")) {
            path = path.substring(1);
        }

        info.setPath(path);

        // Try to extract the path variables
        if (path != null) {
            Template template = new Template(path);
            for (String variable : template.getVariableNames()) {
                ParameterInfo param = new ParameterInfo(variable,
                        ParameterStyle.TEMPLATE, (String) null);
                info.getParameters().add(param);
            }
        }

        // Introspect the current resource to detect the allowed methods
        List<Method> methodsList = new ArrayList<Method>();

        if (resource instanceof ServerResource) {
            ServerResource sr = (ServerResource) resource;
            sr.updateAllowedMethods();
            methodsList.addAll(sr.getAllowedMethods());
            if (sr instanceof DocumentedServerResource) {
                info.setDescription(((DocumentedServerResource) sr)
                        .getDescription());
                info.setName(((DocumentedServerResource) sr).getName());
            }
        } else if (resource instanceof Directory) {
            Directory directory = (Directory) resource;
            methodsList.add(Method.GET);

            if (directory.isModifiable()) {
                methodsList.add(Method.DELETE);
                methodsList.add(Method.PUT);
            }
        }

        Method.sort(methodsList);

        // Update the resource info with the description of the allowed methods
        List<MethodInfo> methods = info.getMethods();
        MethodInfo methodInfo;

        for (Method method : methodsList) {
            if (resource instanceof ServerResource) {
                ServerResource sr = (ServerResource) resource;
                List<AnnotationInfo> annotations = sr.isAnnotated() ? AnnotationUtils
                        .getInstance().getAnnotations(resource.getClass())
                        : null;
                for (AnnotationInfo annotationInfo : annotations) {
                    if (annotationInfo instanceof MethodAnnotationInfo) {
                        MethodAnnotationInfo mai = (MethodAnnotationInfo) annotationInfo;

                        if (method.equals(mai.getRestletMethod())) {
                            methodInfo = new MethodInfo();
                            methods.add(methodInfo);
                            methodInfo.setMethod(method);
                            methodInfo.setAnnotation(annotationInfo);
                            MethodInfo.describeAnnotation(methodInfo, mai, sr);
                            if (sr instanceof DocumentedServerResource) {
                                ((DocumentedServerResource) sr).describe(
                                        methodInfo, mai);
                            }
                        }
                    }
                }
            } else {
                methodInfo = new MethodInfo();
                methods.add(methodInfo);
                methodInfo.setMethod(method);
            }
        }
    }

    /** List of child resources. */
    private List<ResourceInfo> childResources;

    /** Identifier for that element. */
    private String identifier;

    /** List of supported methods. */
    private List<MethodInfo> methods;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** URI template for the identifier of the resource. */
    private String path;

    /** Media type for the query component of the resource URI. */
    private MediaType queryType;

    /** List of references to resource type elements. */
    private List<Reference> type;

    /** Authentication protocol for this resource */
    private String authenticationProtocol;

    /**
     * Constructor.
     */
    public ResourceInfo() {
        super();
    }

    public ResourceInfo(String description, String name) {
        super(description, name);
        // TODO Auto-generated constructor stub
    }

    /**
     * Creates an application descriptor that wraps this resource descriptor.
     * The title of the resource, that is to say the title of its first
     * documentation tag is transfered to the title of the first documentation
     * tag of the main application tag.
     * 
     * @return The new application descriptor.
     */
    public ApplicationInfo createApplication() {
        ApplicationInfo result = new ApplicationInfo();
        ResourcesInfo resources = new ResourcesInfo();
        result.setResources(resources);
        resources.getResources().add(this);
        return result;
    }

    /**
     * Returns the list of child resources.
     * 
     * @return The list of child resources.
     */
    public List<ResourceInfo> getChildResources() {
        // Lazy initialization with double-check.
        List<ResourceInfo> r = this.childResources;
        if (r == null) {
            synchronized (this) {
                r = this.childResources;
                if (r == null) {
                    this.childResources = r = new ArrayList<ResourceInfo>();
                }
            }
        }
        return r;
    }

    /**
     * Returns the identifier for that element.
     * 
     * @return The identifier for that element.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the list of supported methods.
     * 
     * @return The list of supported methods.
     */
    public List<MethodInfo> getMethods() {
        // Lazy initialization with double-check.
        List<MethodInfo> m = this.methods;
        if (m == null) {
            synchronized (this) {
                m = this.methods;

                if (m == null) {
                    this.methods = m = new ArrayList<MethodInfo>();
                }
            }
        }
        return m;
    }

    /**
     * Returns the list of parameters.
     * 
     * @return The list of parameters.
     */
    public List<ParameterInfo> getParameters() {
        // Lazy initialization with double-check.
        List<ParameterInfo> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null) {
                    this.parameters = p = new ArrayList<ParameterInfo>();
                }
            }
        }
        return p;
    }

    /**
     * Returns the URI template for the identifier of the resource.
     * 
     * @return The URI template for the identifier of the resource.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Returns the media type for the query component of the resource URI.
     * 
     * @return The media type for the query component of the resource URI.
     */
    public MediaType getQueryType() {
        return this.queryType;
    }

    /**
     * Returns the list of references to resource type elements.
     * 
     * @return The list of references to resource type elements.
     */
    public List<Reference> getType() {
        // Lazy initialization with double-check.
        List<Reference> t = this.type;
        if (t == null) {
            synchronized (this) {
                t = this.type;
                if (t == null) {
                    this.type = t = new ArrayList<Reference>();
                }
            }
        }
        return t;
    }
    
    /**
     * Returns the authentication protocol of the resource
     * 
     * @return The authentication protocol of the resource
     */
    public String getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    /**
     * Sets the list of child resources.
     * 
     * @param resources
     *            The list of child resources.
     */
    public void setChildResources(List<ResourceInfo> resources) {
        this.childResources = resources;
    }

    /**
     * Sets the identifier for that element.
     * 
     * @param identifier
     *            The identifier for that element.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the list of supported methods.
     * 
     * @param methods
     *            The list of supported methods.
     */
    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *            The list of parameters.
     */
    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the URI template for the identifier of the resource.
     * 
     * @param path
     *            The URI template for the identifier of the resource.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the media type for the query component of the resource URI.
     * 
     * @param queryType
     *            The media type for the query component of the resource URI.
     */
    public void setQueryType(MediaType queryType) {
        this.queryType = queryType;
    }

    /**
     * Sets the list of references to resource type elements.
     * 
     * @param type
     *            The list of references to resource type elements.
     */
    public void setType(List<Reference> type) {
        this.type = type;
    }

    /**
     * Sets the authentication protocol of the resource
     * 
     * @param authenticationProtocol
     *            The authentication protocol of the resource
     */
    public void setAuthenticationProtocol(String authenticationProtocol) {
        this.authenticationProtocol = authenticationProtocol;
    }

}
