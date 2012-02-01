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

package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.PathParamTestResource3;

/**
 * @author Stephan Koops
 * @see PathParamTestResource3
 * @see PathParam
 */
public class PathParamTest3 extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(PathParamTestResource3.class);
            }
        };
    }

    public void test() throws Exception {
        doTest("AAAABBBB", "eeeee", "AAAABBBB", "eeeee");
        doTest("AAA%2FBB", "eeeee", "AAA/BB", "eeeee");
        doTest("AAA%2FBB", "e%2Fe", "AAA/BB", "e/e");
        doTest("AAAABBBB", "e%2Fe", "AAAABBBB", "e/e");
    }

    /**
     * @param doctypeEncoded
     * @param entryidEncoded
     * @param doctypeDecoded
     * @param entryidDecoded
     * @throws IOException
     */
    private void doTest(String doctypeEncoded, String entryidEncoded,
            String doctypeDecoded, String entryidDecoded) throws IOException {
        Reference reference = createBaseRef();
        reference = new Reference(reference + "/supplemental/" + doctypeEncoded
                + "/" + entryidEncoded);
        Response response = get(reference);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(doctypeDecoded + "\n" + entryidDecoded + "\n", response
                .getEntity().getText());
    }
}
