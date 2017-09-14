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

package org.restlet.ext.apispark.internal.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Tools library.
 * 
 * @author Thierry Boileau
 */
public class IntrospectionUtils {

    public static List<String> STRATEGIES = Arrays.asList("update", "replace");

    /**
     * Discover introspection helpers.
     *
     * @return the discovered introspection helpers.
     */
    public static List<IntrospectionHelper> getIntrospectionHelpers() {
        List<IntrospectionHelper> introspectionHelpers = new ArrayList<>();

        ServiceLoader<IntrospectionHelper> ihLoader = ServiceLoader
                .load(IntrospectionHelper.class);
        for (IntrospectionHelper helper : ihLoader) {
            introspectionHelpers.add(helper);
        }
        return introspectionHelpers;
    }
    /**
     * Indicates if the given value is either null or empty.
     * 
     * @param value
     *            The value.
     * @return True if the value is either null or empty.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static void sendDefinition(Definition definition,
                                      String ulogin, String upwd, String serviceUrl,
                                      String cellType, String cellId, String cellVersion,
                                      boolean createNewCell, boolean createNewVersion,
                                      boolean updateCell, String updateStrategy,
                                      Logger logger) {

        String url = serviceUrl + "api/";

        sortDefinition(definition);

        JacksonRepresentation<Definition> definitionRepresentation = new JacksonRepresentation<>(definition);

        ClientResource cr = new ClientResource(url);
        try {
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, ulogin, upwd);
            cr.addQueryParameter("type", "rwadef");

            if (createNewCell) {
                cr.addQueryParameter("cellType", cellType);
                cr.addSegment("apis").addSegment("");
                logger.info("Create a new cell of type " + cellType);
                cr.post(definitionRepresentation, MediaType.APPLICATION_JSON);
            } else if (createNewVersion) {
                cr.addSegment("apis").addSegment(cellId)
                        .addSegment("versions").addSegment("");
                logger.info("Create a new version of the cell "
                        + cellId);
                cr.post(definitionRepresentation, MediaType.APPLICATION_JSON);
            } else if (updateCell) {
                cr.addSegment("apis").addSegment(cellId)
                        .addSegment("versions").addSegment(cellVersion);
                logger.info("Update version " + cellVersion + " of cell "
                        + cellId + " with strategy " + updateStrategy);
                cr.addQueryParameter("strategy", updateStrategy);
                cr.put(definitionRepresentation, MediaType.APPLICATION_JSON);
            } else {
                throw new RuntimeException("No action error");
            }

            logger.fine("Call success to "+ cr.getRequest());

            if (!cr.getResponse().getStatus().isSuccess()) {
                throw new RuntimeException("Request failed with following status: " + cr.getResponse().getStatus());
            }
            // This is not printed by a logger which may be muted.
            if (cr.getResponseEntity() != null
                    && cr.getResponseEntity().isAvailable()) {
                try {
                    cr.getResponseEntity().write(System.out);
                    System.out.println();
                } catch (IOException e) {
                    // [PENDING] analysis
                    logger.log(Level.WARNING, "Request successfully achieved by the server, but it's response cannot be printed", e);
                }
            }

            if (cr.getLocationRef() != null) {
                System.out
                        .println("Your Web API documentation is accessible at this URL: "
                                + cr.getLocationRef());
            }
        } catch (ResourceException e) {
            logger.fine("Error during call to "+ cr.getRequest());
            if (e.getStatus().isConnectorError()) {
                throw new RuntimeException("APISpark communication error. Please check the root cause below.", e);
            } else if (e.getStatus().isClientError()) {
                if (e.getStatus().getCode() == 403) {
                    throw new RuntimeException("APISpark Authentication fails. Check that you provide valid credentials.", e);
                } else if (e.getStatus().getCode() == 404) {
                    throw new RuntimeException("Resource not found. Check that you provide valid cell id and cell version.", e);
                } else if (e.getStatus().getCode() == 422) {
                    System.out.println("Invalid Request Payload. Response payload: \n" + cr.getResponse().getEntityAsText());
                    throw new RuntimeException("Invalid Request Payload.", e);
                } else {
                    throw new RuntimeException("APISpark returns client error. Please check the root cause below.", e);
                }
            } else {
                throw new RuntimeException("APISpark server encounters some issues, please try later", e);
            }
        }
    }

    /**
     * Sorts the sections, representations and resources alphabetically in the
     * given RWADef definition
     * 
     * @param definition
     *            The RWADef definition
     */
    public static void sortDefinition(Definition definition) {
        Collections.sort(definition.getContract().getSections(),
                new Comparator<Section>() {

                    @Override
                    public int compare(Section o1, Section o2) {
                        return o1.getName().compareTo(o2.getName());
                    }

                });
        Collections.sort(definition.getContract().getRepresentations(),
                new Comparator<Representation>() {

                    @Override
                    public int compare(Representation o1, Representation o2) {
                        return o1.getName().compareTo(o2.getName());
                    }

                });
        Collections.sort(definition.getContract().getResources(),
                new Comparator<Resource>() {

                    @Override
                    public int compare(Resource o1, Resource o2) {
                        return o1.getResourcePath().compareTo(
                                o2.getResourcePath());
                    }

                });
    }

    public static void updateRepresentationsSectionsFromResources(
            Definition definition) {
        Map<Resource, Collection<String>> resourcesLinks = new HashMap<Resource, Collection<String>>();
        Map<Representation, Collection<String>> representationsSections = new HashMap<Representation, Collection<String>>();
        for (Resource resource : definition.getContract().getResources()) {
            Collection<String> representations = new HashSet<String>();
            for (Operation operation : resource.getOperations()) {
                if (operation.getInputPayLoad() != null
                        && operation.getInputPayLoad().getType() != null) {
                    representations.add(operation.getInputPayLoad().getType());
                }
                for (Response response : operation.getResponses()) {
                    if (response.getOutputPayLoad() != null
                            && response.getOutputPayLoad().getType() != null) {
                        representations.add(response.getOutputPayLoad()
                                .getType());
                    }
                }
            }
            resourcesLinks.put(resource, representations);
        }

        for (Entry<Resource, Collection<String>> entry : resourcesLinks
                .entrySet()) {
            for (String representationIdentifier : entry.getValue()) {
                Representation representation = definition.getContract()
                        .getRepresentation(representationIdentifier);

                // primitives types are not present in representations list
                if (representation != null) {
                    if (representationsSections.get(representation) != null) {
                        representationsSections.get(representation).addAll(
                                entry.getKey().getSections());
                    } else {
                        Collection<String> representationSections = new HashSet<String>();
                        representationSections.addAll(representation
                                .getSections());
                        representationSections.addAll(entry.getKey()
                                .getSections());
                        representationsSections.put(representation,
                                representationSections);
                    }
                }
            }
        }

        for (Entry<Representation, Collection<String>> entry : representationsSections
                .entrySet()) {
            entry.getKey().setSections(new ArrayList<String>(entry.getValue()));
        }
    }
}
