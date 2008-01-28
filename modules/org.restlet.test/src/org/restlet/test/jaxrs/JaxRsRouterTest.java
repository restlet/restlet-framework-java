package org.restlet.test.jaxrs;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.ext.jaxrs.AllowAllAuthenticator;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.test.jaxrs.services.DoublePath1;
import org.restlet.test.jaxrs.services.DoublePath2;
import org.restlet.test.jaxrs.services.SimpleTrain;
import org.restlet.test.jaxrs.services.path.IllegalPathService1;
import org.restlet.test.jaxrs.services.path.IllegalPathService2;

public class JaxRsRouterTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAttachDouble() throws Exception {
        JaxRsRouter router = new JaxRsRouter(null, AllowAllAuthenticator.getInstance());
        router.attach(DoublePath1.class);
        router.attach(DoublePath1.class);
    }

    public void testAttachSamePathDouble() throws Exception {
        JaxRsRouter router = new JaxRsRouter(null, AllowAllAuthenticator.getInstance());
        router.attach(DoublePath1.class);
        try {
            router.attach(DoublePath2.class);
            fail("Attach two root resource classes with the same @Path must raise an Excption");
        } catch (IllegalArgumentException e) {
            // wunderful, exception raised :-)
        }
    }

    public void testEncodePath() {
        JaxRsRouter router = new JaxRsRouter(null, AllowAllAuthenticator.getInstance());
        router.attach(SimpleTrain.class);
        try {
            router.attach(IllegalPathService1.class);
            fail("must not pass");
        } catch (AssertionFailedError e) {
            // wonderful
        }
        router.attach(IllegalPathService2.class);
    }
}