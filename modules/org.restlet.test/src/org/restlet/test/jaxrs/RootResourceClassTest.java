package org.restlet.test.jaxrs;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.ext.jaxrs.wrappers.RootResourceClass;
import org.restlet.test.jaxrs.services.path.IllegalPathService1;
import org.restlet.test.jaxrs.services.path.IllegalPathService2;

public class RootResourceClassTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEncodePath() {
        try {
            new RootResourceClass(IllegalPathService1.class);
            fail("must not pass");
        } catch (AssertionFailedError e) {
            // wonderful
        }
        RootResourceClass rrc = new RootResourceClass(IllegalPathService2.class);
        assertEquals("/afsdf%3Ause", rrc.getPathRegExp().getPathPattern());
    }
}
