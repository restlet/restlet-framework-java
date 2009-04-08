/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.ext.spring.SpringBeanRouter;
import org.restlet.resource.Resource;
import org.restlet.routing.Route;
import org.restlet.test.RestletTestCase;
import org.restlet.util.RouteList;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class SpringBeanRouterTestCase extends RestletTestCase {
    private static final String ORE_URI = "/non-renewable/ore/{ore_type}";
    private static final String FISH_URI = "/renewable/fish/{fish_name}";

    private DefaultListableBeanFactory factory;

    private SpringBeanRouter router;

    private void assertFinderForBean(String expectedBeanName, Restlet restlet) {
        assertTrue("Restlet is not a bean finder restlet: " + restlet.getClass().getName(),
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

        registerResourceBeanDefinition("ore", ORE_URI);
        registerResourceBeanDefinition("fish", FISH_URI);

        BeanDefinition bd = new RootBeanDefinition(String.class);
        bd.setScope(BeanDefinition.SCOPE_SINGLETON);
        this.factory.registerBeanDefinition("someOtherBean", bd);

        this.router = new SpringBeanRouter();
    }

    private void registerResourceBeanDefinition(String id, String alias) {
        BeanDefinition bd = new RootBeanDefinition(Resource.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        this.factory.registerBeanDefinition(id, bd);
        if (alias != null) {
            this.factory.registerAlias(id, alias);
        }
    }

    private RouteList actualRoutes() {
        this.router.postProcessBeanFactory(this.factory);
        return this.router.getRoutes();
    }

    private Set<String> routeUris(List<Route> routes) {
        final Set<String> uris = new HashSet<String>();
        for (final Route actualRoute : routes) {
            uris.add(actualRoute.getTemplate().getPattern());
        }
        return uris;
    }

    public void testRoutesCreatedForUrlAliases() throws Exception {
        final Set<String> actualUris = routeUris(actualRoutes());
        assertEquals("Wrong number of URIs", 2, actualUris.size());
        assertTrue("Missing ore URI: " + actualUris, actualUris
                .contains(ORE_URI));
        assertTrue("Missing fish URI: " + actualUris, actualUris
                .contains(FISH_URI));
    }

    public void testRoutesCreatedForBeanIdsIfAppropriate() throws Exception {
        String grain = "/renewable/grain/{grain_type}";
        registerResourceBeanDefinition(grain, null);
        
        final Set<String> actualUris = routeUris(actualRoutes());
        assertEquals("Wrong number of URIs", 3, actualUris.size());
        assertTrue("Missing grain URI: " + actualUris, actualUris.contains(grain));
    }

    public void testRoutesPointToFindersForBeans() throws Exception {
        final RouteList actualRoutes = actualRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());
        Route oreRoute = matchRouteFor(ORE_URI);
        Route fishRoute = matchRouteFor(FISH_URI);
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

        final RouteList actualRoutes = actualRoutes();
        assertEquals("Timber resource should have been skipped", 2,
                actualRoutes.size());
    }

    public void testRoutingIncludesSpringRouterStyleExplicitlyMappedBeans() throws Exception {
        final BeanDefinition bd = new RootBeanDefinition(Resource.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        this.factory.registerBeanDefinition("timber", bd);
        this.factory.registerAlias("timber", "no-slash");

        String expectedTemplate = "/renewable/timber/{farm_type}";
        router.setAttachments(Collections.singletonMap(expectedTemplate, "timber"));
        final RouteList actualRoutes = actualRoutes();

        assertEquals("Wrong number of routes", 3, actualRoutes.size());
        Route timberRoute = matchRouteFor(expectedTemplate);
        assertNotNull("Missing timber route: " + actualRoutes, timberRoute);
        assertFinderForBean("timber", timberRoute.getNext());
    }

    public void testExplicitAttachmentsTrumpBeanNames() throws Exception {
        this.router.setAttachments(Collections.singletonMap(ORE_URI, "fish"));
        RouteList actualRoutes = actualRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());

        Route oreRoute = matchRouteFor(ORE_URI);
        assertNotNull("No route for " + ORE_URI, oreRoute);
        assertFinderForBean("fish", oreRoute.getNext());
    }

    private Route matchRouteFor(String uri) {
        Request req = new Request(Method.GET, uri);
        return (Route) router.getNext(req, new Response(req));
    }
}
