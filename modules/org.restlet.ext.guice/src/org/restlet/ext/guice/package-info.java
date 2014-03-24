/**
 * <p>
 *   Tools for dependency injection (DI) of Restlet
 *   {@link org.restlet.resource.ServerResource ServerResource}
 *   types that have {@code javax.inject}-annotated members.
 *   Although the extension has "guice" in the package name and
 *   contains direct support for
 *   <a href="https://code.google.com/p/google-guice/">Google Guice version 2.0 or later</a>,
 *   the tools here can be adapted for use with any DI framework that
 *   complies with
 *   <a href="https://code.google.com/p/atinject/">JSR-330</a>.
 * </p><p>
 *   This extension provides three independent approaches for dependency-injecting Restlet
 *   server resources,
 *   the <em>self-injection</em> approach,
 *   the <em>Finder factory</em> approach, and
 *   the <em>resource-injecting application</em> approach.
 * </p><p>
 *   Note that the extension is limited to injection of server resources, and not other
 *   Restlet types, because server resources are constructed by the Restlet framework,
 *   not by the user.
 *   The last section below describes how to use a JSR-330 DI framework to inject
 *   other Restlet types, without needing the tools in this extension.
 * </p>
 *
 * <h2>Self-injection</h2>
 * <p>
 *   When using this approach:
 * </p>
 * <ul>
 *   <li> DI framework must support static field injection (Guice does). </li>
 *   <li> No constructor injection for resources; only field and method injection will work. </li>
 * </ul>
 * <p>
 *   In the self-injection approach, extend
 *   {@link org.restlet.ext.guice.SelfInjectingServerResource SelfInjectingServerResource}
 *   and annotate fields to be injected with {@code @Inject}.
 * </p><p>
 *   To inject resources with Guice, install a
 *   {@link org.restlet.ext.guice.SelfInjectingServerResourceModule SelfInjectingServerResourceModule}
 *   when creating the {@code Injector}.
 * </p>
 *
 * <h2>Finder factory</h2>
 * <p>
 *   When using this approach:
 * </p>
 * <ul>
 *   <li> DI framework does <em>not</em> need to support static field injection. </li>
 *   <li> All forms of injection are allowed: constructor, field, method. </li>
 *   <li>
 *     Target resource can be specified by type alone or
 *     by type and JSR-330 {@code Qualifier}.
 *   </li>
 * </ul>
 * <p>
 *   In the {@link org.restlet.resource.Finder Finder} factory approach, inject
 *   {@link org.restlet.ext.guice.FinderFactory FinderFactory}
 *   into contexts where routing to resources is initialized,
 *   e.g., {@link org.restlet.Application#createInboundRoot createInboundRoot()},
 *   and use
 *   {@link org.restlet.ext.guice.FinderFactory#finder FinderFactory.finder(Class<?>)}
 *   in calls to
 *   {@link org.restlet.routing.Router#attach Router.attach()} instead of the
 *   plain server resource class name. For example:
 * </p>
 * <pre>
 *   // Binding in Guice:
 *   bind(ServerResource.class)
 *       .annotatedWith(Hello.class)
 *       .to(HelloServerResource.class);
 *
 *   // In createInboundRoot():
 *   FinderFactory finderFactory = ...;
 *
 *   // Attachment with no coupling to concrete resource type:
 *   router.attach("/hello", finderFactory.finder(ServerResource.class, Hello.class);
 *
 *   // Attachment with direct knowledge of concrete resource type:
 *   router.attach("/bye", finderFactory.finder(ByeServerResource.class);
 * </pre>
 * <p>
 *   To use a Guice-enabled {@code FinderFactory}, install a
 *   {@link org.restlet.ext.guice.RestletGuice.Module RestletGuice.Module}
 *   when creating the {@code Injector}.
 *   ({@link org.restlet.ext.guice.RestletGuice RestletGuice} has convenience
 *   methods to install such a module that parallel those in the {@code Guice} class.)
 * </p><p>
 *   Alternatively, for standalone Applications, create a single {@code RestletGuice.Module}
 *   instance, possibly passing other Guice modules to the constructor, and use it
 *   as the {@code FinderFactory} in {@code createInboundRoot()}.
 * </p>
 *
 * <h2>Resource-injecting application</h2>
 * <p>
 *   When using this approach:
 * </p>
 * <ul>
 *   <li> DI framework does <em>not</em> need to support static field injection. </li>
 *   <li> No constructor injection for resources; only field and method injection will work. </li>
 *   <li> Application instance must itself be injected. </li>
 * </ul>
 * <p>
 *   In the resource-injecting application approach, extend
 *   {@link org.restlet.ext.guice.ResourceInjectingApplication ResourceInjectingApplication}
 *   and use
 *   {@code org.restlet.ext.guice.ResourceInjectingApplication#newRouter newRouter()}
 *   instead of {@code new Router(...)}.
 *   The overridden {@code createFinder} will arrange to inject.
 * </p><p>
 *   To work with Guice, install a
 *   {@link org.restlet.ext.guice.SelfInjectingServerResourceModule SelfInjectingServerResourceModule}
 *   when creating the {@code Injector} that injects the application.
 *   To work with another JSR-330-compliant framework, bind
 *   {@link org.restlet.ext.guice.SelfInjectingServerResource.MembersInjector SelfInjectingServerResource.MembersInjector}
 *   to a framework-specific implementation.
 * </p>
 *
 * <h2>Injecting other Restlet types</h2>
 * <p>
 *   Instead of calling {@code new FooApplication(...)} when attaching an application
 *   in setting up a component, inject that application beforehand:
 * </p>
 * <pre>
 *   public class MyComponent extends Component {
 *
 *       public static void main(String... args) {
 *           // Run as standalone component:
 *           Injector injector = <em>... create injector ...</em>;
 *           MyComponent comp = injector.getInstance(MyComponent.class);
 *           // <em>... shutdown hooks, etc. ...</em>
 *           comp.start();
 *       }
 *
 *       &#64;Inject
 *       MyComponent(FooApplication fooApp, BarApplication barApp) {
 *           // ...
 *           getDefaultHost().attach("/foo", fooApp);
 *           getDefaultHost().attach("/bar", barApp);
 *       }
 *   }
 * </pre>
 * <p>
 *   To avoid coupling a knowledge of a specific application subtype
 *   in this setting, use qualifiers:
 * </p>
 * <pre>
 *   &#64;Inject
 *   MyComponent(&#64;Foo Application fooApp, &#64;Bar Application barApp) {
 *       // ...
 *       getDefaultHost().attach("/foo", fooApp);
 *       getDefaultHost().attach("/bar", barApp);
 *   }
 *
 *   // With the qualifiers defined elsewhere:
 *
 *   &#64;java.lang.annotation.Retention(RUNTIME)
 *   &#64;javax.inject.Qualifier
 *   public &#64;interface Foo {}
 * </pre>
 * <p>
 *   Using the {@code @Named} qualifier trades some type-safety for
 *   convenience:
 * </p>
 * <pre>
 *   &#64;Inject
 *   MyComponent(&#64;Named(FOO) Application fooApp, &#64;Named(BAR) Application barApp) ...
 * </pre>
 */
package org.restlet.ext.guice;
