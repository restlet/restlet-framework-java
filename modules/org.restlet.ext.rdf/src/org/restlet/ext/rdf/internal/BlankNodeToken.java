/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import org.restlet.ext.rdf.RdfN3Representation;

public class BlankNodeToken extends LexicalUnit {
    List<Arc> arcs;

    public BlankNodeToken(RdfN3ContentHandler contentHandler) {
        super(contentHandler);
        arcs = new ArrayList<Arc>();
    }

    public void addArc(BlankNodeToken element, String predicate, boolean subject) {
        arcs.add(new Arc(element, predicate, subject));
    }

    public void addArc(String predicate, String element, boolean subject) {
        arcs.add(new Arc(element, predicate, subject));
    }

    @Override
    public void parse() throws IOException {
        int cpt = 0;
        int c = getContentHandler().step();
        while (c != RdfN3ContentHandler.EOF && c != ']') {
            LexicalUnit unit = null;
            if (c == '(') {
                unit = new ListToken(getContentHandler());
                unit.parse();
            } else if (c == '<') {
                getContentHandler().stepBack();
                LexicalUnit uriToken = new UriToken(getContentHandler());
                uriToken.parse();
            } else if (c == '[') {
                unit = new BlankNodeToken(getContentHandler());
                unit.parse();
            } else if (c == '{') {
                unit = new FormulaToken(getContentHandler());
                unit.parse();
            } else if (c == '_') {
                getContentHandler().step();
                getContentHandler().discard();
                unit = new BlankNodeToken(getContentHandler());
                unit.setValue(getContentHandler().parseToken());
            } else if (RdfN3ContentHandler.isAlphaNum(c)) {
                unit = new Token(getContentHandler());
                unit.parse();
            } else if (RdfN3ContentHandler.isWhiteSpace(c)) {
                getContentHandler().discard();
            } else if (c == '!') {
                unit = new Token(getContentHandler());
                unit.setValue("!");
            } else if (c == '^') {
                unit = new Token(getContentHandler());
                unit.setValue("^");
            } else if (c == ';') {
                getContentHandler().discard();
                // End of the statement
            } else if (c == ',') {
                getContentHandler().discard();
                // End of the statement
            } else if (c == '.') {
                getContentHandler().discard();
                // End of the statement
            }
            c = getContentHandler().step();
        }
        setValue(getContentHandler().getCurrentToken());
        setParsed(c != RdfN3ContentHandler.EOF);
    }
}