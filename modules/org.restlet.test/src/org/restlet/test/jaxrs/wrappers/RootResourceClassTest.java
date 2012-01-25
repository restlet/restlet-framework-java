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

package org.restlet.test.jaxrs.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.engine.Engine;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClasses;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.test.jaxrs.services.path.IllegalPathService1;
import org.restlet.test.jaxrs.services.path.IllegalPathService2;

/**
 * @author Stephan Koops
 * @see RootResourceClass
 */
@SuppressWarnings("all")
public class RootResourceClassTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEncodePath() throws Exception {
        final ResourceClasses resourceClasses = new ResourceClasses(
                new ThreadLocalizedContext(), null, null, Engine
                        .getAnonymousLogger());
        try {
            getPerRequestRootClassWrapper(resourceClasses,
                    IllegalPathService1.class);
            fail("must not pass");
        } catch (AssertionFailedError e) {
            // wonderful
        }
        final RootResourceClass rrc = getPerRequestRootClassWrapper(
                resourceClasses, IllegalPathService2.class);
        PathRegExp rrcRegExp = rrc.getPathRegExp();
        assertEquals("/afsdf:use", rrcRegExp.getPathTemplateDec());
    }

    static RootResourceClass getPerRequestRootClassWrapper(
            ResourceClasses resourceClasses, Class<?> jaxRsRootResourceClass)
            throws Exception {
        Method method = ResourceClasses.class.getDeclaredMethod(
                "getPerRequestRootClassWrapper", Class.class);
        method.setAccessible(true);
        try {
            return (RootResourceClass) method.invoke(resourceClasses,
                    jaxRsRootResourceClass);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception)
                throw (Exception) cause;
            if (cause instanceof Error)
                throw (Error) cause;

            throw new RuntimeException(cause);
        }
    }
}
