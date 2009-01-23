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
package org.restlet.test.jaxrs.services.tests;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.resources.SimpleTrain;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see SimpleTrainTest
 * @see CarTest
 */
public class MultipleResourcesTest extends JaxRsTestCase {
    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return TestUtils.createSet(SimpleTrain.class,
                        CarListResource.class);
            }
        };
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testCar() throws Exception {
        final CarTest carTest = new CarTest();
        carTest.setServerWrapper(getServerWrapper());
        carTest.testGetPlainText();
        carTest.testGetHtmlText();
        carTest.testDelete();
        carTest.testGetCar();
        carTest.testGetOffers();
    }

    public void testSimpleTrain() throws Exception {
        final SimpleTrainTest simpleTrainTest = new SimpleTrainTest();
        simpleTrainTest.setServerWrapper(getServerWrapper());
        simpleTrainTest.testGetPlainText();
        simpleTrainTest.testGetHtmlText();
        simpleTrainTest.testGetTextAll();
        simpleTrainTest.testGetTextMultiple1();
        simpleTrainTest.testGetTextMultiple2();
    }
}
