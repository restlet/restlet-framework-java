/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.connector;

import org.restlet.client.data.Method;

/**
 * Protocol helper for the WEBDAV protocol.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed in next version.
 */
@Deprecated
public class WebDavProtocolHelper extends ProtocolHelper {

    @Override
    public void registerMethods() {
        Method.register(Method.COPY);
        Method.register(Method.LOCK);
        Method.register(Method.MKCOL);
        Method.register(Method.MOVE);
        Method.register(Method.PROPFIND);
        Method.register(Method.PROPPATCH);
        Method.register(Method.UNLOCK);
    }

}
