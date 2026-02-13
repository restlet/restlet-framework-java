package org.restlet.ext.openapi;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.restlet.data.Method;

class PathItems {
    private PathItems() {
    }

    static void setOperation(PathItem pathItem, Method restletMethod, Operation operation) {
        if (restletMethod.equals(Method.POST)) {
            pathItem.setPost(operation);
        } else if (restletMethod.equals(Method.GET)) {
            pathItem.setGet(operation);
        } else if (restletMethod.equals(Method.DELETE)) {
            pathItem.setDelete(operation);
        } else if (restletMethod.equals(Method.PUT)) {
            pathItem.setPut(operation);
        } else if (restletMethod.equals(Method.PATCH)) {
            pathItem.setPatch(operation);
        } else if (restletMethod.equals(Method.OPTIONS)) {
            pathItem.setOptions(operation);
        } else {
            throw new IllegalArgumentException("Unsupported Restlet Method: " + restletMethod.getName());
        }
    }
}
