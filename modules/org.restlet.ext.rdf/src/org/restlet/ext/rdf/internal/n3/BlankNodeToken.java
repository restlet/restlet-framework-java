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

package org.restlet.ext.rdf.internal.n3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.LinkReference;

/**
 * Represents a blank node inside a RDF N3 document. Contains all the logic to
 * parse a blank node in N3 documents.
 */
public class BlankNodeToken extends LexicalUnit {

    /** List of lexical units contained by this blank node. */
    List<LexicalUnit> lexicalUnits;

    /**
     * Constructor. The blank node is given a new identifier thanks to the
     * context..
     * 
     * @param contentHandler
     *            The parent content handler.
     * @param context
     *            The context used to resolved references.
     * @throws IOException
     */
    public BlankNodeToken(RdfN3ContentHandler contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        lexicalUnits = new ArrayList<LexicalUnit>();
        this.setValue("_:" + RdfN3ContentHandler.newBlankNodeId());
        lexicalUnits.add(this);
        this.parse();
    }

    /**
     * Constructor
     * 
     * @param value
     *            The value of this blank node.
     */
    public BlankNodeToken(String value) {
        super(value);
    }

    @Override
    public void parse() throws IOException {
        getContentHandler().step();
        do {
            getContentHandler().consumeWhiteSpaces();
            switch (getContentHandler().getChar()) {
            case '(':
                lexicalUnits.add(new ListToken(getContentHandler(),
                        getContext()));
                break;
            case '<':
                if (getContentHandler().step() == '=') {
                    lexicalUnits.add(new Token("<="));
                    getContentHandler().step();
                    getContentHandler().discard();
                } else {
                    getContentHandler().stepBack();
                    lexicalUnits.add(new UriToken(getContentHandler(),
                            getContext()));
                }
                break;
            case '_':
                lexicalUnits.add(new BlankNodeToken(getContentHandler()
                        .parseToken()));
                break;
            case '"':
                lexicalUnits.add(new StringToken(getContentHandler(),
                        getContext()));
                break;
            case '[':
                lexicalUnits.add(new BlankNodeToken(getContentHandler(),
                        getContext()));
                break;
            case '{':
                lexicalUnits.add(new FormulaToken(getContentHandler(),
                        getContext()));
                break;
            case ']':
                break;
            case RdfN3ContentHandler.EOF:
                break;
            default:
                lexicalUnits.add(new Token(getContentHandler(), getContext()));
                break;
            }
        } while (getContentHandler().getChar() != RdfN3ContentHandler.EOF
                && getContentHandler().getChar() != ']');
        if (getContentHandler().getChar() == ']') {
            // Set the cursor at the right of the list token.
            getContentHandler().step();
        }
    }

    @Override
    public Object resolve() {
        if (getContentHandler() != null) {
            getContentHandler().generateLinks(lexicalUnits);
        }

        if (getValue() != null) {
            if (getValue().startsWith("_:")) {
                return new Reference(getValue());
            } else {
                return LinkReference.createBlank(getValue());
            }
        } else {
            // TODO Error
        }

        return null;
    }
}