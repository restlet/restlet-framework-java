/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.routing;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

/**
 * Thin layer around an AbstractRestlet. Takes care about being started when it
 * should handle a call.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://www.semagia.com/">Semagia</a>
 */
public class MockRestlet extends Restlet {
    public MockRestlet(Context context) {
        super(context);
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (!super.isStarted()) {
            throw new IllegalStateException("Restlet was not started");
        }
    }
}
