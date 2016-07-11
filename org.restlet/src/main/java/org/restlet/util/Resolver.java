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

package org.restlet.util;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.util.CallResolver;
import org.restlet.engine.util.MapResolver;

/**
 * Resolves a name into a value. By default, the {@link #createResolver(Map)}
 * static method can adapt a Java map into a resolver. Another useful method is
 * {@link #createResolver(Request, Response)}, which can expose a Restlet call
 * into a compact data model, with the following variables:
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
 * <td>request.clientInfo.upstreamAddress</td>
 * <td>ciua</td>
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
 * </table>
 * <br>
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
 * <td>targetRef</td>
 * <td>t*</td>
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
 * @author Jerome Louvel
 */
public abstract class Resolver<T> {

    /**
     * Creates a resolver that is based on a given map.
     * 
     * @param map
     *            Map between names and values.
     * @return The map resolver.
     */
    public static Resolver<?> createResolver(Map<String, ?> map) {
        return new MapResolver(map);
    }

    /**
     * Creates a resolver that is based on a call (request, response couple). It
     * first looks up the response attributes, then the request attributes and
     * finally the variables listed in this class Javadocs above.
     * 
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @return The call resolver.
     */
    public static Resolver<?> createResolver(Request request, Response response) {
        return new CallResolver(request, response);
    }

    /**
     * Resolves a name into a value.
     * 
     * @param name
     *            The name to resolve.
     * @return The resolved value.
     */
    public abstract T resolve(String name);

}
