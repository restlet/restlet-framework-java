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

package org.restlet.engine.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Utility class which extends the provided {@link DefaultHandler} and
 * implements the {@link LSResourceResolver} interface. All the methods of this
 * class do nothing besides generating log messages.
 * 
 * @author Raif S. Naffah
 * @author Jerome Louvel
 */
public class DefaultSaxHandler extends DefaultHandler implements
        LSResourceResolver {

    /**
     * A class field set to {@code true} if the the JAXP debug property is
     * turned on; {@code false} otherwise. This is used to control the degree of
     * output generated in the logs.
     */
    private static boolean debug;

    static {
        try {
            final String debugStr = System.getProperty("jaxp.debug");
            debug = debugStr != null && !"false".equalsIgnoreCase(debugStr);
        } catch (SecurityException x) {
            debug = false;
        }
    }

    /**
     * Set to {@code true} if the current Context's logger is capable of
     * outputting messages at the CONFIG level; {@code false} otherwise.
     */
    private boolean loggable;

    /** The current context JDK {@link Logger} to use for message output. */
    private Logger logger;

    /**
     * Trivial constructor.
     */
    public DefaultSaxHandler() {
        super();
        logger = Context.getCurrentLogger();
        loggable = logger.isLoggable(Level.CONFIG);
    }

    @Override
    public void error(SAXParseException x) throws SAXException {
        if (loggable) {
            final String msg = "[ERROR] - Unexpected exception while parsing "
                    + "an instance of PUBLIC [" + x.getPublicId()
                    + "], SYSTEM [" + x.getSystemId() + "] - line #"
                    + x.getLineNumber() + ", column #" + x.getColumnNumber();
            if (debug) {
                Context.getCurrentLogger().log(Level.CONFIG, msg, x);
            } else {
                logger.config(msg + ": " + x.getLocalizedMessage());
            }
        }
    }

    @Override
    public void fatalError(SAXParseException x) throws SAXException {
        if (loggable) {
            final String msg = "[FATAL] - Unexpected exception while parsing "
                    + "an instance of PUBLIC [" + x.getPublicId()
                    + "], SYSTEM [" + x.getSystemId() + "] - line #"
                    + x.getLineNumber() + ", column #" + x.getColumnNumber();
            if (debug) {
                Context.getCurrentLogger().log(Level.CONFIG, msg, x);
            } else {
                logger.config(msg + ": " + x.getLocalizedMessage());
            }
        }
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        if (loggable) {
            logger.config("Resolve entity with PUBLIC [" + publicId
                    + "], and SYSTEM [" + systemId + "]");
        }
        return super.resolveEntity(publicId, systemId);
    }

    /**
     * Allow the application to resolve external resources.
     * <p>
     * This implementation <em>always</em> returns a {@code null}.
     * 
     * @param type
     *            The type of the resource being resolved.
     * @param namespaceUri
     *            The namespace of the resource being resolved.
     * @param publicId
     *            The public identifier.
     * @param systemId
     *            The system identifier.
     * @param baseUri
     *            The absolute base URI of the resource being parsed.
     * @return Always {@code null}.
     */
    public LSInput resolveResource(String type, String namespaceUri,
            String publicId, String systemId, String baseUri) {
        if (loggable) {
            logger.config("Resolve resource with type [" + type
                    + "], namespace URI [" + namespaceUri + "], PUBLIC ["
                    + publicId + "], SYSTEM [" + systemId + "], and base URI ["
                    + baseUri + "]");
        }
        return null;
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        super.skippedEntity(name);
        if (loggable) {
            logger.config("Skipped entity named [" + name + "]");
        }
    }

    @Override
    public void warning(SAXParseException x) throws SAXException {
        if (loggable) {
            final String msg = "[WARN] - Unexpected exception while parsing "
                    + "an instance of PUBLIC [" + x.getPublicId()
                    + "], SYSTEM [" + x.getSystemId() + "] - line #"
                    + x.getLineNumber() + ", column #" + x.getColumnNumber();
            if (debug) {
                Context.getCurrentLogger().log(Level.CONFIG, msg, x);
            } else {
                logger.config(msg + ": " + x.getLocalizedMessage());
            }
        }
    }
}
