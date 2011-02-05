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

import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.internal.RdfConstants;

/**
 * Represents a still unidentified Turtle token.
 * 
 * @author Thierry Boileau
 */
public class Token extends LexicalUnit {

    /**
     * Constructor with arguments.
     * 
     * @param contentHandler
     *            The document's parent handler.
     * @param context
     *            The parsing context.
     */
    public Token(RdfTurtleReader contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        this.parse();
    }

    /**
     * Constructor with value.
     * 
     * @param value
     *            The value of the current lexical unit.
     */
    public Token(String value) {
        super(value);
    }

    @Override
    public void parse() throws IOException {
        getContentReader().parseToken(this);
    }

    @Override
    public Object resolve() {
        Object result = null;
        if ((getContext() != null) && getContext().isQName(getValue())) {
            result = (getContext() != null) ? getContext().resolve(getValue())
                    : getValue();
        } else {
            // Must be a literal
            if (getValue().charAt(0) > '0' && getValue().charAt(0) < '9') {
                if (getValue().contains(".")) {
                    // Consider it as a float
                    result = new Literal(getValue(),
                            RdfConstants.XML_SCHEMA_TYPE_FLOAT);
                } else {
                    // Consider it as an integer
                    result = new Literal(getValue(),
                            RdfConstants.XML_SCHEMA_TYPE_INTEGER);
                }
            } else {
                org.restlet.Context.getCurrentLogger().warning(
                        "Cannot identify this token value: " + getValue());
                if (getContentReader() != null) {
                    org.restlet.Context.getCurrentLogger().warning(
                            getContentReader().getParsingMessage());
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return getValue();
    }

}