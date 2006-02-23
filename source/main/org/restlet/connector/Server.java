/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.connector;

import java.io.IOException;

import org.restlet.UniformInterface;

/**
 * Connector that listens for connections and responds to requests.<br/>By default, the handle(UniformCall)
 * method delegates the call received to the target handler<br/><br/>"The primary connector types are
 * client and server. The essential difference between the two is that a client initiates communication by
 * making a request, whereas a server listens for connections and responds to requests in order to supply
 * access to its services. A component may include both client and server connectors." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_2">Source
 * dissertation</a>
 */
public interface Server extends Connector
{
   /**
    * Handles the HTTP protocol call.<br/>
    * The default behavior is to create an UniformCall and delegate it to the attached handler.
    * @param call The HTTP protocol call.
    */
   public void handle(ServerCall call) throws IOException;

   /**
    * Returns the target handler.
    * @return The target handler.
    */
   public UniformInterface getTarget();

   /**
    * Sets the target handler.
    * @param target The target handler.
    */
   public void setTarget(UniformInterface target);

   /**
    * Configure the SSL properties for secure protocols like HTTPS.
    * @param keystorePath The path of the keystore file. 
    * @param keystorePassword The keystore password.
    * @param keyPassword The password of the server key .
    */
   public void configureSsl(String keystorePath, String keystorePassword, String keyPassword);
}
