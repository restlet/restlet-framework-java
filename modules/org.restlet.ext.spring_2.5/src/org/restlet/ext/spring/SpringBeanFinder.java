/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.spring;

import org.restlet.resource.Resource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * An alternative to {@link SpringFinder} which uses Spring's BeanFactory
 * mechanism to load a prototype bean by name.
 * 
 * @author Rhett Sutphin
 */
public class SpringBeanFinder extends SpringFinder implements BeanFactoryAware {
    /** The parent bean factory. */
    private BeanFactory beanFactory;

    /** The bean name. */
    private String beanName;

    /**
     * Default constructor.
     */
    public SpringBeanFinder() {
    }

    /**
     * Constructor.
     * 
     * @param beanFactory
     *                The Spring bean factory.
     * @param beanName
     *                The bean name.
     */
    public SpringBeanFinder(BeanFactory beanFactory, String beanName) {
        setBeanFactory(beanFactory);
        setBeanName(beanName);
    }

    @Override
    public Resource createResource() {
        Object resource = getBeanFactory().getBean(getBeanName());

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
        return beanFactory;
    }

    /**
     * Returns the bean name.
     * 
     * @return The bean name.
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * Sets the parent bean factory.
     * 
     * @param beanFactory
     *                The parent bean factory.
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Sets the bean name.
     * 
     * @param beanName
     *                The bean name.
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

}
