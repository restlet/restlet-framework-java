/*
 * Copyright 2005-2007 Noelios Consulting.
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

import org.restlet.Application;
import org.restlet.Router;
import org.restlet.ext.wadl.WadlComponent;
import org.restlet.resource.Representation;

/**
 * Unit test case for the WADL extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class WadlTestCase extends TestCase {

    public void testWadl() throws Exception {
        WadlComponent comp = new WadlComponent();
        Representation wadl = comp.getContext().getDispatcher().get(
                "clap://class/org/restlet/test/YahooSearch.wadl").getEntity();
        comp.attach(wadl);

        Application app = (Application) comp.getHosts().get(0).getRoutes().get(
                0).getNext();
        assertNotNull(app);

        Router root = (Router) app.getRoot();
        assertNotNull(root);
    }

}
