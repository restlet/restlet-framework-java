/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.test.jaxrs.wrappers;

import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperFactory;
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
        WrapperFactory wrapperFactory = new WrapperFactory(
                new ThreadLocalizedContext(), null, null, Logger
                        .getAnonymousLogger());
        try {
            wrapperFactory.getRootResourceClass(IllegalPathService1.class);
            fail("must not pass");
        } catch (AssertionFailedError e) {
            // wonderful
        }
        RootResourceClass rrc = wrapperFactory
                .getRootResourceClass(IllegalPathService2.class);
        assertEquals("/afsdf%3Ause", rrc.getPathRegExp().getPathPattern());
    }
}
