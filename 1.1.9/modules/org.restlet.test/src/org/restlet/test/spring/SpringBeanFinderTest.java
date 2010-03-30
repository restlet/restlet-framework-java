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

package org.restlet.test.spring;

import junit.framework.TestCase;

import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.resource.Resource;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Rhett Sutphin
 */
public class SpringBeanFinderTest extends TestCase {
    private static class SomeResource extends Resource {
    }

    private static final String BEAN_NAME = "fish";

    private SpringBeanFinder finder;

    private DefaultListableBeanFactory beanFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.beanFactory = new DefaultListableBeanFactory();
        this.finder = new SpringBeanFinder(this.beanFactory, BEAN_NAME);
    }

    public void testExceptionWhenBeanIsWrongType() throws Exception {
        this.beanFactory.registerBeanDefinition(BEAN_NAME,
                new RootBeanDefinition(String.class));

        try {
            this.finder.createResource();
            fail("Exception not thrown");
        } catch (ClassCastException cce) {
            assertEquals(
                    "fish does not resolve to an instance of org.restlet.resource.Resource",
                    cce.getMessage());
        }
    }

    public void testReturnsCorrectBeanWhenExists() throws Exception {
        this.beanFactory.registerBeanDefinition(BEAN_NAME,
                new RootBeanDefinition(SomeResource.class));

        final Resource actual = this.finder.createResource();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeResource);
    }
}
