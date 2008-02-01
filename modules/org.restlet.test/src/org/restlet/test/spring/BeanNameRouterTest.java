package org.restlet.test.spring;

import junit.framework.TestCase;
import org.restlet.Route;
import org.restlet.Restlet;
import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.ext.spring.SpringBeanRouter;
import org.restlet.resource.Resource;
import org.restlet.util.RouteList;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class BeanNameRouterTest extends TestCase {
    private static final String ORE_URI = "/non-renewable/ore/{ore_type}";
    private static final String FISH_URI = "/renewable/fish/{fish_name}";

    private DefaultListableBeanFactory factory;
    private SpringBeanRouter router;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("ore", new RootBeanDefinition(Resource.class, false));
        factory.registerAlias("ore", ORE_URI);
        factory.registerBeanDefinition("fish", new RootBeanDefinition(Resource.class, false));
        factory.registerAlias("fish", FISH_URI);
        factory.registerBeanDefinition("someOtherBean", new RootBeanDefinition(String.class, true));

        router = new SpringBeanRouter();
    }

    public void testRoutesCreatedForUrlAliases() throws Exception {
        router.postProcessBeanFactory(factory);

        RouteList actualRoutes = router.getRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());
        Set<String> actualUris = new HashSet<String>();
        for (Route actualRoute : actualRoutes) {
            actualUris.add(actualRoute.getTemplate().getPattern());
        }
        assertTrue("Missing ore URI: " + actualUris, actualUris.contains(ORE_URI));
        assertTrue("Missing fish URI: " + actualUris, actualUris.contains(FISH_URI));
    }

    public void testRoutesPointToFindersForBeans() throws Exception {
        router.postProcessBeanFactory(factory);

        RouteList actualRoutes = router.getRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());
        Route oreRoute = null, fishRoute = null;
        for (Route actualRoute : actualRoutes) {
            if (actualRoute.getTemplate().getPattern().equals(FISH_URI)) fishRoute = actualRoute;
            if (actualRoute.getTemplate().getPattern().equals(ORE_URI)) oreRoute = actualRoute;
        }
        assertNotNull("ore route not present: " + actualRoutes, oreRoute);
        assertNotNull("fish route not present: " + actualRoutes, fishRoute);

        assertFinderForBean("ore", oreRoute.getNext());
        assertFinderForBean("fish", fishRoute.getNext());
    }

    public void testRoutingSkipsResourcesWithoutAppropriateAliases() throws Exception {
        factory.registerBeanDefinition("timber", new RootBeanDefinition(Resource.class, false));
        factory.registerAlias("timber", "no-slash");

        router.postProcessBeanFactory(factory);

        RouteList actualRoutes = router.getRoutes();
        assertEquals("Timber resource should have been skipped", 2, actualRoutes.size());
    }

    private void assertFinderForBean(String expectedBeanName, Restlet restlet) {
        assertTrue("Restlet is not a bean finder restlet", restlet instanceof SpringBeanFinder);
        SpringBeanFinder actualFinder = (SpringBeanFinder) restlet;
        assertEquals("Finder does not point to correct bean", expectedBeanName, actualFinder.getBeanName());
        assertEquals("Finder does not point to correct bean factory", factory, actualFinder.getBeanFactory());
    }
}
