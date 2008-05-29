/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.util;

import java.util.Date;
import java.util.Map;

import org.restlet.gwt.data.Reference;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;

/**
 * Resolves a name into a value.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Resolver<T> {

    /**
     * Resolves variable values based on a request and a response.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    private static class CallResolver extends Resolver<String> {
        /** The request to use as a model. */
        private Request request;

        /** The response to use as a model. */
        private Response response;

        /**
         * Constructor.
         * 
         * @param request
         *                The request to use as a model.
         * @param response
         *                The response to use as a model.
         */
        public CallResolver(Request request, Response response) {
            this.request = request;
            this.response = response;
        }

        /**
         * Returns the content corresponding to a reference property.
         * 
         * @param partName
         *                The variable sub-part name.
         * @param reference
         *                The reference to use as a model.
         * @return The content corresponding to a reference property.
         */
        private String getReferenceContent(String partName, Reference reference) {
            String result = null;

            if (reference != null) {
                if (partName.equals("a")) {
                    result = reference.getAuthority();
                } else if (partName.startsWith("b")) {
                    result = getReferenceContent(partName.substring(1),
                            reference.getBaseRef());
                } else if (partName.equals("e")) {
                    result = reference.getRelativePart();
                } else if (partName.equals("f")) {
                    result = reference.getFragment();
                } else if (partName.equals("h")) {
                    result = reference.getHostIdentifier();
                } else if (partName.equals("i")) {
                    result = reference.getIdentifier();
                } else if (partName.equals("p")) {
                    result = reference.getPath();
                } else if (partName.equals("q")) {
                    result = reference.getQuery();
                } else if (partName.equals("r")) {
                    result = reference.getRemainingPart();
                }
            }

            return result;
        }

        @Override
        public String resolve(String variableName) {
            String result = null;

            // Check for a matching request attribute
            if (request != null) {
                Object variable = request.getAttributes().get(variableName);
                if (variable != null) {
                    result = variable.toString();
                }
            }

            // Check for a matching response attribute
            if ((result == null) && (response != null)
                    && response.getAttributes().containsKey(variableName)) {
                result = response.getAttributes().get(variableName).toString();
            }

            // Check for a matching request or response property
            if (result == null) {
                if (request != null) {
                    if (variableName.equals("c")) {
                        result = Boolean.toString(request.isConfidential());
                    } else if (variableName.equals("cia")) {
                        result = request.getClientInfo().getAddress();
                    } else if (variableName.equals("cig")) {
                        result = request.getClientInfo().getAgent();
                    } else if (variableName.equals("cri")) {
                        result = request.getChallengeResponse().getIdentifier();
                    } else if (variableName.equals("crs")) {
                        if (request.getChallengeResponse().getScheme() != null) {
                            result = request.getChallengeResponse().getScheme()
                                    .getTechnicalName();
                        }
                    } else if (variableName.equals("d")) {
                        result = DateUtils.format(new Date(),
                                DateUtils.FORMAT_RFC_1123.get(0));
                    } else if (variableName.equals("ecs")) {
                        if ((request.getEntity() != null)
                                && (request.getEntity().getCharacterSet() != null)) {
                            result = request.getEntity().getCharacterSet()
                                    .getName();
                        }
                    } else if (variableName.equals("ee")) {
                        if ((request.getEntity() != null)
                                && (!request.getEntity().getEncodings()
                                        .isEmpty())) {
                            StringBuilder value = new StringBuilder();
                            for (int i = 0; i < request.getEntity()
                                    .getEncodings().size(); i++) {
                                if (i > 0)
                                    value.append(", ");
                                value.append(request.getEntity().getEncodings()
                                        .get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("eed")) {
                        if ((request.getEntity() != null)
                                && (request.getEntity().getExpirationDate() != null)) {
                            result = DateUtils.format(request.getEntity()
                                    .getExpirationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("el")) {
                        if ((request.getEntity() != null)
                                && (!request.getEntity().getLanguages()
                                        .isEmpty())) {
                            StringBuilder value = new StringBuilder();
                            for (int i = 0; i < request.getEntity()
                                    .getLanguages().size(); i++) {
                                if (i > 0)
                                    value.append(", ");
                                value.append(request.getEntity().getLanguages()
                                        .get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("emd")) {
                        if ((request.getEntity() != null)
                                && (request.getEntity().getModificationDate() != null)) {
                            result = DateUtils.format(request.getEntity()
                                    .getModificationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("emt")) {
                        if ((request.getEntity() != null)
                                && (request.getEntity().getMediaType() != null)) {
                            result = request.getEntity().getMediaType()
                                    .getName();
                        }
                    } else if (variableName.equals("es")) {
                        if ((request.getEntity() != null)
                                && (request.getEntity().getSize() != -1)) {
                            result = Long.toString(request.getEntity()
                                    .getSize());
                        }
                    } else if (variableName.equals("et")) {
                        if ((request.getEntity() != null)
                                && (request.getEntity().getTag() != null)) {
                            result = request.getEntity().getTag().getName();
                        }
                    } else if (variableName.startsWith("f")) {
                        result = getReferenceContent(variableName.substring(1),
                                request.getReferrerRef());
                    } else if (variableName.startsWith("h")) {
                        result = getReferenceContent(variableName.substring(1),
                                request.getHostRef());
                    } else if (variableName.equals("m")) {
                        if (request.getMethod() != null) {
                            result = request.getMethod().getName();
                        }
                    } else if (variableName.startsWith("o")) {
                        result = getReferenceContent(variableName.substring(1),
                                request.getRootRef());
                    } else if (variableName.equals("p")) {
                        if (request.getProtocol() != null) {
                            result = request.getProtocol().getName();
                        }
                    } else if (variableName.startsWith("r")) {
                        result = getReferenceContent(variableName.substring(1),
                                request.getResourceRef());
                    }
                }

                if ((result == null) && (response != null)) {
                    if (variableName.equals("ECS")) {
                        if ((response.getEntity() != null)
                                && (response.getEntity().getCharacterSet() != null)) {
                            result = response.getEntity().getCharacterSet()
                                    .getName();
                        }
                    } else if (variableName.equals("EE")) {
                        if ((response.getEntity() != null)
                                && (!response.getEntity().getEncodings()
                                        .isEmpty())) {
                            StringBuilder value = new StringBuilder();
                            for (int i = 0; i < response.getEntity()
                                    .getEncodings().size(); i++) {
                                if (i > 0)
                                    value.append(", ");
                                value.append(response.getEntity()
                                        .getEncodings().get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("EED")) {
                        if ((response.getEntity() != null)
                                && (response.getEntity().getExpirationDate() != null)) {
                            result = DateUtils.format(response.getEntity()
                                    .getExpirationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("EL")) {
                        if ((response.getEntity() != null)
                                && (!response.getEntity().getLanguages()
                                        .isEmpty())) {
                            StringBuilder value = new StringBuilder();
                            for (int i = 0; i < response.getEntity()
                                    .getLanguages().size(); i++) {
                                if (i > 0)
                                    value.append(", ");
                                value.append(response.getEntity()
                                        .getLanguages().get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("EMD")) {
                        if ((response.getEntity() != null)
                                && (response.getEntity().getModificationDate() != null)) {
                            result = DateUtils.format(response.getEntity()
                                    .getModificationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("EMT")) {
                        if ((response.getEntity() != null)
                                && (response.getEntity().getMediaType() != null)) {
                            result = response.getEntity().getMediaType()
                                    .getName();
                        }
                    } else if (variableName.equals("ES")) {
                        if ((response.getEntity() != null)
                                && (response.getEntity().getSize() != -1)) {
                            result = Long.toString(response.getEntity()
                                    .getSize());
                        }
                    } else if (variableName.equals("ET")) {
                        if ((response.getEntity() != null)
                                && (response.getEntity().getTag() != null)) {
                            result = response.getEntity().getTag().getName();
                        }
                    } else if (variableName.startsWith("R")) {
                        result = getReferenceContent(variableName.substring(1),
                                response.getLocationRef());
                    } else if (variableName.equals("S")) {
                        if (response.getStatus() != null) {
                            result = Integer.toString(response.getStatus()
                                    .getCode());
                        }
                    } else if (variableName.equals("SIA")) {
                        result = response.getServerInfo().getAddress();
                    } else if (variableName.equals("SIG")) {
                        result = response.getServerInfo().getAgent();
                    } else if (variableName.equals("SIP")) {
                        if (response.getServerInfo().getPort() != -1) {
                            result = Integer.toString(response.getServerInfo()
                                    .getPort());
                        }
                    }
                }
            }

            return result;
        }
    }

    /**
     * Resolves variable values based on a map.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    private static class MapResolver extends Resolver<String> {
        /** The variables to use when formatting. */
        private Map<String, Object> map;

        /**
         * Constructor.
         * 
         * @param map
         *                The variables to use when formatting.
         */
        public MapResolver(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public String resolve(String variableName) {
            Object value = this.map.get(variableName);
            return (value == null) ? null : value.toString();
        }
    }

    /**
     * Creates a resolver that is based on a given map.
     * 
     * @param map
     *                Map between names and values.
     * @return The map resolver.
     */
    public static Resolver<String> createResolver(Map<String, Object> map) {
        return new MapResolver(map);
    }

    /**
     * Creates a resolver that is based on a call (request, response couple).
     * 
     * <table>
     * <tr>
     * <th>Model property</th>
     * <th>Variable name</th>
     * <th>Content type</th>
     * </tr>
     * <tr>
     * <td>request.confidential</td>
     * <td>c</td>
     * <td>boolean (true|false)</td>
     * </tr>
     * <tr>
     * <td>request.clientInfo.address</td>
     * <td>cia</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.clientInfo.agent</td>
     * <td>cig</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.challengeResponse.identifier</td>
     * <td>cri</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.challengeResponse.scheme</td>
     * <td>crs</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.date</td>
     * <td>d</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>request.entity.characterSet</td>
     * <td>ecs</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.characterSet</td>
     * <td>ECS</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.encoding</td>
     * <td>ee</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.encoding</td>
     * <td>EE</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.expirationDate</td>
     * <td>eed</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>response.entity.expirationDate</td>
     * <td>EED</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>request.entity.language</td>
     * <td>el</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.language</td>
     * <td>EL</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.modificationDate</td>
     * <td>emd</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>response.entity.modificationDate</td>
     * <td>EMD</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>request.entity.mediaType</td>
     * <td>emt</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.mediaType</td>
     * <td>EMT</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.size</td>
     * <td>es</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>response.entity.size</td>
     * <td>ES</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>request.entity.tag</td>
     * <td>et</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.tag</td>
     * <td>ET</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.referrerRef</td>
     * <td>f*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>request.hostRef</td>
     * <td>h*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>request.method</td>
     * <td>m</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.rootRef</td>
     * <td>o*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>request.protocol</td>
     * <td>p</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.resourceRef</td>
     * <td>r*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>response.redirectRef</td>
     * <td>R*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>response.status</td>
     * <td>S</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>response.serverInfo.address</td>
     * <td>SIA</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.serverInfo.agent</td>
     * <td>SIG</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.serverInfo.port</td>
     * <td>SIP</td>
     * <td>Integer</td>
     * </tr>
     * </table> <br>
     * 
     * Below is the list of name sub-parts, for Reference variables, that can
     * replace the asterix in the variable names above:<br>
     * <br>
     * 
     * <table>
     * <tr>
     * <th>Reference property</th>
     * <th>Sub-part name</th>
     * <th>Content type</th>
     * </tr>
     * <tr>
     * <td>authority</td>
     * <td>a</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>baseRef</td>
     * <td>b*</td>
     * <td>Reference</td>
     * </tr>
     * <tr>
     * <td>relativePart</td>
     * <td>e</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>fragment</td>
     * <td>f</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>hostIdentifier</td>
     * <td>h</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>identifier</td>
     * <td>i</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>path</td>
     * <td>p</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>query</td>
     * <td>q</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>remainingPart</td>
     * <td>r</td>
     * <td>String</td>
     * </tr>
     * </table>
     * 
     * @param request
     *                The request.
     * @param response
     *                The response.
     * @return The call resolver.
     */
    public static Resolver<String> createResolver(Request request,
            Response response) {
        return new CallResolver(request, response);
    }

    /**
     * Resolves a name into a value.
     * 
     * @param name
     *                The name to resolve.
     * @return The resolved value.
     */
    public abstract T resolve(String name);

}
