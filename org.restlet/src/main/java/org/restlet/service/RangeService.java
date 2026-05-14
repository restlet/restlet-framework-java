/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import org.restlet.Context;
import org.restlet.engine.application.RangeFilter;
import org.restlet.routing.Filter;

/**
 * Application service automatically exposes ranges of response entities. This allows resources to
 * not care for requested ranges and return full representations that will then be transparently
 * wrapped in partial representations by this service, allowing the client to benefit from partial
 * downloads.
 *
 * @author Jerome Louvel
 */
public class RangeService extends Service {

    /** Constructor. */
    public RangeService() {
        super();
    }

    /**
     * Constructor.
     *
     * @param enabled True if the service has been enabled.
     */
    public RangeService(boolean enabled) {
        super(enabled);
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new RangeFilter(context);
    }
}
