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
        } catch (final ClassCastException cce) {
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
