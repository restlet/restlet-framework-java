/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.internal;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.restlet.data.Method;

public class PathItems {
    private PathItems() {}

    public static void setOperation(PathItem pathItem, Method restletMethod, Operation operation) {
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
            throw new IllegalArgumentException(
                    "Unsupported Restlet Method: " + restletMethod.getName());
        }
    }
}
