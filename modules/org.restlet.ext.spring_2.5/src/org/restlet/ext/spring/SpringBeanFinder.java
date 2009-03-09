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

package org.restlet.ext.spring;

import org.restlet.resource.Resource;
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
public class SpringBeanFinder extends SpringFinder
        implements BeanFactoryAware, ApplicationContextAware {
    /** The parent bean factory. */
    private volatile BeanFactory beanFactory;

    /** The parent application context. */
    private volatile ApplicationContext applicationContext;

    /** The bean name. */
    private volatile String beanName;

    /**
     * Default constructor.
     */
    public SpringBeanFinder() {
    }

    /**
     * Constructor.
     * 
     * @param beanFactory
     *            The Spring bean factory.
     * @param beanName
     *            The bean name.
     */
    public SpringBeanFinder(BeanFactory beanFactory, String beanName) {
        setBeanFactory(beanFactory);
        setBeanName(beanName);
    }

    @Override
    public Resource createResource() {
        final Object resource = findBean();

        if (!(resource instanceof Resource)) {
            throw new ClassCastException(getBeanName()
                    + " does not resolve to an instance of "
                    + Resource.class.getName());
        }

        return (Resource) resource;
    }

    private Object findBean() {
        if (getApplicationContext() != null && getApplicationContext().containsBean(getBeanName())) {
            return getApplicationContext().getBean(getBeanName());
        } else if (getBeanFactory() != null && getBeanFactory().containsBean(getBeanName())) {
            return getBeanFactory().getBean(getBeanName());
        }
        throw new IllegalStateException(
                "Either a beanFactory or an applicationContext is required for SpringBeanFinder.");
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

    /**
     * Returns the parent application context.
     *
     * @return The parent context.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
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
     * Sets the parent application context
     *
     * @param applicationContext The parent context.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
}
