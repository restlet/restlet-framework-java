/*
 * Copyright 2005-2007 Noelios Technologies.
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

package com.noelios.restlet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple IDENT client. Follow the RFC 1413.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class IdentClient {
    /** The timeout while attempting to connect to the Ident server. */
    private static final int CONNECT_TIMEOUT = 100;

    /** The timeout while communicating with the Ident server. */
    private static final int SO_TIMEOUT = 500;

    /** The remote host type. */
    private String hostType;

    /** The user identifier. */
    private String userIdentifier;

    /**
     * Constructor.
     * 
     * @param clientAddress
     *                The client IP address.
     * @param clientPort
     *                The client port (remote).
     * @param serverPort
     *                The server port (local).
     */
    public IdentClient(Logger logger, String clientAddress, int clientPort,
            int serverPort) {
        Socket socket = null;

        if ((logger != null) && (clientAddress != null) && (clientPort != -1)
                && (serverPort != -1)) {
            BufferedReader in = null;
            try {
                // Compose the IDENT request
                StringBuilder sb = new StringBuilder();
                sb.append(clientPort).append(" , ").append(serverPort).append(
                        "\r\n");
                String request = sb.toString();

                // Send the request to the remote server
                socket = new Socket();
                socket.setSoTimeout(SO_TIMEOUT);
                socket.connect(new InetSocketAddress(clientAddress, 113),
                        CONNECT_TIMEOUT);
                socket.getOutputStream().write(request.getBytes());

                // Read the response
                in = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));
                String response = in.readLine();

                // Parse the response
                if (response != null) {
                    StringTokenizer st = new StringTokenizer(response, ":");

                    if (st.countTokens() >= 3) {
                        // Skip the first token
                        st.nextToken();

                        // Get the command
                        String command = st.nextToken().trim();
                        if (command.equalsIgnoreCase("USERID")
                                && st.countTokens() >= 2) {
                            // Get the host type
                            this.hostType = st.nextToken().trim();

                            // Get the remaining text as a user identifier
                            this.userIdentifier = st.nextToken("").substring(1);
                        }
                    }
                }
            } catch (IOException ioe) {
                logger.log(Level.FINE, "Unable to complete the IDENT request",
                        ioe);
            } finally {
                try {
                    // Always attempt to close the reader, therefore the socket
                    if (in != null)
                        in.close();
                } catch (IOException ioe) {
                    logger.log(Level.FINE, "Unable to close the socket", ioe);
                }
                ;
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
