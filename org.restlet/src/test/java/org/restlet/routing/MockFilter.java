/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;

/**
 * Thin layer around an AbstractFilter. Takes care about being started and having a target when it
 * should handle a call.
 *
 * <p>Concurrency note: instances of this class or its subclasses can be invoked by several threads
 * at the same time and therefore must be thread-safe. You should be especially careful when storing
 * state in member variables.
 *
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 */
public class MockFilter extends Filter {
    public MockFilter(Context context) {
        super(context);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (!super.isStarted()) {
            throw new IllegalStateException("Filter is not started");
        }
        if (!super.hasNext()) {
            throw new IllegalStateException("Target is not set");
        }

        return CONTINUE;
    }
}
