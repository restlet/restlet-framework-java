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

package org.restlet.ext.rdf.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlankNodeToken extends LexicalUnit {

    List<LexicalUnit> lexicalUnits;

    public BlankNodeToken(RdfN3ContentHandler contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        lexicalUnits = new ArrayList<LexicalUnit>();
        this.setValue(RdfN3ContentHandler.newBlankNodeId());
        lexicalUnits.add(this);
        this.parse();
    }

    public BlankNodeToken(String value) {
        super(value);
    }

    @Override
    public String getValue() {
        if (super.getValue() != null) {
            return super.getValue();
        } else if (this.lexicalUnits != null) {
            return this.lexicalUnits.toString();
        }
        return null;
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
        if (!isResolved()) {
            setResolved(true);
            if (getContentHandler() != null) {
                getContentHandler().generateLinks(lexicalUnits);
            }
        }

        if (this.getValue() != null) {
            return getValue();
        } else {
        }

        return null;
    }
}