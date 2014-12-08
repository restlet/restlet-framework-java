/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.services.tests;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.test.ext.jaxrs.services.car.CarListResource;
import org.restlet.test.ext.jaxrs.services.resources.SimpleTrain;
import org.restlet.test.ext.jaxrs.util.TestUtils;

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
