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

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ServerHelper;

/**
 * Server connector handling RIAP calls. By design, there is only one instance
 * by JVM.
 * 
 * @author Thierry Boileau
 */
public class RiapServerHelper extends ServerHelper {

    /** The unique registered helper. */
    public static RiapServerHelper instance = null;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public RiapServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.RIAP);

        // Lazy initialization with double-check.
        if (server != null && RiapServerHelper.instance == null) {
            synchronized (this.getClass()) {
                if (RiapServerHelper.instance == null) {
                    RiapServerHelper.instance = this;
                }
            }
        }
    }
}
