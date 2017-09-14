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

import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
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
 * @author Jerome Louvel</a>
 */
public class SpringContext extends GenericApplicationContext {
    /** Indicates if the context has been already loaded. */
    private volatile boolean loaded;

    /**
     * The modifiable list of configuration URIs for beans definitions via
     * property representations.
     */
    private volatile List<String> propertyConfigRefs;

    /** The parent Restlet context. */
    private volatile Context restletContext;

    /**
     * The modifiable list of configuration URIs for beans definitions via XML
     * representations.
     */
    private volatile List<String> xmlConfigRefs;

    /**
     * Constructor.
     * 
     * @param restletContext
     *            The parent Restlet context.
     */
    public SpringContext(Context restletContext) {
        this.restletContext = restletContext;
        this.propertyConfigRefs = null;
        this.xmlConfigRefs = null;
        this.loaded = false;
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
                if (p == null) {
                    this.propertyConfigRefs = p = new ArrayList<String>();
                }
            }
        }
        return p;
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
                if (x == null) {
                    this.xmlConfigRefs = x = new ArrayList<String>();
                }
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
            for (final String ref : getPropertyConfigRefs()) {
                config = getRestletContext().getClientDispatcher()
                        .handle(new Request(Method.GET, ref)).getEntity();

                if (config != null) {
                    propReader = new PropertiesBeanDefinitionReader(this);
                    propReader.loadBeanDefinitions(new SpringResource(config));
                }
            }

            // Then, read the bean definitions from XML representations
            XmlBeanDefinitionReader xmlReader = null;
            for (final String ref : getXmlConfigRefs()) {
                config = getRestletContext().getClientDispatcher()
                        .handle(new Request(Method.GET, ref)).getEntity();

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
