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

import org.restlet.data.Reference;
import org.restlet.ext.rdf.RdfN3Representation;

class ListToken extends LexicalUnit {
    List<LexicalUnit> lexicalUnits;

    public ListToken(RdfN3ContentHandler contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        lexicalUnits = new ArrayList<LexicalUnit>();
        this.parse();
    }

    @Override
    public Object resolve() {
        Reference currentBlankNode = new Reference((String) new BlankNodeToken(
                RdfN3ContentHandler.newBlankNodeId()).resolve());
        for (LexicalUnit lexicalUnit : lexicalUnits) {
            Object element = lexicalUnit.resolve();

            if (element instanceof Reference) {
                getContentHandler().link(currentBlankNode,
                        RdfN3Representation.LIST_FIRST, (Reference) element);
            } else if (element instanceof String) {
                getContentHandler().link(currentBlankNode,
                        RdfN3Representation.LIST_FIRST,
                        new Reference((String) element));
            } else {
                // TODO Error.
            }

            Reference restBlankNode = new Reference(
                    (String) new BlankNodeToken(RdfN3ContentHandler
                            .newBlankNodeId()).resolve());

            getContentHandler().link(currentBlankNode,
                    RdfN3Representation.LIST_REST, restBlankNode);
            currentBlankNode = restBlankNode;
        }
        getContentHandler().link(currentBlankNode,
                RdfN3Representation.LIST_REST, RdfN3Representation.OBJECT_NIL);

        setResolved(true);
        return currentBlankNode;
    }

    @Override
    public String getValue() {
        return lexicalUnits.toString();
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
            case ')':
                break;
            case RdfN3ContentHandler.EOF:
                break;
            default:
                lexicalUnits.add(new Token(getContentHandler(), getContext()));
                break;
            }
        } while (getContentHandler().getChar() != RdfN3ContentHandler.EOF
                && getContentHandler().getChar() != ')');
        if (getContentHandler().getChar() == ')') {
            // Set the cursor at the right of the list token.
            getContentHandler().step();
        }
    }
}