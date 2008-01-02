/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.test.jaxrs.services.tests;

import java.util.Collection;

import org.restlet.ext.jaxrs.util.Util;
import org.restlet.test.jaxrs.services.SimpleTrain;
import org.restlet.test.jaxrs.services.car.CarListResource;

public class MultipleResourcesTest extends JaxRsTestCase {
    @Override
    protected Collection<Class<?>> createRootResourceColl() {
        return Util.createColl(SimpleTrain.class, CarListResource.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testCar() throws Exception {
        CarTest.testGetPlainText();
        CarTest.testGetHtmlText();
        CarTest.testDelete();
        CarTest.testGetCar();
        CarTest.testGetOffers();
    }

    public void testSimpleTrain() throws Exception {
        SimpleTrainTest.testGetPlainText();
        SimpleTrainTest.testGetHtmlText();
        SimpleTrainTest.testGetTextAll();
        SimpleTrainTest.testGetTextMultiple1();
        SimpleTrainTest.testGetTextMultiple2();
    }
}