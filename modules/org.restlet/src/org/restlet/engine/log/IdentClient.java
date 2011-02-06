/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.util.StringUtils;

/**
 * Simple IDENT client. Follow the RFC 1413.
 * 
 * @author Jerome Louvel
 */
public class IdentClient {
    /** The timeout while attempting to connect to the Ident server. */
    private static final int CONNECT_TIMEOUT = 100;

    /** The timeout while communicating with the Ident server. */
    private static final int SO_TIMEOUT = 500;

    /** The remote host type. */
    private volatile String hostType;

    /** The user identifier. */
    private volatile String userIdentifier;

    /**
     * Constructor.
     * 
     * @param clientAddress
     *            The client IP address.
     * @param clientPort
     *            The client port (remote).
     * @param serverPort
     *            The server port (local).
     */
    public IdentClient(String clientAddress, int clientPort, int serverPort) {
        Socket socket = null;

        if ((clientAddress != null) && (clientPort != -1) && (serverPort != -1)) {
            BufferedReader in = null;
            try {
                // Compose the IDENT request
                final StringBuilder sb = new StringBuilder();
                sb.append(clientPort).append(" , ").append(serverPort).append(
                        "\r\n");
                final String request = sb.toString();

                // Send the request to the remote server
                socket = new Socket();
                socket.setSoTimeout(SO_TIMEOUT);
                socket.connect(new InetSocketAddress(clientAddress, 113),
                        CONNECT_TIMEOUT);
                socket.getOutputStream().write(
                        StringUtils.getAsciiBytes(request));

                // Read the response
                in = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()), IoUtils.getBufferSize());
                final String response = in.readLine();

                // Parse the response
                if (response != null) {
                    final StringTokenizer st = new StringTokenizer(response,
                            ":");

                    if (st.countTokens() >= 3) {
                        // Skip the first token
                        st.nextToken();

                        // Get the command
                        final String command = st.nextToken().trim();
                        if (command.equalsIgnoreCase("USERID")
                                && (st.countTokens() >= 2)) {
                            // Get the host type
                            this.hostType = st.nextToken().trim();

                            // Get the remaining text as a user identifier
                            this.userIdentifier = st.nextToken("").substring(1);
                        }
                    }
                }
            } catch (IOException ioe) {
                Context.getCurrentLogger().log(Level.FINE,
                        "Unable to complete the IDENT request", ioe);
            } finally {
                try {
                    // Always attempt to close the reader, therefore the socket
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(Level.FINE,
                            "Unable to close the socket", ioe);
                }
            }
        }
    }

    /**
     * Returns the remote host type.
     * 
     * @return The remote host type.
     */
    public String getHostType() {
        return this.hostType;
    }

    /**
     * Returns the user identifier.
     * 
     * @return The user identifier.
     */
    public String getUserIdentifier() {
        return this.userIdentifier;
    }

}
