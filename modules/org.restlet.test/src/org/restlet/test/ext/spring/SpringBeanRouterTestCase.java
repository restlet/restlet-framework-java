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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.ext.spring.SpringBeanFinder;
import org.restlet.ext.spring.SpringBeanRouter;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.test.RestletTestCase;
import org.restlet.util.Resolver;
import org.restlet.util.RouteList;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Rhett Sutphin
 */
public class SpringBeanRouterTestCase extends RestletTestCase {

    private static class TestAuthenticator extends ChallengeAuthenticator {
        private TestAuthenticator() throws IllegalArgumentException {
            super(null, ChallengeScheme.HTTP_BASIC, "Test");
        }
    }

    private static class TestFilter extends Filter {
    }

    private static class TestResource extends ServerResource {
    }

    private static class TestRestlet extends Restlet {
    }

    private static final String FISH_URI = "/renewable/fish/{fish_name}";

    private static final String ORE_URI = "/non-renewable/ore/{ore_type}";

    private DefaultListableBeanFactory factory;

    private SpringBeanRouter router;

    private RouteList actualRoutes() {
        doPostProcess();
        return this.router.getRoutes();
    }

    private void assertFinderForBean(String expectedBeanName, Restlet restlet) {
        assertTrue("Restlet is not a bean finder restlet: "
                + restlet.getClass().getName(),
                restlet instanceof SpringBeanFinder);
        final SpringBeanFinder actualFinder = (SpringBeanFinder) restlet;
        assertEquals("Finder does not point to correct bean", expectedBeanName,
                actualFinder.getBeanName());
        assertEquals("Finder does not point to correct bean factory",
                this.factory, actualFinder.getBeanFactory());
    }

    private void doPostProcess() {
        this.router.postProcessBeanFactory(this.factory);
    }

    private TemplateRoute matchRouteFor(String uri) {
        Request req = new Request(Method.GET,
                new Template(uri).format(new Resolver<String>() {
                    @Override
                    public String resolve(String name) {
                        return name;
                    }
                }));
        return (TemplateRoute) router.getNext(req, new Response(req));
    }

    private void registerBeanDefinition(String id, String alias,
            Class<?> beanClass, String scope) {
        BeanDefinition bd = new RootBeanDefinition(beanClass);
        bd.setScope(scope == null ? BeanDefinition.SCOPE_SINGLETON : scope);
        this.factory.registerBeanDefinition(id, bd);

        if (alias != null) {
            this.factory.registerAlias(id, alias);
        }
    }

    private void registerServerResourceBeanDefinition(String id, String alias) {
        registerBeanDefinition(id, alias, ServerResource.class,
                BeanDefinition.SCOPE_PROTOTYPE);
    }

    private Set<String> routeUris(RouteList routes) {
        Set<String> uris = new HashSet<String>();

        for (Route actualRoute : routes) {
            if (actualRoute instanceof TemplateRoute) {
                uris.add(((TemplateRoute) actualRoute).getTemplate()
                        .getPattern());
            }
        }

        return uris;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.factory = new DefaultListableBeanFactory();
        registerServerResourceBeanDefinition("ore", ORE_URI);
        registerServerResourceBeanDefinition("fish", FISH_URI);
        registerBeanDefinition("someOtherBean", null, String.class, null);

        this.router = new SpringBeanRouter();
    }

    @Override
    protected void tearDown() throws Exception {
        this.factory = null;
        this.router = null;
        super.tearDown();
    }

    public void testExplicitAttachmentsMayBeRestlets() throws Exception {
        String expected = "/protected/timber";
        this.router
                .setAttachments(Collections.singletonMap(expected, "timber"));
        registerBeanDefinition("timber", null, TestAuthenticator.class, null);

        doPostProcess();
        TemplateRoute timberRoute = matchRouteFor(expected);
        assertNotNull("No route for " + expected, timberRoute);
        assertTrue("Route is not for correct restlet",
                timberRoute.getNext() instanceof TestAuthenticator);
    }

    public void testExplicitAttachmentsTrumpBeanNames() throws Exception {
        this.router.setAttachments(Collections.singletonMap(ORE_URI, "fish"));
        RouteList actualRoutes = actualRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());

