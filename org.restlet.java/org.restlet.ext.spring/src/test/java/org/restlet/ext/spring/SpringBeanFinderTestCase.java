/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.resource.ServerResource;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Rhett Sutphin
 */
public class SpringBeanFinderTestCase {

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
        return new MutablePropertyValues(List.of(new PropertyValue("src","spring")));
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
    protected void setUpEach() {
        this.beanFactory = new DefaultListableBeanFactory();
        this.applicationContext = new StaticApplicationContext();
        this.finder = new SpringBeanFinder();
        this.finder.setBeanName(BEAN_NAME);
    }

    @AfterEach
    protected void tearDownEach() {
        this.beanFactory = null;
        this.applicationContext = null;
        this.finder = null;
    }

    @Test
    public void testBeanResolutionFailsWithNeitherApplicationContextOrBeanFactory() {
        IllegalStateException iae = assertThrows(IllegalStateException.class, () -> this.finder.create());
        assertEquals("Either a beanFactory or an applicationContext is required for SpringBeanFinder.", iae.getMessage());
    }

    @Test
    public void testBeanResolutionFailsWhenNoMatchingBeanButThereIsABeanFactory() {
        this.finder.setBeanFactory(beanFactory);

        IllegalStateException iae = assertThrows(IllegalStateException.class, () -> this.finder.create());
        assertEquals("No bean named " + BEAN_NAME + " present.", iae.getMessage());
    }

    @Test
    public void testBeanResolutionFailsWhenNoMatchingBeanButThereIsAnApplicationContext() {
        this.finder.setApplicationContext(applicationContext);
        IllegalStateException iae = assertThrows(IllegalStateException.class, () -> this.finder.create());
        assertEquals("No bean named " + BEAN_NAME + " present.", iae.getMessage());
    }

    @Test
    public void testExceptionWhenResourceBeanIsWrongType() {
        registerBeanFactoryBean(BEAN_NAME, String.class);

        this.finder.setBeanFactory(beanFactory);

        ClassCastException classCastException = assertThrows(ClassCastException.class, () -> this.finder.create());
        assertEquals(
                "fish does not resolve to an instance of org.restlet.resource.ServerResource",
                classCastException.getMessage());
    }

    @Test
    public void testExceptionWhenServerResourceBeanIsWrongType() {
        registerBeanFactoryBean(BEAN_NAME, String.class);

        this.finder.setBeanFactory(beanFactory);

        ClassCastException classCastException = assertThrows(ClassCastException.class, () -> this.finder.create());
        assertEquals(
                "fish does not resolve to an instance of org.restlet.resource.ServerResource",
                classCastException.getMessage());
    }

    @Test
    public void testPrefersApplicationContextOverBeanFactoryIfTheBeanIsInBoth() {
        registerApplicationContextBean(BEAN_NAME, SomeResource.class);
        registerBeanFactoryBean(BEAN_NAME, AnotherResource.class);

        this.finder.setApplicationContext(applicationContext);

        ServerResource actual = this.finder.create();

        assertTrue(actual instanceof SomeResource, "Resource not from application context: " + actual.getClass().getName());
    }

    @Test
    public void testReturnsResourceBeanWhenExists() {
        registerBeanFactoryBean(BEAN_NAME, SomeResource.class);

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create();

        assertTrue(actual instanceof SomeResource, "Resource not the correct type");
    }

    @Test
    public void testReturnsServerResourceBeanForLongFormOfCreate() {
        registerBeanFactoryBean(BEAN_NAME, SomeServerResource.class,
                createServerResourcePropertyValues());

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create(
                SomeServerResource.class, null, null);

        assertTrue(actual instanceof SomeServerResource, "Resource not the correct type");
        assertEquals("spring", ((SomeServerResource) actual).getSrc(), "Resource not from spring context");
    }

    @Test
    public void testReturnsServerResourceBeanWhenExists() {
        registerBeanFactoryBean(BEAN_NAME, SomeServerResource.class,
                createServerResourcePropertyValues());

        this.finder.setBeanFactory(beanFactory);

        final ServerResource actual = this.finder.create();

        assertTrue(actual instanceof SomeServerResource, "Resource not the correct type");
    }

    @Test
    public void testUsesApplicationContextIfPresent() {
        registerApplicationContextBean(BEAN_NAME, SomeResource.class);

        this.finder.setApplicationContext(applicationContext);

        ServerResource actual = this.finder.create();

        assertTrue(actual instanceof SomeResource, "Resource not the correct type");
    }
}
