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
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public class Operations {
    private Operations() {}

    public static void addApiResponse(
            Operation operation, String apiResponseName, ApiResponse apiResponse) {
        if (operation.getResponses() == null) {
            operation.responses(new ApiResponses().addApiResponse(apiResponseName, apiResponse));
        } else {
            operation.getResponses().addApiResponse(apiResponseName, apiResponse);
        }
    }
}
