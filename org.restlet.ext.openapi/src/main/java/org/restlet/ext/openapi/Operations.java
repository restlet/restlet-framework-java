package org.restlet.ext.openapi;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class Operations {
    private Operations() {
    }

    static void addApiResponse(Operation operation, String apiResponseName, ApiResponse apiResponse) {
        if (operation.getResponses() == null) {
            operation.responses(
                new ApiResponses().addApiResponse(apiResponseName, apiResponse)
            );
        } else {
            operation.getResponses().addApiResponse(apiResponseName, apiResponse);
        }
    }
}
