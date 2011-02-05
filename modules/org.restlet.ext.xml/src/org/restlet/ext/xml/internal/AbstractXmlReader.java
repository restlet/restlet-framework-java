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

package org.restlet.ext.xml.internal;

import java.util.HashMap;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Abstract SAX XML Reader.
 * 
 * @author Warren Janssens
 */
public abstract class AbstractXmlReader implements XMLReader {

    /** The features map. */
    private final HashMap<String, Boolean> features;

    /** The properties map. */
    private final HashMap<String, Object> properties;

    /** The entity resolver. */
    private EntityResolver entityResolver;

    /** The DTD handler. */
    private DTDHandler handler;

    /** The content handler. */
    private ContentHandler contentHandler;

    /** The error handler. */
    private ErrorHandler errorHandler;

    /**
     * Default constructor.
     */
    public AbstractXmlReader() {
        this.features = new HashMap<String, Boolean>();
        this.properties = new HashMap<String, Object>();
        this.contentHandler = null;
        this.entityResolver = null;
        this.errorHandler = null;
        this.handler = null;
    }

    /**
     * Return the content handler.
     * 
     * @return The content handler.
     * @see XMLReader#getContentHandler()
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Return the DTD handler.
     * 
     * @return The DTD handler.
     * @see XMLReader#getDTDHandler()
     */
    public DTDHandler getDTDHandler() {
        return handler;
    }

    /**
     * Return the entity resolver.
     * 
     * @return The entity resolver.
     * @see XMLReader#getEntityResolver()
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Return the error handler.
     * 
     * @return The error handler.
     * @see XMLReader#getErrorHandler()
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns the feature by name.
     * 
     * @param name
     *            The feature name.
     * @return The feature.
     * @see XMLReader#getFeature(String)
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        final Boolean result = features.get(name);
        return result == null ? false : result.booleanValue();
    }

    /**
     * Returns the property by name.
     * 
     * @param name
     *            The property name.
     * @return The property.
     * @see XMLReader#getProperty(String)
     */
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return properties.get(name);
    }

    /**
     * Sets the content handler.
     * 
     * @param contentHandler
     *            The content handler.
     * @see XMLReader#setContentHandler(ContentHandler)
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    /**
     * Sets the DTD handler.
     * 
     * @param handler
     *            The DTD handler.
     * @see XMLReader#setDTDHandler(DTDHandler)
     */
    public void setDTDHandler(DTDHandler handler) {
        this.handler = handler;
    }

    /**
     * Sets the entity resolver.
     * 
     * @param entityResolver
     *            The entity resolver.
     * @see XMLReader#setEntityResolver(EntityResolver)
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * Sets the error handler.
     * 
     * @param errorHandler
     *            The error handler.
     * @see XMLReader#setErrorHandler(ErrorHandler)
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Sets a feature.
     * 
     * @param name
     *            The feature name.
     * @param value
     *            The feature value.
     * @see XMLReader#setFeature(String, boolean)
     */
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        this.features.put(name, value);
    }

    /**
     * Sets a property.
     * 
     * @param name
     *            The property name.
     * @param value
     *            The property value.
     * @see XMLReader#setProperty(String, Object)
     */
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        this.properties.put(name, value);
    }

}