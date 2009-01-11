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

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.resource.Resource;

/**
 * Resource test case.
 * 
 * @author Kevin Conaway
 * @author Konstantin Laufer (laufer@cs.luc.edu)
 */
public class ResourceTestCase extends TestCase {

    public void testIsAvailable() {
        Resource r = new Resource();
        assertTrue(r.isAvailable());
        r.init(null, null, null);
        assertTrue(r.isAvailable());

        r = new Resource(null, null, null);
        assertTrue(r.isAvailable());
    }

    public void testIsModifiable() {
        Resource r = new Resource();
        assertFalse(r.isModifiable());
        r.setModifiable(true);
        assertTrue(r.isModifiable());
        r.init(null, null, null);
        assertTrue(r.isModifiable());

        r = new Resource(null, null, null);
        assertFalse(r.isModifiable());
    }

}
