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

package org.restlet.service;

import org.restlet.Context;
import org.restlet.engine.application.Decoder;
import org.restlet.routing.Filter;

/**
 * Application service automatically decoding or uncompressing received
 * entities. This service works both for received requests entities on the
 * server-side and received response entities on the client-side.
 * 
 * @author Jerome Louvel
 */
public class DecoderService extends Service {

    /**
     * Constructor.
     */
    public DecoderService() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public DecoderService(boolean enabled) {
        super(enabled);
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new Decoder(context, true, false);
    }

    @Override
    public Filter createOutboundFilter(Context context) {
        return new Decoder(context, false, true);
    }

}
