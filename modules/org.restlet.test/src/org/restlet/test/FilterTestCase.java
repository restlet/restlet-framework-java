/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Test {@link org.restlet.Filter}.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://www.semagia.com/">Semagia</a>
 */
public class FilterTestCase extends AbstractFilterTestCase {
    @Override
    protected Filter getFilter() {
        return new MockFilter(null);
    }

    @Override
    protected Request getRequest() {
        return new Request();
    }

    @Override
    protected Response getResponse(Request request) {
        return new Response(request);
    }

    @Override
    protected Restlet getRestlet() {
        return new MockRestlet(null);
    }

    @Override
    protected Class<?> getRestletClass() {
        return MockRestlet.class;
    }

}
