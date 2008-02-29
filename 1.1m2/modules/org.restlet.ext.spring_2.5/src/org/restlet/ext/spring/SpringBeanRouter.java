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

import org.restlet.Router;
import org.restlet.Finder;
import org.restlet.resource.Resource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 * Restlet {@link Router} which behaves like Spring's
 * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}.
 * It takes every bean of type {@link Resource} defined in a particular context
 * and examines its aliases (generally speaking, its name and id). If one of the
 * aliases begins with a forward slash, the resource will be attached to that
 * URL.
 * <p>
 * Example:
 * 
 * <pre>
 * &lt;beans xmlns=&quot;http://www.springframework.org/schema/beans&quot; xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xsi:schemaLocation=&quot;http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd&quot; &gt;
 *    &lt;!-- Singleton instance of this class --&gt;
 *    &lt;bean name=&quot;router&quot; class=&quot;org.restlet.ext.spring.BeanNameRouter&quot;/&gt;
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
 * @author Rhett Sutphin
 */
public class SpringBeanRouter extends Router implements
        BeanFactoryPostProcessor {

    /**
     * Creates an instance of {@link SpringBeanFinder}. This can be overriden
     * if necessary.
     * 
     * @param beanFactory
     *                The Spring bean factory.
     * @param beanName
     *                The bean name.
     */
    protected Finder createFinder(BeanFactory beanFactory, String beanName) {
        return new SpringBeanFinder(beanFactory, beanName);
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
        String[] names = factory
                .getBeanNamesForType(Resource.class, true, true);

        for (String name : names) {
            String uri = resolveUri(name, factory);
            if (uri != null) {
                attach(uri, createFinder(factory, name));
            }
        }
    }

    /**
     * Uses this first alias for this bean that starts with '/'. This mimics the
     * behavior of
     * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}.
     */
    protected String resolveUri(String resourceName,
            ConfigurableListableBeanFactory factory) {
        for (String alias : factory.getAliases(resourceName)) {
            if (alias.startsWith("/"))
                return alias;
        }

        return null;
    }
}
