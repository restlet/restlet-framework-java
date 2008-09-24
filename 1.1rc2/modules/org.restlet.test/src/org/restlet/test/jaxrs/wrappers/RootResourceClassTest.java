/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.wrappers;

import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClasses;
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
                new ThreadLocalizedContext(), null, null, Logger
                        .getAnonymousLogger());
        try {
            resourceClasses.getRootClassWrapper(IllegalPathService1.class);
            fail("must not pass");
        } catch (final AssertionFailedError e) {
            // wonderful
        }
        final RootResourceClass rrc = resourceClasses
                .getRootClassWrapper(IllegalPathService2.class);
        PathRegExp rrcRegExp = rrc.getPathRegExp();
        assertEquals("/afsdf:use", rrcRegExp.getPathTemplateDec());
        // TODO assertEquals("/afsdf%3Ause", rrcRegExp.getPathTemplateEnc());
    }
}
