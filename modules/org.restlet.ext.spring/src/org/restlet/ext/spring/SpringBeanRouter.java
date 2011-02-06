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

import java.util.Map;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Restlet {@link Router} which behaves like Spring's
 * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}. It
 * takes every bean of type {@link org.restlet.resource.ServerResource} or
 * {@link Restlet} defined in a particular context and examines its aliases
 * (generally speaking, its name and id). If one of the aliases begins with a
 * forward slash, the resource will be attached to that URI.
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
 * 
 *    &lt;!-- Singleton bean for a restlet --&gt;
 *    &lt;bean name=&quot;/studies/{study-identifier}/files&quot; id=&quot;filesResource&quot; autowire=&quot;byName&quot; class=&quot;edu.northwestern.myapp.MyDirectory&quot; /&gt;
 * &lt;/beans&gt;
 * </pre>
 * 
 * This will route two resources and one restlet: <code>"/studies"</code>,
 * <code>"/studies/{study-identifier}/template"</code>, and
 * <code>"/studies/{study-identifier}/files"</code> to the corresponding beans.
 * N.b.: Resources must be scoped prototype, since a new instance must be
 * created for each request. Restlets may be singletons (this class will only
 * ever load one instance for each).
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

    /** Supplemental explicit mappings. */
    private Map<String, String> attachments;

    /** If beans should be searched for higher up in the BeanFactory hierarchy. */
    private volatile boolean findingInAncestors = true;

    /**
     * Constructor.
     */
    public SpringBeanRouter() {
        super();
    }

    /**
     * Constructor with a parent context.
     */
    public SpringBeanRouter(Context context) {
        super(context);
    }

    /**
     * Constructor with a parent Restlet.
     */
    public SpringBeanRouter(Restlet parent) {
        super(parent.getContext());
    }

    /**
     * Attaches all the resources.
     * 
     * @param beanFactory
     *            The Spring bean factory.
     */
    @SuppressWarnings("deprecation")
    private void attachAllResources(ListableBeanFactory beanFactory) {
        for (String beanName : getBeanNamesByType(
                org.restlet.resource.Resource.class, beanFactory)) {
            String uri = resolveUri(beanName, beanFactory);

            if (uri != null)
                attachResource(uri, beanName, beanFactory);
        }

        for (String beanName : getBeanNamesByType(
                org.restlet.resource.ServerResource.class, beanFactory)) {
            String uri = resolveUri(beanName, beanFactory);

            if (uri != null)
                attachResource(uri, beanName, beanFactory);
        }
    }

    /**
     * Attaches all the Restlet instances.
     * 
     * @param beanFactory
     *            The Spring bean factory.
     */
    private void attachAllRestlets(ListableBeanFactory beanFactory) {
        for (String beanName : getBeanNamesByType(Restlet.class, beanFactory)) {
            String uri = resolveUri(beanName, beanFactory);

            if (uri != null)
                attachRestlet(uri, beanName, beanFactory);
        }
    }

    /**
     * Attaches the named resource bean at the given URI, creating a finder for
     * it via {@link #createFinder(BeanFactory, String)}.
     * 
     * @param uri
     *            The attachment URI.
     * @param beanName
     *            The bean name.
     * @param beanFactory
     *            The Spring bean factory.
     */
    protected void attachResource(String uri, String beanName,
            BeanFactory beanFactory) {
        attach(uri, createFinder(beanFactory, beanName));
    }

    /**
     * Attaches the named restlet bean directly at the given URI.
     * 
     * @param uri
     *            The attachment URI.
     * @param beanName
     *            The bean name.
     * @param beanFactory
     *            The Spring bean factory.
     */
    protected void attachRestlet(String uri, String beanName,
            BeanFactory beanFactory) {
        attach(uri, (Restlet) beanFactory.getBean(beanName));
    }

    /**
     * Creates an instance of {@link SpringBeanFinder}. This can be overridden
     * if necessary.
     * 
     * @param beanFactory
     *            The Spring bean factory.
     * @param beanName
     *            The bean name.
     * @see #attachResource
     */
    protected Finder createFinder(BeanFactory beanFactory, String beanName) {
        return new SpringBeanFinder(this, beanFactory, beanName);
    }

    /**
     * Returns supplemental explicit mappings
     * 
     * @return Supplemental explicit mappings
     */
    protected Map<String, String> getAttachments() {
        return this.attachments;
    }

    /**
     * Returns the list of bean name for the given type.
     * 
     * @param beanClass
     *            The bean class to lookup.
     * @param beanFactory
     *            The Spring bean factory.
     * @return The array of bean names.
     */
    private String[] getBeanNamesByType(Class<?> beanClass,
            ListableBeanFactory beanFactory) {
        return isFindingInAncestors() ? BeanFactoryUtils
                .beanNamesForTypeIncludingAncestors(beanFactory, beanClass,
                        true, true) : beanFactory.getBeanNamesForType(
                beanClass, true, true);
    }

    /**
     * Indicates if the attachments contain a mapping for the given URI.
     * 
     * @param name
     *            The name to test.
     * @return True if the attachments contain a mapping for the given URI.
     */
    private boolean isAvailableUri(String name) {
        return name.startsWith("/")
                && (getAttachments() == null || !getAttachments().containsKey(
                        name));
    }

    /**
     * Returns true if bean names will be searched for higher up in the
     * BeanFactory hierarchy. Default is true.
     * 
     * @return True if bean names will be searched for higher up in the
     *         BeanFactory hierarchy.
     * @deprecated Use {@link #isFindingInAncestors()} instead.
     */
    @Deprecated
    public boolean isFindInAncestors() {
        return this.findingInAncestors;
    }

    /**
     * Returns true if bean names will be searched for higher up in the
     * BeanFactory hierarchy. Default is true.
     * 
     * @return True if bean names will be searched for higher up in the
     *         BeanFactory hierarchy.
     */
    public boolean isFindingInAncestors() {
        return isFindInAncestors();
    }

    /**
     * Attaches all {@link ServerResource} and {@link Restlet} beans found in
     * the surrounding bean factory for which {@link #resolveUri} finds a usable
     * URI. Also attaches everything explicitly routed in the attachments
     * property.
     * 
     * @param beanFactory
     *            The Spring bean factory.
     * @see #setAttachments
     */
    @SuppressWarnings("deprecation")
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {

        ListableBeanFactory source = this.applicationContext == null ? beanFactory
                : this.applicationContext;
        attachAllResources(source);
        attachAllRestlets(source);

        if (getAttachments() != null) {
            for (Map.Entry<String, String> attachment : getAttachments()
                    .entrySet()) {
                String uri = attachment.getKey();
                String beanName = attachment.getValue();
                Class<?> beanType = source.getType(beanName);

                if (org.restlet.resource.Resource.class
                        .isAssignableFrom(beanType)) {
                    attachResource(uri, beanName, source);
                } else if (org.restlet.resource.ServerResource.class
                        .isAssignableFrom(beanType)) {
                    attachResource(uri, beanName, source);
                } else if (Restlet.class.isAssignableFrom(beanType)) {
                    attachRestlet(uri, beanName, source);
                } else {
                    throw new IllegalStateException(
                            beanName
                                    + " is not routable.  It must be either a Resource, a ServerResource or a Restlet.");
                }
            }
        }
    }

    /**
     * Uses this first alias for this bean that starts with '/' and is not
     * mapped in the explicit attachments to another bean. This mimics the
     * behavior of
     * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}
     * .
     * 
     * @param beanName
     *            The bean name to lookup in the bean factory aliases.
     * @param beanFactory
     *            The Spring bean factory.
     * @return The alias URI.
     */
    protected String resolveUri(String beanName, ListableBeanFactory beanFactory) {
        if (isAvailableUri(beanName)) {
            return beanName;
        }

        for (final String alias : beanFactory.getAliases(beanName)) {
            if (isAvailableUri(alias)) {
                return alias;
            }
        }

        return null;
    }

    /**
     * Sets the Spring application context.
     * 
     * @param applicationContext
     *            The context.
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets an explicit mapping of URI templates to bean IDs to use in addition
     * to the usual bean name mapping behavior. If a URI template appears in
     * both this mapping and as a bean name, the bean it is mapped to here is
     * the one that will be used.
     * 
     * @param attachments
     *            Supplemental explicit mappings.
     * @see SpringRouter
     */
    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    /**
     * Sets if bean names will be searched for higher up in the BeanFactory
     * hierarchy.
     * 
     * @param findingInAncestors
     *            Search for beans higher up in the BeanFactory hierarchy.
     * @deprecated Use {@link #setFindingInAncestors(boolean)} instead.
     */
    @Deprecated
    public void setFindInAncestors(boolean findingInAncestors) {
        this.findingInAncestors = findingInAncestors;
    }

    /**
     * Sets if bean names will be searched for higher up in the BeanFactory
     * hierarchy.
     * 
     * @param findingInAncestors
     *            Search for beans higher up in the BeanFactory hierarchy.
     */
    public void setFindingInAncestors(boolean findingInAncestors) {
        setFindInAncestors(findingInAncestors);
    }

}
