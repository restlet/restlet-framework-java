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

package org.restlet.engine.riap;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ServerHelper;

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
