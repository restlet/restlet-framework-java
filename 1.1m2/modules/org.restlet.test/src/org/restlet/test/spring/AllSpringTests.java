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

package org.restlet.test.spring;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.restlet.test.SpringTestCase;

/**
 * Suite with all Spring unit tests.
 * 
 * @author Rhett Sutphin
 */
public class AllSpringTests extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("all spring-ext tests");
        suite.addTestSuite(SpringTestCase.class);
        suite.addTestSuite(SpringBeanFinderTest.class);
        suite.addTestSuite(BeanNameRouterTest.class);
        return suite;
    }
}
