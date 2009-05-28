package org.restlet.engine.component;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ServerHelper;

/**
 * Server connector handling RIAP calls. By design, there is only one instance
 * by JVM.
 * 
 * @author Thierry Boileau
 * 
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
