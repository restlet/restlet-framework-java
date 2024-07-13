/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.resource.ServerResource;
import org.restlet.test.RestletTestCase;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
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

    private void registerApplicationContextBean(String beanName, Class<SomeResource> resourceClass) {
        this.applicationContext.registerPrototype(beanName, resourceClass);
        this.applicationContext.refresh();
    }

    private void registerBeanFactoryBean(String beanName, Class<?> resourceClass) {
        registerBeanFactoryBean(beanName, resourceClass, null);
    }

    private void registerBeanFactoryBean(String beanName, Class<?> resourceClass, MutablePropertyValues values) {
        this.beanFactory.registerBeanDefinition(beanName,
                new RootBeanDefinition(resourceClass, new ConstructorArgumentValues(), values));
    }

    @BeforeEach
    protected void setUpEach() throws Exception {
        this.beanFactory = new DefaultListableBeanFactory();
        this.applicationContext = new StaticApplicationContext();
        this.finder = new SpringBeanFinder();
        this.finder.setBeanName(BEAN_NAME);
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
        this.beanFactory = null;
        this.applicationContext = null;
        this.finder = null;
    }

    @Test
    public void testBeanResolutionFailsWithNeitherApplicationContextOrBeanFactory() {
        try {
            this.finder.create();
            fail("Exception not thrown");
        } catch (IllegalStateException iae) {
            assertEquals(
                    "Either a beanFactory or an applicationContext is required for SpringBeanFinder.",
                    iae.getMessage());
        }
    }

    @Test
    public void testBeanResolutionFailsWhenNoMatchingBeanButThereIsABeanFactory() {
        try {
            this.finder.setBeanFactory(beanFactory);
            this.finder.create();
            fail("Exception not thrown");
        } catch (IllegalStateException iae) {
            assertEquals("No bean named " + BEAN_NAME + " present.",
                    iae.getMessage());
        }
    }

    @Test
    public void testBeanResolutionFailsWhenNoMatchingBeanButThereIsAnApplicationContext() {
        try {
            this.finder.setApplicationContext(applicationContext);
            this.finder.create();
            fail("Exception not thrown");
        } catch (IllegalStateException iae) {
            assertEquals("No bean named " + BEAN_NAME + " present.",
                    iae.getMessage());
        }
    }

    @Test
    public void testExceptionWhenResourceBeanIsWrongType() {
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

    @Test
    public void testExceptionWhenServerResourceBeanIsWrongType() {
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

    @Test
    public void testPrefersApplicationContextOverBeanFactoryIfTheBeanIsInBoth() {
        registerApplicationContextBean(BEAN_NAME, SomeResource.class);
        registerBeanFactoryBean(BEAN_NAME, AnotherResource.class);

        this.finder.setApplicationContext(applicationContext);

        ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not from application context: "
                + actual.getClass().getName(), actual instanceof SomeResource);
    }

    @Test
    public void testReturnsResourceBeanWhenExists() {
        registerBeanFactoryBean(BEAN_NAME, SomeResource.class);

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeResource);
    }

    @Test
    public void testReturnsServerResourceBeanForLongFormOfCreate() {
        registerBeanFactoryBean(BEAN_NAME, SomeServerResource.class,
                createServerResourcePropertyValues());

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create(
                SomeServerResource.class, null, null);

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeServerResource);
        assertEquals("spring",
                ((SomeServerResource) actual).getSrc(), "Resource not from spring context");
    }

    @Test
    public void testReturnsServerResourceBeanWhenExists() {
        registerBeanFactoryBean(BEAN_NAME, SomeServerResource.class,
                createServerResourcePropertyValues());

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeServerResource);
    }

    @Test
    public void testUsesApplicationContextIfPresent() {
        registerApplicationContextBean(BEAN_NAME, SomeResource.class);

        this.finder.setApplicationContext(applicationContext);

        ServerResource actual = this.finder.create();

        assertNotNull("Resource not found", actual);
        assertTrue("Resource not the correct type",
                actual instanceof SomeResource);
    }
}
