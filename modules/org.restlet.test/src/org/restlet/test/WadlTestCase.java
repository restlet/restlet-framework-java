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

import junit.framework.TestCase;

import org.restlet.ext.wadl.WadlApplication;
import org.restlet.ext.wadl.WadlComponent;

/**
 * Unit test case for the WADL extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
