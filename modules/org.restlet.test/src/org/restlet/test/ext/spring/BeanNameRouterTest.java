/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.ext.spring;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.ext.spring.SpringBeanRouter;
import org.restlet.resource.Resource;
import org.restlet.util.RouteList;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Rhett Sutphin
 */
public class BeanNameRouterTest extends TestCase {
    private static final String ORE_URI = "/non-renewable/ore/{ore_type}";

    private static final String FISH_URI = "/renewable/fish/{fish_name}";

    private DefaultListableBeanFactory factory;

    private SpringBeanRouter router;

    private void assertFinderForBean(String expectedBeanName, Restlet restlet) {
        assertTrue("Restlet is not a bean finder restlet",
                restlet instanceof SpringBeanFinder);
        final SpringBeanFinder actualFinder = (SpringBeanFinder) restlet;
        assertEquals("Finder does not point to correct bean", expectedBeanName,
                actualFinder.getBeanName());
        assertEquals("Finder does not point to correct bean factory",
                this.factory, actualFinder.getBeanFactory());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.factory = new DefaultListableBeanFactory();

        BeanDefinition bd = new RootBeanDefinition(Resource.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        this.factory.registerBeanDefinition("ore", bd);
        this.factory.registerAlias("ore", ORE_URI);

        bd = new RootBeanDefinition(Resource.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        this.factory.registerBeanDefinition("fish", bd);
        this.factory.registerAlias("fish", FISH_URI);

        bd = new RootBeanDefinition(String.class);
        bd.setScope(BeanDefinition.SCOPE_SINGLETON);
        this.factory.registerBeanDefinition("someOtherBean", bd);

        this.router = new SpringBeanRouter();
    }

    public void testRoutesCreatedForUrlAliases() throws Exception {
        this.router.postProcessBeanFactory(this.factory);

        final RouteList actualRoutes = this.router.getRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());
        final Set<String> actualUris = new HashSet<String>();
        for (final Route actualRoute : actualRoutes) {
            actualUris.add(actualRoute.getTemplate().getPattern());
        }
        assertTrue("Missing ore URI: " + actualUris, actualUris
                .contains(ORE_URI));
        assertTrue("Missing fish URI: " + actualUris, actualUris
                .contains(FISH_URI));
    }

    public void testRoutesPointToFindersForBeans() throws Exception {
        this.router.postProcessBeanFactory(this.factory);

        final RouteList actualRoutes = this.router.getRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());
        Route oreRoute = null, fishRoute = null;
        for (final Route actualRoute : actualRoutes) {
            if (actualRoute.getTemplate().getPattern().equals(FISH_URI)) {
                fishRoute = actualRoute;
            }
            if (actualRoute.getTemplate().getPattern().equals(ORE_URI)) {
                oreRoute = actualRoute;
            }
        }
        assertNotNull("ore route not present: " + actualRoutes, oreRoute);
        assertNotNull("fish route not present: " + actualRoutes, fishRoute);

        assertFinderForBean("ore", oreRoute.getNext());
        assertFinderForBean("fish", fishRoute.getNext());
    }

    public void testRoutingSkipsResourcesWithoutAppropriateAliases()
            throws Exception {
        final BeanDefinition bd = new RootBeanDefinition(Resource.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        this.factory.registerBeanDefinition("timber", bd);
        this.factory.registerAlias("timber", "no-slash");

        this.router.postProcessBeanFactory(this.factory);

        final RouteList actualRoutes = this.router.getRoutes();
        assertEquals("Timber resource should have been skipped", 2,
                actualRoutes.size());
    }
}
