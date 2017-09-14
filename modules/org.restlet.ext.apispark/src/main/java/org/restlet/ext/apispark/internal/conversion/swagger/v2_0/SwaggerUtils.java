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

package org.restlet.ext.apispark.internal.conversion.swagger.v2_0;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.ext.apispark.internal.conversion.ImportUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.model.Definition;

import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.util.Json;

/**
 * Tools library for Swagger 2.0.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(SwaggerUtils.class
            .getName());

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SwaggerUtils() {
    }

    /**
     * Returns the {@link Definition} by reading the Swagger definition URL.
     * 
     * @param swaggerUrl
     *            The URl of the Swagger definition service.
     * @param userName
     *            The user name for service authentication.
     * @param password
     *            The paswword for service authentication.
     * @return A {@link Definition}.
     * @throws org.restlet.ext.apispark.internal.conversion.TranslationException
     * @throws IOException
     */
    public static Definition getDefinition(String swaggerUrl, String userName,
            String password) throws TranslationException {

        // Check that URL is non empty and well formed
        if (swaggerUrl == null) {
            throw new TranslationException("url", "You did not provide any URL");
        }

        Swagger swagger = null;
        LOGGER.log(Level.FINE, "Reading file: " + swaggerUrl);
        if (ImportUtils.isRemoteUrl(swaggerUrl)) {
            swagger = ImportUtils.getAndDeserialize(swaggerUrl, userName, password, Swagger.class);
        } else {
            File swaggerFile = new File(swaggerUrl);
            try {
                swagger = Json.mapper().readValue(swaggerFile, Swagger.class);
            } catch (Exception e) {
                throw new TranslationException("file", e.getMessage());
            }
        }
        return Swagger2Reader.translate(swagger);
    }

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
}