        TemplateRoute oreRoute = matchRouteFor(ORE_URI);
        assertNotNull("No route for " + ORE_URI, oreRoute);
        assertFinderForBean("fish", oreRoute.getNext());
    }

    public void testExplicitRoutingForNonResourceNonRestletBeansFails()
            throws Exception {
        this.router.setAttachments(Collections.singletonMap("/fail",
                "someOtherBean"));
        try {
            doPostProcess();
            fail("Exception not thrown");
        } catch (IllegalStateException ise) {
            assertEquals(
                    "someOtherBean is not routable.  It must be either a Resource, a ServerResource or a Restlet.",
                    ise.getMessage());
        }
    }

    public void testRoutesCreatedForBeanIdsIfAppropriate() throws Exception {
        String grain = "/renewable/grain/{grain_type}";
        registerServerResourceBeanDefinition(grain, null);

        final Set<String> actualUris = routeUris(actualRoutes());
        assertEquals("Wrong number of URIs", 3, actualUris.size());
        assertTrue("Missing grain URI: " + actualUris,
                actualUris.contains(grain));
    }

    public void testRoutesCreatedForUrlAliases() throws Exception {
        final Set<String> actualUris = routeUris(actualRoutes());
        assertEquals("Wrong number of URIs", 2, actualUris.size());
        assertTrue("Missing ore URI: " + actualUris,
                actualUris.contains(ORE_URI));
        assertTrue("Missing fish URI: " + actualUris,
                actualUris.contains(FISH_URI));
    }

    public void testRoutesPointToFindersForBeans() throws Exception {
        final RouteList actualRoutes = actualRoutes();
        assertEquals("Wrong number of routes", 2, actualRoutes.size());
        TemplateRoute oreRoute = matchRouteFor(ORE_URI);
        TemplateRoute fishRoute = matchRouteFor(FISH_URI);
        assertNotNull("ore route not present: " + actualRoutes, oreRoute);
        assertNotNull("fish route not present: " + actualRoutes, fishRoute);

        assertFinderForBean("ore", oreRoute.getNext());
        assertFinderForBean("fish", fishRoute.getNext());
    }

    public void testRoutingIncludesAuthenticators() throws Exception {
        String expected = "/protected/timber";
        registerBeanDefinition("timber", expected, TestAuthenticator.class,
                null);
        doPostProcess();

        TemplateRoute authenticatorRoute = matchRouteFor(expected);
        assertNotNull("No route for authenticator", authenticatorRoute);
        assertTrue("Route is not for authenticator",
                authenticatorRoute.getNext() instanceof TestAuthenticator);
    }

    public void testRoutingIncludesFilters() throws Exception {
        String expected = "/filtered/timber";
        registerBeanDefinition("timber", expected, TestFilter.class, null);
        doPostProcess();

        TemplateRoute filterRoute = matchRouteFor(expected);
        assertNotNull("No route for filter", filterRoute);
        assertTrue("Route is not for filter",
                filterRoute.getNext() instanceof Filter);
    }

    public void testRoutingIncludesOtherRestlets() throws Exception {
        String expected = "/singleton";
        registerBeanDefinition("timber", expected, TestRestlet.class, null);
        doPostProcess();

        TemplateRoute restletRoute = matchRouteFor(expected);
        assertNotNull("No route for restlet", restletRoute);
        assertTrue("Route is not for restlet",
                restletRoute.getNext() instanceof TestRestlet);
    }

    public void testRoutingIncludesResourceSubclasses() throws Exception {
        String expected = "/renewable/timber/{id}";
        registerBeanDefinition("timber", expected, TestResource.class,
                BeanDefinition.SCOPE_PROTOTYPE);

        doPostProcess();
        TemplateRoute timberRoute = matchRouteFor("/renewable/timber/sycamore");
        assertNotNull("No route for timber", timberRoute);
        assertFinderForBean("timber", timberRoute.getNext());
    }

    public void testRoutingIncludesSpringRouterStyleExplicitlyMappedBeans()
            throws Exception {
        final BeanDefinition bd = new RootBeanDefinition(ServerResource.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        this.factory.registerBeanDefinition("timber", bd);
        this.factory.registerAlias("timber", "no-slash");

        String expectedTemplate = "/renewable/timber/{farm_type}";
        router.setAttachments(Collections.singletonMap(expectedTemplate,
                "timber"));
        final RouteList actualRoutes = actualRoutes();

        assertEquals("Wrong number of routes", 3, actualRoutes.size());
        TemplateRoute timberRoute = matchRouteFor(expectedTemplate);
        assertNotNull("Missing timber route: " + actualRoutes, timberRoute);
        assertFinderForBean("timber", timberRoute.getNext());
    }

    public void testRoutingSkipsResourcesWithoutAppropriateAliases()
            throws Exception {
        final BeanDefinition bd = new RootBeanDefinition(ServerResource.class);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        this.factory.registerBeanDefinition("timber", bd);
        this.factory.registerAlias("timber", "no-slash");

        final RouteList actualRoutes = actualRoutes();
        assertEquals("Timber resource should have been skipped", 2,
                actualRoutes.size());
    }
}
