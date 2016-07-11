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

package org.restlet.engine.local;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.engine.connector.ClientHelper;

/**
 * Connector to the local resources accessible via file system, class loaders
 * and similar mechanisms. Here is the list of parameters that are supported.
 * They should be set in the Client's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>timeToLive</td>
 * <td>int</td>
 * <td>600</td>
 * <td>Time to live for a representation before it expires (in seconds). If you
 * set the value to '0', the representation will never expire.</td>
 * </tr>
 * <tr>
 * <td>defaultLanguage</td>
 * <td>String</td>
 * <td></td>
 * <td>When no metadata service is available (simple client connector with no
 * parent application), falls back on this default language. To indicate that no
 * default language should be set, "" can be used.</td>
 * </tr>
 * </table>
 * 
 * @see org.restlet.data.LocalReference
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public abstract class LocalClientHelper extends ClientHelper {
    /**
     * Constructor. Note that the common list of metadata associations based on
     * extensions is added, see the addCommonExtensions() method.
     * 
     * @param client
     *            The client to help.
     */
    public LocalClientHelper(Client client) {
        super(client);
    }

    /**
     * Returns the default language. When no metadata service is available
     * (simple client connector with no parent application), falls back on this
     * default language.
     * 
     * @return The default language.
     */
    public String getDefaultLanguage() {
        return getHelpedParameters().getFirstValue("defaultLanguage", "");
    }

    /**
     * Returns the time to live for a file representation before it expires (in
     * seconds).
     * 
     * @return The time to live for a file representation before it expires (in
     *         seconds).
     */
    public int getTimeToLive() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "timeToLive", "600"));
    }

    /**
     * Handles a call. Note that this implementation will systematically
     * normalize and URI-decode the resource reference.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public final void handle(Request request, Response response) {
        // Ensure that all ".." and "." are normalized into the path
        // to prevent unauthorized access to user directories.
        request.getResourceRef().normalize();

        // As the path may be percent-encoded, it has to be percent-decoded.
        // Then, all generated URIs must be encoded.
        String path = request.getResourceRef().getPath();
        String decodedPath = Reference.decode(path);

        if (decodedPath != null) {
            // Continue the local handling
            handleLocal(request, response, decodedPath);
        } else {
            getLogger().warning(
                    "Unable to get the path of this local URI: "
                            + request.getResourceRef());
        }
    }

    /**
     * Handles a local call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param decodedPath
     *            The decoded local path.
     */
    protected abstract void handleLocal(Request request, Response response,
            String decodedPath);
}
