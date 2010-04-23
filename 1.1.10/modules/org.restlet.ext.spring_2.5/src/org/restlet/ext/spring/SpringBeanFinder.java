/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.spring;

import org.restlet.resource.Resource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * An alternative to {@link SpringFinder} which uses Spring's BeanFactory
 * mechanism to load a prototype bean by name.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Rhett Sutphin
 */
public class SpringBeanFinder extends SpringFinder implements BeanFactoryAware {
    /** The parent bean factory. */
    private volatile BeanFactory beanFactory;

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
        final Object resource = getBeanFactory().getBean(getBeanName());

        if (!(resource instanceof Resource)) {
            throw new ClassCastException(getBeanName()
                    + " does not resolve to an instance of "
                    + Resource.class.getName());
        }

        return (Resource) resource;
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

}
