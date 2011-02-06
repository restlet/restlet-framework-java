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

package org.restlet.ext.rdf.internal.turtle;

import java.io.IOException;

/**
 * Represents a lexical unit inside a Turtle document.
 * 
 * @author Thierry Boileau
 */
public abstract class LexicalUnit {

    /** The content handler of the current Turtle document. */
    private RdfTurtleReader contentReader;

    /** The context maintained during the parsing. */
    private Context context;

    /** The parsed value as a simple string of characters. */
    private String value;

    /**
     * Constructor with arguments.
     * 
     * @param contentHandler
     *            The document's parent handler.
     * @param context
     *            The parsing context.
     */
    public LexicalUnit(RdfTurtleReader contentReader,
            Context context) {
        super();
        this.contentReader = contentReader;
        this.context = context;
    }

    /**
     * Constructor with value.
     * 
     * @param value
     *            The value of the current lexical unit.
     */
    public LexicalUnit(String value) {
        super();
        setValue(value);
    }

    /**
     * Returns the document's reader.
     * 
     * @return The document's reader.
     */
    public RdfTurtleReader getContentReader() {
        return contentReader;
    }

    /**
     * Returns the parsing context.
     * 
     * @return The parsing context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the current value.
     * 
     * @return The current value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Contains the parsing logic of this lexical unit.
     * 
     * @throws IOException
     */
    public abstract void parse() throws IOException;

    /**
     * Resolves the current value as a reference or a literal or a graph of
     * links according to the current context.
     * 
     * @return The current value as a reference or a literal or a graph of links
     *         according to the current context.
     */
    public abstract Object resolve();

    /**
     * Sets the parsing context.
     * 
     * @param context
     *            The parsing context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The current value.
     */
    public void setValue(String value) {
        this.value = value;
    }

}