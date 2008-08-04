/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
 * @version $Rev:$ - $Date:$
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
