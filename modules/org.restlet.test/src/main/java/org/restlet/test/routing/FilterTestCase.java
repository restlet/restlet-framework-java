/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.routing;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.test.AbstractFilterTestCase;
import org.restlet.test.MockFilter;
import org.restlet.test.MockRestlet;

/**
 * Test {@link org.restlet.routing.Filter}.
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

}
