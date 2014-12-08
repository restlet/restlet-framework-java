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

package org.restlet.ext.apispark.internal.conversion.swagger.v1_2;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ApiDeclaration;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListing;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model.ResourceListingApi;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tools library for Swagger.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(SwaggerUtils.class
            .getName());

    private static ClientResource createAuthenticatedClientResource(String url,
            String userName, String password) {
        ClientResource cr = new ClientResource(url);
        cr.accept(MediaType.APPLICATION_JSON);
        if (!StringUtils.isNullOrEmpty(userName)
                && !StringUtils.isNullOrEmpty(password)) {
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, userName,
                    password);
        }
        return cr;
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
     */
    public static Definition getDefinition(String swaggerUrl, String userName,
            String password) throws TranslationException {

        // Check that URL is non empty and well formed
        if (swaggerUrl == null) {
            throw new TranslationException("url", "You did not provide any URL");
        }
        Pattern p = Pattern
                .compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        boolean remote = p.matcher(swaggerUrl).matches();
        ResourceListing resourceListing;
        Map<String, ApiDeclaration> apis = new HashMap<String, ApiDeclaration>();
        if (remote) {
            LOGGER.log(Level.FINE, "Reading file: " + swaggerUrl);
            resourceListing = createAuthenticatedClientResource(swaggerUrl,
                    userName, password).get(ResourceListing.class);
            for (ResourceListingApi api : resourceListing.getApis()) {
                LOGGER.log(Level.FINE,
                        "Reading file: " + swaggerUrl + api.getPath());
                apis.put(
                        api.getPath(),
                        createAuthenticatedClientResource(
                                swaggerUrl + api.getPath(), userName, password)
                                .get(ApiDeclaration.class));
            }
        } else {
            File resourceListingFile = new File(swaggerUrl);
            ObjectMapper om = new ObjectMapper();
            try {
                resourceListing = om.readValue(resourceListingFile,
                        ResourceListing.class);
                String basePath = resourceListingFile.getParent();
                LOGGER.log(Level.FINE, "Base path: " + basePath);
                for (ResourceListingApi api : resourceListing.getApis()) {
                    LOGGER.log(Level.FINE,
                            "Reading file " + basePath + api.getPath());
                    apis.put(api.getPath(), om.readValue(new File(basePath
                            + api.getPath()), ApiDeclaration.class));
                }
            } catch (IOException e) {
                throw new TranslationException("file", e.getMessage());
            }
        }
        return SwaggerTranslator.translate(resourceListing, apis);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SwaggerUtils() {
    }
}