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
    private static final String BEAN_NAME = "fish";

    private SpringBeanFinder finder;
    private DefaultListableBeanFactory beanFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        beanFactory = new DefaultListableBeanFactory();
        finder = new SpringBeanFinder(beanFactory, BEAN_NAME);
    }

    public void testReturnsCorrectBeanWhenExists() throws Exception {
        beanFactory.registerBeanDefinition(BEAN_NAME, new RootBeanDefinition(SomeResource.class));

        Resource actual = finder.createResource();

        assertNotNull("Resource not found",  actual);
        assertTrue("Resource not the correct type", actual instanceof SomeResource);
    }

    public void testExceptionWhenBeanIsWrongType() throws Exception {
        beanFactory.registerBeanDefinition(BEAN_NAME, new RootBeanDefinition(String.class));

        try {
            finder.createResource();
            fail("Exception not thrown");
        } catch (ClassCastException cce) {
            assertEquals("fish does not resolve to an instance of org.restlet.resource.Resource", cce.getMessage());
        }
    }

    private static class SomeResource extends Resource { }
}
