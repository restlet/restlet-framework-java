/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
	 * @param enabled True if the service has been enabled.
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
