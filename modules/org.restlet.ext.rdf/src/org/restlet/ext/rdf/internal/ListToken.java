/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

class ListToken extends LexicalUnit {
    List<LexicalUnit> lexicalUnits;

    public ListToken(RdfN3ContentHandler contentHandler) {
        super(contentHandler);
        lexicalUnits = new ArrayList<LexicalUnit>();
    }

    @Override
    public String getValue() {
        return lexicalUnits.toString();
    }

    @Override
    public void parse() throws IOException {
        int c = getContentHandler().step();
        while (c != RdfN3ContentHandler.EOF) {
            if (c == '(') {
                LexicalUnit unit = new ListToken(getContentHandler());
                unit.parse();
                lexicalUnits.add(unit);
            } else if (c == '<') {
                getContentHandler().stepBack();
                LexicalUnit uriToken = new UriToken(getContentHandler());
                uriToken.parse();
                lexicalUnits.add(uriToken);
            } else if (c == '[') {
                LexicalUnit unit = new BlankNodeToken(getContentHandler());
                unit.parse();
                lexicalUnits.add(unit);
            } else if (c == '{') {
                LexicalUnit unit = new FormulaToken(getContentHandler());
                unit.parse();
                lexicalUnits.add(unit);
            } else if (c == '_') {
                getContentHandler().step();
                getContentHandler().discard();
                BlankNodeToken unit = new BlankNodeToken(getContentHandler());
                unit.setValue(getContentHandler().parseToken());
                System.out.println("BlankNode" + unit.getValue());
                lexicalUnits.add(unit);
            } else if (RdfN3ContentHandler.isAlphaNum(c)) {
                Token unit = new Token(getContentHandler());
                unit.parse();
                System.out.println("unit.getToken()=" + unit.getValue());
                lexicalUnits.add(unit);
            } else if (RdfN3ContentHandler.isWhiteSpace(c)) {
                getContentHandler().discard();
            } else if (c == '!') {
                Token unit = new Token(getContentHandler());
                unit.setValue("!");
                System.out.println("unit.getToken()=" + unit.getValue());
                lexicalUnits.add(unit);
            } else if (c == '^') {
                Token unit = new Token(getContentHandler());
                unit.setValue("^");
                System.out.println("unit.getToken()=" + unit.getValue());
                lexicalUnits.add(unit);
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

        for (LexicalUnit lexicalUnit : lexicalUnits) {
            System.out.print("lexicalUnit " + lexicalUnit.getClass());
            System.out.println(" => value " + lexicalUnit.getValue());
        }

        setParsed(c != RdfN3ContentHandler.EOF);
    }
}