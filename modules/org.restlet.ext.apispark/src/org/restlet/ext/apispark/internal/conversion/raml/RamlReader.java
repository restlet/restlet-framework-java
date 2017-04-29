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

package org.restlet.ext.apispark.internal.conversion.raml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.model.Protocol;
import org.raml.model.Raml;
import org.raml.model.SecurityScheme;
import org.restlet.data.ChallengeScheme;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;

import com.google.common.collect.Iterables;

/**
 * Tools library for generating Restlet Web API Definitions from RAML definitions.
 * 
 * @author Cyprien Quilici
 */
public class RamlReader {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(RamlReader.class.getName());

    /**
     * Translates a RAML definition to a Restlet Web API Definition
     * 
     * @param raml
     *            The RAML definition to translate.
     * 
     * @return The Restlet Web API definition
     */
    public static Definition translate(Raml raml) {
        // conversion
        Definition definition = new Definition();

        // fill RWADef main attributes
        fillEndpoints(raml, definition);

        List<String> defaultProduces = new ArrayList<>();
        List<String> defaultConsumes = new ArrayList<>();
        fillGeneralInformation(raml, definition, defaultProduces, defaultConsumes);

        // TODO: fill definition.sections
        // Contract contract = definition.getContract();
        // fillSections(raml, contract);

        // TODO: Get declared parameters
        // Map<String, Object> parameters = new LinkedHashMap<>();
        // fillDeclaredParameters(raml, definition, parameters);

        // TODO: fill definition.representations
        // fillRepresentations(raml, contract);

        // TODO: fill definition.resources
        // fillResources(raml, contract, produces, consumes, parameters);
        //
        // for (Representation representation : contract.getRepresentations()) {
        // representation.addSectionsToProperties(contract);
        // }

        return definition;
    }

    private static void fillGeneralInformation(Raml raml, Definition definition,
            List<String> defaultProduces, List<String> defaultConsumes) {
        definition.setVersion(raml.getVersion());

        Contract contract = definition.getContract();
        contract.setName(raml.getTitle());

        String defaultMediaType = raml.getMediaType();
        if (!StringUtils.isNullOrEmpty(defaultMediaType)) {
                defaultProduces.add(defaultMediaType);
                defaultConsumes.add(defaultMediaType);
        }
    }

    private static void fillEndpoints(Raml raml, Definition definition) {
        String baseUri = raml.getBaseUri();
        if (StringUtils.isNullOrEmpty(baseUri)) {
            return;
        }
        
        Pattern pattern = Pattern.compile("(http|https|HTTP|HTTPS)(://.*)");
        Matcher matcher = pattern.matcher(baseUri);
        if (!matcher.matches()) {
            LOGGER.warning("Invalid base URI, no endpoints added.");
            return;
        }

        List<Map<String, SecurityScheme>> securitySchemes = raml.getSecuritySchemes();
        Set<ChallengeScheme> challengeSchemes = new HashSet<>();
        for (Map<String, SecurityScheme> map : securitySchemes) {
            for (Entry<String, SecurityScheme> entry : map.entrySet()) {
                SecurityScheme securityScheme = entry.getValue();

                if (securityScheme == null) {
                    continue;
                }

                fillChallengeScheme(challengeSchemes, securityScheme.getType());
            }
        }

        String authenticationProtocol = null;
        if (challengeSchemes.size() > 0) {
            
            authenticationProtocol = Iterables.get(challengeSchemes, 0).getName();
            
            if (challengeSchemes.size() > 1) {
                LOGGER.warning("Multiple security schemes found, only one per API is supported, kept first one: "
                    + authenticationProtocol);
            }
        }

        List<Protocol> protocols = raml.getProtocols();

        if (protocols.isEmpty()) {
            Endpoint endpoint = new Endpoint(baseUri);
            endpoint.setAuthenticationProtocol(authenticationProtocol);
            definition.getEndpoints().add(endpoint);

            return;
        }
        
        String basePath = matcher.group(2);
        for (Protocol protocol : protocols) {
            String url = protocol.toString().toLowerCase() + basePath;
            if (Protocol.HTTP == protocol || Protocol.HTTPS == protocol) {
                Endpoint endpoint = new Endpoint(url);
                endpoint.setAuthenticationProtocol(authenticationProtocol);
                definition.getEndpoints().add(endpoint);
            } else {
                LOGGER.warning("Unsupported protocol " + protocol + " no endpoint created.");
            }
        }
    }

    private static void fillChallengeScheme(Set<ChallengeScheme> challengeSchemes, String securitySchemeType) {
        if (RamlUtils.SecurityScheme.OAUTH_1.test(securitySchemeType)) {
            LOGGER.warning("Oauth 1.0 not supported yet.");
            return;
            
        } else if (RamlUtils.SecurityScheme.OAUTH_2.test(securitySchemeType)) {
            challengeSchemes.add(ChallengeScheme.HTTP_OAUTH);
            return;
            
        } else if (RamlUtils.SecurityScheme.BASIC.test(securitySchemeType)) {
            challengeSchemes.add(ChallengeScheme.HTTP_BASIC);
            return;

        } else if (RamlUtils.SecurityScheme.DIGEST.test(securitySchemeType)) {
            challengeSchemes.add(ChallengeScheme.HTTP_DIGEST);
            return;

        } else if (RamlUtils.SecurityScheme.CUSTOM.test(securitySchemeType)) {
            challengeSchemes.add(ChallengeScheme.CUSTOM);
            return;

        } else {
            LOGGER.warning("Unsupported security scheme type: " + securitySchemeType);
        }
    }
}
