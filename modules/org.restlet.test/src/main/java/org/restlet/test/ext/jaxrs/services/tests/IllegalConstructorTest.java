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

package org.restlet.test.ext.jaxrs.services.tests;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.test.ext.jaxrs.services.providers.EntityConstructorProvider;
import org.restlet.test.ext.jaxrs.services.providers.ParamConstructorProvider;
import org.restlet.test.ext.jaxrs.services.resources.IllegalConstructorResource;

/**
 * Checks, if illegal things are forbidden.
 * 
 * @author Stephan Koops
 * @see IllegalConstructorResource
 */
public class IllegalConstructorTest extends JaxRsTestCase {

    /**
     * @return
     */
    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Util.createSet(IllegalConstructorResource.class,
                        ParamConstructorProvider.class,
                        EntityConstructorProvider.class);
            }
        };
    }

    public void testNullSubResource() throws Exception {
        final Response response = get();
        assertTrue(response.getStatus().isError());
    }
}
