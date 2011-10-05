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

import org.restlet.data.Language;
import org.restlet.ext.rdf.Literal;

/**
 * Represents a string of characters. This string could have a type and a
 * language.
 * 
 * @author Thierry Boileau
 */
public class StringToken extends LexicalUnit {
    /** The language of the value. */
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** Does this string contains at least a new line character? */
    private boolean multiLines;

    /** The type of the represented value. */
    private String type;

    /**
     * Constructor with arguments.
     * 
     * @param contentHandler
     *            The document's parent handler.
     * @param context
     *            The parsing context.
     */
    public StringToken(RdfTurtleReader contentHandler,
            Context context) throws IOException {
        super(contentHandler, context);
        multiLines = false;
        this.parse();
    }

    /**
     * Returns true if this string of characters contains at least one newline
     * character.
     * 
     * @return
     */
    public boolean isMultiLines() {
        return multiLines;
    }

    @Override
    public void parse() throws IOException {
        getContentReader().parseString(this);
    }

    @Override
    public Object resolve() {
        Literal result = new Literal(getValue());
        if (this.type != null) {
            result.setDatatypeRef(getContext().resolve(this.type));
        }
        if (this.language != null) {
            result.setLanguage(Language.valueOf(this.language));
        }
        return result;
    }

    public void setMultiLines(boolean multiLines) {
        this.multiLines = multiLines;
    }
    
    @Override
    public String toString() {
        return getValue();
    }

}