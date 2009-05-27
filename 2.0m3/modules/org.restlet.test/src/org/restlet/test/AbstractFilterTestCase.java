/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.test;

import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;

/**
 * Tests where every Filter should run through.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://www.semagia.com/">Semagia</a>
 */
public abstract class AbstractFilterTestCase extends RestletTestCase {
    /**
     * Returns a Filter to be used for the tests.
     * 
     * @return Filter instance.
     */
    protected abstract Filter getFilter();

    /**
     * Returns a request.
     * 
     * @return Request instance.
     */
    protected abstract Request getRequest();

    /**
     * Returns a response.
     * 
     * @param request
     *            The associated request.
     * @return Response instance.
     */
    protected abstract Response getResponse(Request request);

    /**
     * Returns a restlet.
     * 
     * @return Restlet instance.
     */
    protected abstract Restlet getRestlet();

    /**
     * Returns a restlet class.
     * 
     * @return Restlet class.
     */
    protected abstract Class<?> getRestletClass();

    /**
     * Test Restlet instance attaching/detaching.
     */
    public void testAttachDetachInstance() throws Exception {
        final Filter filter = getFilter();
        assertFalse(filter.hasNext());
        filter.setNext(getRestlet());
        filter.start();
        assertTrue(filter.isStarted());
        assertFalse(filter.isStopped());
        final Request request = getRequest();
        final Response response = getResponse(request);
        filter.handle(request, response);
        assertTrue(filter.hasNext());
        filter.setNext((Restlet) null);
        assertFalse(filter.hasNext());
    }

    /**
     * Test not started Filter.
     */
    public void testIllegalStartedState() throws Exception {
        final Filter filter = getFilter();
        filter.setNext(getRestlet());
        assertTrue(filter.hasNext());
        assertFalse(filter.isStarted());
        assertTrue(filter.isStopped());
        final Request request = getRequest();
        final Response response = getResponse(request);
        try {
            filter.handle(request, response);

            if (!filter.isStarted()) {
                fail("Filter handles call without being started");
            }
        } catch (Exception ex) {
            // noop.
        }
    }

    /**
     * Test with null target.
     */
    public void testIllegalTarget() throws Exception {
        final Filter filter = getFilter();
        filter.start();
        assertTrue(filter.isStarted());
        assertFalse(filter.isStopped());
        assertFalse(filter.hasNext());
        final Request request = getRequest();
        final Response response = getResponse(request);
        try {
            filter.handle(request, response);
            fail("Filter handles call without a target");
        } catch (Exception ex) {
            // noop.
        }
    }

}
