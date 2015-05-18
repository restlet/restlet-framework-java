package org.restlet.ext.apispark.internal.conversion.swagger.v2_0;

import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.Parameter;

/**
 * Tools library for Swagger 2.0.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerUtils {

    /**
     * Returns the input payload of the given operation if there is one. Null otherwise.
     * 
     * @param operation
     *            The Swagger operation.
     * @return The input payload.
     */
    public static BodyParameter getInputPayload(Operation operation) {
        for (Parameter parameter : operation.getParameters()) {
            if (parameter instanceof BodyParameter) {
                return (BodyParameter) parameter;
            }
        }
        return null;
    }

    /**
     * Safe toString method, returns null if the object is null, calls its toString otherwise.
     * Avoids NPEs.
     * 
     * @param object
     *            The object on which to perform a toString.
     * @return
     *         The String representation of the object.
     */
    public static String toString(Object object) {
        return object == null ? null : object.toString();
    }

    /**
     * Returns the separator for multi-valued parameters given the collection format.
     * 
     * @param collectionFormat
     *            The Swagger's collection format, {@see
     *            https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#fixed-fields-7}
     * @return The separator character
     */
    public static Character getSeparator(String collectionFormat) {
        if (collectionFormat == null) {
            return null;
        }

        switch (collectionFormat) {
        case "csv":
            return ',';
        case "ssv":
            return ' ';
        case "tsv":
            return '\t';
        case "pipes":
            return '|';
        default:
            return null;
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SwaggerUtils() {
    }
}
