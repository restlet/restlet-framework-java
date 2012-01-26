/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.resource;

import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.test.RestletTestCase;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource13TestCase extends RestletTestCase {

    private ClientResource clientResource;

    private MyResource13 myResource;

    protected void setUp() throws Exception {
        super.setUp();
        Finder finder = new Finder();
        finder.setTargetClass(MyServerResource13.class);

        this.clientResource = new ClientResource("http://local");
        this.clientResource.setNext(finder);
        this.myResource = clientResource.wrap(MyResource13.class);
    }

    @Override
    protected void tearDown() throws Exception {
        clientResource = null;
        myResource = null;
        super.tearDown();
    }

    public void testQuery() {
        Contact contact = myResource.retrieve();
        assertNotNull(contact);

        LightContact lightContact = myResource.retrieveLight();
        assertFalse(lightContact.getClass().equals(Contact.class));
        assertNotNull(lightContact);

        FullContact fullContact = myResource.retrieveFull();
        assertNotNull(fullContact);
    }

}
