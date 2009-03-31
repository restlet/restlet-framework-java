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

package org.restlet.ext.spring;

import org.restlet.resource.Finder;
import org.restlet.resource.Resource;
import org.restlet.routing.Router;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Restlet {@link Router} which behaves like Spring's
 * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}. It
 * takes every bean of type {@link Resource} defined in a particular context and
 * examines its aliases (generally speaking, its name and id). If one of the
 * aliases begins with a forward slash, the resource will be attached to that
 * URL.
 * <p>
 * Example:
 * 
 * <pre>
 * &lt;beans xmlns=&quot;http://www.springframework.org/schema/beans&quot; xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xsi:schemaLocation=&quot;http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd&quot; &gt;
 *    &lt;!-- Singleton instance of this class --&gt;
 *    &lt;bean name=&quot;router&quot; class=&quot;org.restlet.ext.spring.SpringBeanRouter&quot;/&gt;
 * 
 *    &lt;!-- Prototype beans for the resources --&gt;
 *    &lt;bean name=&quot;/studies&quot; id=&quot;studiesResource&quot; autowire=&quot;byName&quot; scope=&quot;prototype&quot; class=&quot;edu.northwestern.myapp.StudiesResource&quot; &gt;
 *       &lt;property name=&quot;studyDao&quot; ref=&quot;studyDao&quot;/&gt;
 *    &lt;/bean&gt;
 * 
 *    &lt;bean name=&quot;/studies/{study-identifier}/template&quot; id=&quot;templateResource&quot; autowire=&quot;byName&quot; scope=&quot;prototype&quot; class=&quot;edu.northwestern.myapp.TemplateResource&quot; /&gt;
 * &lt;/beans&gt;
 * </pre>
 * 
 * This will route two resources: <code>"/studies"</code> and
 * <code>"/studies/{study-identifier}/template"</code> to the corresponding
 * Resource subclass.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Rhett Sutphin
 * @author James Maki
 */
public class SpringBeanRouter extends Router implements
        ApplicationContextAware, BeanFactoryPostProcessor {

    /** The Spring application context. */
    private volatile ApplicationContext applicationContext;

    /** If beans should be searched for higher up in the BeanFactory hierarchy */
    private volatile boolean findInAncestors = true;

    /**
     * Creates an instance of {@link SpringBeanFinder}. This can be overriden if
     * necessary.
     * 
     * @param beanFactory
     *            The Spring bean factory.
     * @param beanName
     *            The bean name.
     */
    protected Finder createFinder(BeanFactory beanFactory, String beanName) {
        return new SpringBeanFinder(beanFactory, beanName);
    }

    /**
     * Returns true if bean names will be searched for higher up in the
     * BeanFactory hierarchy.
     * <p>
     * Default is true.
     * 
     * @return true if bean names will be searched for higher up in the
     *         BeanFactory hierarchy
     */
    public boolean isFindInAncestors() {
        return findInAncestors;
    }

    /**
     * Modify the application context by looking up the name of all beans of
     * type Resource, calling the
     * {@link #resolveUri(String, ConfigurableListableBeanFactory)} method for
     * each of them. If an URI is found, a finder is created for the bean name
     * using the {@link #createFinder(BeanFactory, String)} method.
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
            throws BeansException {
        String[] names = isFindInAncestors() ? BeanFactoryUtils
                .beanNamesForTypeIncludingAncestors(factory, Resource.class,
                        true, true) : factory.getBeanNamesForType(
                Resource.class, true, true);

        BeanFactory bf = this.applicationContext == null ? factory
                : this.applicationContext;
        for (final String name : names) {
            final String uri = resolveUri(name, factory);
            if (uri != null) {
                attach(uri, createFinder(bf, name));
            }
        }
    }

    /**
     * Uses this first alias for this bean that starts with '/'. This mimics the
     * behavior of
     * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}
     * .
     */
    protected String resolveUri(String resourceName,
            ConfigurableListableBeanFactory factory) {
        if (isUri(resourceName)) {
            return resourceName;
        }
        for (final String alias : factory.getAliases(resourceName)) {
            if (isUri(alias)) {
                return alias;
            }
        }

        return null;
    }

    private boolean isUri(String name) {
        return name.startsWith("/");
    }

    /**
     * Sets the Spring application context.
     * 
     * @param applicationContext
     *            The context to set.
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets if bean names will be searched for higher up in the BeanFactory
     * hierarchy.
     * 
     * @param findInAncestors
     *            search for beans higher up in the BeanFactory hierarchy.
     */
    public void setFindInAncestors(boolean findInAncestors) {
        this.findInAncestors = findInAncestors;
    }
}
