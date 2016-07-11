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

package org.restlet.engine.connector;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.local.Entity;
import org.restlet.engine.util.SystemUtils;
import org.restlet.representation.InputRepresentation;

/**
 * FTP client connector using the {@link URLConnection}. Here is the list of
 * parameters that are supported. They should be set in the Client's context
 * before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>allowUserInteraction</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, this URL is being examined in a context in which it makes sense
 * to allow user interactions such as popping up an authentication dialog.</td>
 * </tr>
 * <tr>
 * <td>readTimeout</td>
 * <td>int</td>
 * <td>60000</td>
 * <td>Sets the read timeout to a specified timeout, in milliseconds. A timeout
 * of zero is interpreted as an infinite timeout.</td>
 * </tr>
 * <tr>
 * <td>useCaches</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the protocol is allowed to use caching whenever it can.</td>
 * </tr>
 * </table>
 * 
 * @see <a
 *      href="http://download.oracle.com/javase/1.5.0/docs/guide/net/index.html">Networking
 *      Features</a>
 * @author Jerome Louvel
 */
public class FtpClientHelper extends ClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public FtpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.FTP);
    }

    /**
     * Returns the read timeout value. A timeout of zero is interpreted as an
     * infinite timeout.
     * 
     * @return The read timeout value.
     */
    public int getReadTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "readTimeout", "60000"));
    }

    /**
     * Returns the connection timeout. Defaults to 15000.
     * 
     * @return The connection timeout.
     */
    public int getSocketConnectTimeoutMs() {
        int result = 0;

        if (getHelpedParameters().getNames().contains("socketConnectTimeoutMs")) {
            result = Integer.parseInt(getHelpedParameters().getFirstValue(
                    "socketConnectTimeoutMs", "15000"));
        }

        return result;
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            if (Protocol.FTP.equals(request.getProtocol())) {
                if (Method.GET.equals(request.getMethod())) {
                    Reference ftpRef = request.getResourceRef();
                    String userInfo = null;

                    if ((request.getChallengeResponse() != null)
                            && ChallengeScheme.FTP_PLAIN.equals(request
                                    .getChallengeResponse().getScheme())
                            && (request.getChallengeResponse().getIdentifier() != null)) {
                        userInfo = request.getChallengeResponse()
                                .getIdentifier();

                        if (request.getChallengeResponse().getSecret() != null) {
                            userInfo += ":"
                                    + new String(request.getChallengeResponse()
                                            .getSecret());
                        }
                    }

                    if (userInfo != null) {
                        ftpRef.setUserInfo(userInfo);
                    }

                    URL url = ftpRef.toUrl();
                    URLConnection connection = url.openConnection();

                    // These properties can only be used with Java 1.5 and upper
                    // releases
                    int majorVersionNumber = SystemUtils.getJavaMajorVersion();
                    int minorVersionNumber = SystemUtils.getJavaMinorVersion();
                    if ((majorVersionNumber > 1)
                            || ((majorVersionNumber == 1) && (minorVersionNumber >= 5))) {
                        connection
                                .setConnectTimeout(getSocketConnectTimeoutMs());
                        connection.setReadTimeout(getReadTimeout());
                    }

                    connection
                            .setAllowUserInteraction(isAllowUserInteraction());
                    connection.setUseCaches(isUseCaches());
                    response.setEntity(new InputRepresentation(connection
                            .getInputStream()));

                    // Try to infer the metadata from the file extensions
                    Entity.updateMetadata(request.getResourceRef().getPath(),
                            response.getEntity(), true, getMetadataService());
                } else {
                    getLogger()
                            .log(Level.WARNING,
                                    "Only GET method are supported by this FTP connector");
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "FTP client error", e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e.getMessage());
        }
    }

    /**
     * Indicates if this URL is being examined in a context in which it makes
     * sense to allow user interactions such as popping up an authentication
     * dialog.
     * 
     * @return True if it makes sense to allow user interactions.
     */
    public boolean isAllowUserInteraction() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "allowUserInteraction", "false"));
    }

    /**
     * Indicates if the protocol is allowed to use caching whenever it can.
     * 
     * @return True if the protocol is allowed to use caching whenever it can.
     */
    public boolean isUseCaches() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "useCaches", "false"));
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info("Starting the internal FTP client");
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the internal FTP client");
    }

}
