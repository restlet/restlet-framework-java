package org.restlet.ext.apispark.internal.agent.bean;

import java.util.List;

/**
 * @author Manuel Boillod
 */
public class OperationAuthorization {

    String method;

    /**
     *  The URI path template that must match the relative part of the
     *  resource URI.
     */
    String pathTemplate;

    List<String> groupsAllowed;

    public OperationAuthorization() {
    }

    public OperationAuthorization(String method, String pathTemplate, List<String> groupsAllowed) {
        this.method = method;
        this.pathTemplate = pathTemplate;
        this.groupsAllowed = groupsAllowed;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPathTemplate() {
        return pathTemplate;
    }

    public void setPathTemplate(String pathTemplate) {
        this.pathTemplate = pathTemplate;
    }

    public List<String> getGroupsAllowed() {
        return groupsAllowed;
    }

    public void setGroupsAllowed(List<String> groupsAllowed) {
        this.groupsAllowed = groupsAllowed;
    }
}
