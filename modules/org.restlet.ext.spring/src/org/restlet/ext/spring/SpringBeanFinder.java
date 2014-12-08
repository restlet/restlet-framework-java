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
        final Object resource = findBean();

        if (!(resource instanceof ServerResource)) {
            throw new ClassCastException(getBeanName()
                    + " does not resolve to an instance of "
                    + org.restlet.resource.ServerResource.class.getName());
        }

        return (org.restlet.resource.ServerResource) resource;
    }

    private Object findBean() {
        if (getBeanFactory() == null && getApplicationContext() == null) {
            throw new IllegalStateException(
                    "Either a beanFactory or an applicationContext is required for SpringBeanFinder.");
        } else if (getApplicationContext() != null
                && getApplicationContext().containsBean(getBeanName())) {
            return getApplicationContext().getBean(getBeanName());
        } else if (getBeanFactory() != null
                && getBeanFactory().containsBean(getBeanName())) {
            return getBeanFactory().getBean(getBeanName());
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
