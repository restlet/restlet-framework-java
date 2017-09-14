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

package org.restlet.ext.apispark.internal.agent.bean;

import java.util.List;

/**
 * @author Manuel Boillod
 */
public class OperationAuthorization {

    List<String> groupsAllowed;

    String method;

    /**
     * The URI path template that must match the relative part of the resource
     * URI.
     */
    String pathTemplate;

    public OperationAuthorization() {
    }

    public OperationAuthorization(String method, String pathTemplate,
            List<String> groupsAllowed) {
        this.method = method;
        this.pathTemplate = pathTemplate;
        this.groupsAllowed = groupsAllowed;
    }

    public List<String> getGroupsAllowed() {
        return groupsAllowed;
    }

    public String getMethod() {
        return method;
    }

    public String getPathTemplate() {
        return pathTemplate;
    }

    public void setGroupsAllowed(List<String> groupsAllowed) {
        this.groupsAllowed = groupsAllowed;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPathTemplate(String pathTemplate) {
        this.pathTemplate = pathTemplate;
    }
}
