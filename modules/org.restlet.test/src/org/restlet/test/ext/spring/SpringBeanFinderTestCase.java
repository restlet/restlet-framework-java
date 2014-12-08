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

package org.restlet.test.ext.spring;

import java.util.Arrays;

import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.resource.ServerResource;
import org.restlet.test.RestletTestCase;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class SpringBeanFinderTestCase extends RestletTestCase {

    private static class AnotherResource extends ServerResource {
    }

    private static class SomeResource extends ServerResource {
    }

    private static class SomeServerResource extends ServerResource {
        private String src;

        @SuppressWarnings("unused")
        public SomeServerResource() {
            setSrc("constructor");
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }
    }

    private static final String BEAN_NAME = "fish";

    private StaticApplicationContext applicationContext;

    private DefaultListableBeanFactory beanFactory;

    private SpringBeanFinder finder;

    private MutablePropertyValues createServerResourcePropertyValues() {
        return new MutablePropertyValues(Arrays.asList(new PropertyValue("src",
                "spring")));
    }

    private void registerApplicationContextBean(String beanName,
            Class<SomeResource> resourceClass) {
        this.applicationContext.registerPrototype(beanName, resourceClass);
        this.applicationContext.refresh();
    }

    private void registerBeanFactoryBean(String beanName, Class<?> resourceClass) {
        registerBeanFactoryBean(beanName, resourceClass, null);
    }

    @SuppressWarnings("deprecation")
    private void registerBeanFactoryBean(String beanName,
            Class<?> resourceClass, MutablePropertyValues values) {
        this.beanFactory.registerBeanDefinition(beanName,
                new RootBeanDefinition(resourceClass, values));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.beanFactory = new DefaultListableBeanFactory();
        this.applicationContext = new StaticApplicationContext();
        this.finder = new SpringBeanFinder();
        this.finder.setBeanName(BEAN_NAME);
    }

    @Override
    protected void tearDown() throws Exception {
        this.beanFactory = null;
        this.applicationContext = null;
        this.finder = null;
        super.tearDown();
    }

    public void testBeanResolutionFailsWithNeitherApplicationContextOrBeanFactory()
            throws Exception {
        try {
            this.finder.create();
            fail("Exception not thrown");
        } catch (IllegalStateException iae) {
            assertEquals(
                    "Either a beanFactory or an applicationContext is required for SpringBeanFinder.",
                    iae.getMessage());
        }
    }

    public void testBeanResolutionFailsWhenNoMatchingBeanButThereIsABeanFactory()
            throws Exception {
        try {
            this.finder.setBeanFactory(beanFactory);
            this.finder.create();
            fail("Exception not thrown");
        } catch (IllegalStateException iae) {
            assertEquals("No bean named " + BEAN_NAME + " present.",
                    iae.getMessage());
        }
    }

    public void testBeanResolutionFailsWhenNoMatchingBeanButThereIsAnApplicationContext()
            throws Exception {
        try {
            this.finder.setApplicationContext(applicationContext);
            this.finder.create();
            fail("Exception not thrown");
        } catch (IllegalStateException iae) {
            assertEquals("No bean named " + BEAN_NAME + " present.",
                    iae.getMessage());
        }
    }

    public void testExceptionWhenResourceBeanIsWrongType() throws Exception {
        registerBeanFactoryBean(BEAN_NAME, String.class);

        this.finder.setBeanFactory(beanFactory);

        try {
            this.finder.create();
            fail("Exception not thrown");
        } catch (ClassCastException cce) {
            assertEquals(
                    "fish does not resolve to an instance of org.restlet.resource.ServerResource",
                    cce.getMessage());
        }
    }

    public void testExceptionWhenServerResourceBeanIsWrongType()
            throws Exception {
        registerBeanFactoryBean(BEAN_NAME, String.class);

        this.finder.setBeanFactory(beanFactory);

        try {
            this.finder.create();
            fail("Exception not thrown");
        } catch (ClassCastException cce) {
            assertEquals(
                    "fish does not resolve to an instance of org.restlet.resource.ServerResource",
                    cce.getMessage());
        }
    }

    public void testPrefersApplicationContextOverBeanFactoryIfTheBeanIsInBoth()
            throws Exception {
        registerApplicationContextBean(BEAN_NAME, SomeResource.class);
        registerBeanFactoryBean(BEAN_NAME, AnotherResource.class);

        this.finder.setApplicationContext(applicationContext);

        ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not from application context: "
                + actual.getClass().getName(), actual instanceof SomeResource);
    }

    public void testReturnsResourceBeanWhenExists() throws Exception {
        registerBeanFactoryBean(BEAN_NAME, SomeResource.class);

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeResource);
    }

    public void testReturnsServerResourceBeanForLongFormOfCreate()
            throws Exception {
        registerBeanFactoryBean(BEAN_NAME, SomeServerResource.class,
                createServerResourcePropertyValues());

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create(
                SomeServerResource.class, null, null);

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeServerResource);
        assertEquals("Resource not from spring context", "spring",
                ((SomeServerResource) actual).getSrc());
    }

    public void testReturnsServerResourceBeanWhenExists() throws Exception {
        registerBeanFactoryBean(BEAN_NAME, SomeServerResource.class,
                createServerResourcePropertyValues());

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeServerResource);
    }

    public void testUsesApplicationContextIfPresent() throws Exception {
        registerApplicationContextBean(BEAN_NAME, SomeResource.class);

        this.finder.setApplicationContext(applicationContext);

        ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeResource);
    }
}
