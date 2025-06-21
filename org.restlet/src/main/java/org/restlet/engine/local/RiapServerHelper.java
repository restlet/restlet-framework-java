/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
	 * @param server The server to help.
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
