/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.spring;

import org.restlet.Context;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * An alternative to {@link SpringFinder} which uses Spring's BeanFactory
 * mechanism to load a prototype bean by name.
 * 
 * If both a {@link BeanFactory} and a {@link ApplicationContext} are provided,
 * the bean will be looked up first in the application context and then in the
 * bean factory.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Rhett Sutphin
 */
public class SpringBeanFinder extends SpringFinder implements BeanFactoryAware,
        ApplicationContextAware {
    /** The parent application context. */
    private volatile ApplicationContext applicationContext;

    /** The parent bean factory. */
    private volatile BeanFactory beanFactory;

    /** The bean name. */
    private volatile String beanName;

    /** The associated router. */
    private volatile Router router;

    /**
     * Default constructor.
     */
    public SpringBeanFinder() {
    }

    /**
     * Constructor.
     * 
     * @param router
     *            The associated router used to retrieve the context.
     * @param beanFactory
     *            The Spring bean factory.
     * @param beanName
     *            The bean name.
     */
    public SpringBeanFinder(Router router, BeanFactory beanFactory,
            String beanName) {
        this.router = router;
        setBeanFactory(beanFactory);
        setBeanName(beanName);
    }

    @Override
    public ServerResource create() {
        final ServerResource resource = findBean(ServerResource.class);

        if (resource == null) {
            throw new ClassCastException(getBeanName()
                    + " does not resolve to an instance of "
                    + org.restlet.resource.ServerResource.class.getName());
        }

        return resource;
    }

    @SuppressWarnings("deprecation")
    @Override
    public org.restlet.resource.Resource createResource() {
        return findBean(org.restlet.resource.Resource.class);
    }

    @SuppressWarnings({ "unchecked" })
    private <T> T findBean(Class<T> expectedType) {
        if (getBeanFactory() == null && getApplicationContext() == null) {
            throw new IllegalStateException(
                    "Either a beanFactory or an applicationContext is required for SpringBeanFinder.");
        }
        BeanFactory effectiveFactory = getApplicationContext();
        if (effectiveFactory == null) effectiveFactory = getBeanFactory();

        if (effectiveFactory.containsBean(getBeanName())) {
            if (expectedType.isAssignableFrom(effectiveFactory.getType(getBeanName()))) {
                return (T) effectiveFactory.getBean(getBeanName());
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException(String.format(
                    "No bean named %s present.", getBeanName()));
        }
    }

    /**
     * Returns the parent application context.
     * 
     * @return The parent context.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Returns the parent bean factory.
     * 
     * @return The parent bean factory.
     */
    public BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    /**
     * Returns the bean name.
     * 
     * @return The bean name.
     */
    public String getBeanName() {
        return this.beanName;
    }

    @Override
    public Context getContext() {
        return (getRouter() == null) ? Context.getCurrent() : getRouter()
                .getContext();
    }

    /**
     * Returns the associated router.
     * 
     * @return The associated router.
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Returns the associated router.
     * 
     * @return The associated router.
     * @deprecated Use {@link #getRouter()} instead
     */
    @Deprecated
    public SpringBeanRouter getSpringBeanRouter() {
        return (SpringBeanRouter) router;
    }

    /**
     * Sets the parent application context
     * 
     * @param applicationContext
     *            The parent context.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets the parent bean factory.
     * 
     * @param beanFactory
     *            The parent bean factory.
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Sets the bean name.
     * 
     * @param beanName
     *            The bean name.
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Sets the associated router.
     * 
     * @param router
     *            The associated router.
     */
    public void setRouter(Router router) {
        this.router = router;
    }

}
