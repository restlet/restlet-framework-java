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

package org.restlet.test.resource;

import junit.framework.TestCase;

import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResourceTestCase extends TestCase {

    private ClientResource clientResource;

    private MyResource myResource;

    protected void setUp() throws Exception {
        Finder finder = new Finder();
        finder.setTargetClass(MyServerResource.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.setNext(finder);
        this.myResource = clientResource.wrap(MyResource.class);
    }

    public void testGet() {
        MyBean myBean = myResource.represent();
        assertNotNull(myBean);
        assertEquals("myName", myBean.getName());
        assertEquals("myDescription", myBean.getDescription());
    }

    public void testPut() {
        // Get current representation
        MyBean myBean = myResource.represent();
        assertNotNull(myBean);

        // Put new representation
        MyBean newBean = new MyBean("newName", "newDescription");
        String result = myResource.store(newBean);
        assertEquals("Done", result);
    }

    public void testPost() {
        MyBean myBean = new MyBean("myName", "myDescription");
        assertTrue(myResource.accept(myBean));
    }

}
