/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.test.ext.wadl;

import org.restlet.ext.wadl.WadlApplication;
import org.restlet.ext.wadl.WadlComponent;
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the WADL extension.
 * 
 * @author Jerome Louvel
 */
public class WadlTestCase extends RestletTestCase {

    public void testWadl() throws Exception {
        WadlComponent comp = new WadlComponent(
                "clap://class/org/restlet/test/ext/wadl/YahooSearch.wadl");
        WadlApplication app = (WadlApplication) comp.getHosts().get(0)
                .getRoutes().get(0).getNext();
        assertNotNull(app);
        assertEquals(app.getInboundRoot(), app.getRouter());
        assertNotNull(app.getInboundRoot());
    }

}
