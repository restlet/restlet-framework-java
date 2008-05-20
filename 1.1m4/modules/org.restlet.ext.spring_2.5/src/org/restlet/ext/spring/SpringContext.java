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

import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Spring application context based on a Restlet context. Here is an example
 * illustrating the various ways to use this class:
 * 
 * <pre>
 * SpringContext springContext = new SpringContext(getContext());
 * springContext.getPropertyConfigRefs().add(&quot;war://config/database.properties&quot;);
 * springContext.getXmlConfigRefs().add(&quot;war://config/applicationContext.xml&quot;);
 * springContext.getXmlConfigRefs().add(
 *         &quot;file:///C/myApp/config/applicationContext.xml&quot;);
 * springContext.getXmlConfigRefs().add(
 *         &quot;clap://thread/config/applicationContext.xml&quot;);
 * </pre>
 * 
 * @author Jerome Louvel (contact@noelios.com)</a>
 */
public class SpringContext extends GenericApplicationContext {
    /** The parent Restlet context. */
    private volatile Context restletContext;

    /**
     * The modifiable list of configuration URIs for beans definitions via
     * property representations.
     */
    private volatile List<String> propertyConfigRefs;

    /**
     * The modifiable list of configuration URIs for beans definitions via XML
     * representations.
     */
    private volatile List<String> xmlConfigRefs;

    /** Indicates if the context has been already loaded. */
    private volatile boolean loaded;

    /**
     * Constructor.
     * 
     * @param restletContext
     *                The parent Restlet context.
     */
    public SpringContext(Context restletContext) {
        this.restletContext = restletContext;
        this.propertyConfigRefs = null;
        this.xmlConfigRefs = null;
        this.loaded = false;
    }

    /**
     * Returns the parent Restlet context.
     * 
     * @return The parent Restlet context.
     */
    public Context getRestletContext() {
        return this.restletContext;
    }

    /**
     * Returns the modifiable list of configuration URIs for beans definitions
     * via property representations.
     * 
     * @return The modifiable list of configuration URIs.
     */
    public List<String> getPropertyConfigRefs() {
        // Lazy initialization with double-check.
        List<String> p = this.propertyConfigRefs;
        if (p == null) {
            synchronized (this) {
                p = this.propertyConfigRefs;
                if (p == null)
                    this.propertyConfigRefs = p = new ArrayList<String>();
            }
        }
        return p;
    }

    /**
     * Returns the modifiable list of configuration URIs for beans definitions
     * via XML representations.
     * 
     * @return The modifiable list of configuration URIs.
     */
    public List<String> getXmlConfigRefs() {
        // Lazy initialization with double-check.
        List<String> x = this.xmlConfigRefs;
        if (x == null) {
            synchronized (this) {
                x = this.xmlConfigRefs;
                if (x == null)
                    this.xmlConfigRefs = x = new ArrayList<String>();
            }
        }
        return x;
    }

    @Override
    public void refresh() {
        // If this context hasn't been loaded yet, read all the configurations
        // registered
        if (!this.loaded) {
            Representation config = null;

            // First, read the bean definitions from properties representations
            PropertiesBeanDefinitionReader propReader = null;
            for (String ref : getPropertyConfigRefs()) {
                config = getRestletContext().getClientDispatcher().get(ref)
                        .getEntity();

                if (config != null) {
                    propReader = new PropertiesBeanDefinitionReader(this);
                    propReader.loadBeanDefinitions(new SpringResource(config));
                }
            }

            // Then, read the bean definitions from XML representations
            XmlBeanDefinitionReader xmlReader = null;
            for (String ref : getXmlConfigRefs()) {
                config = getRestletContext().getClientDispatcher().get(ref)
                        .getEntity();

                if (config != null) {
                    xmlReader = new XmlBeanDefinitionReader(this);
                    xmlReader
                            .setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
                    xmlReader.loadBeanDefinitions(new SpringResource(config));
                }
            }
        }

        // Now load or refresh
        super.refresh();
    }

}
