/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.ext.wadl;

import junit.framework.TestCase;

import org.restlet.ext.wadl.WadlApplication;
import org.restlet.ext.wadl.WadlComponent;

/**
 * Unit test case for the WADL extension.
 * 
 * @author Jerome Louvel
 */
public class WadlTestCase extends TestCase {

    public void testWadl() throws Exception {
        final WadlComponent comp = new WadlComponent(
                "clap://class/org/restlet/test/YahooSearch.wadl");

        final WadlApplication app = (WadlApplication) comp.getHosts().get(0)
                .getRoutes().get(0).getNext();
        assertNotNull(app);
        assertEquals(app.getRoot(), app.getRouter());
        assertNotNull(app.getRoot());
    }

}
