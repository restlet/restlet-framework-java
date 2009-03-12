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

class StringToken extends LexicalUnit {

    private boolean multiLines;

    public StringToken(RdfN3ContentHandler contentHandler, Context context)
            throws IOException {
        super(contentHandler, context);
        multiLines = false;
        this.parse();
    }

    public boolean isMultiLines() {
        return multiLines;
    }

    @Override
    public void parse() throws IOException {
        // Answer the question : is it multi lines or not?
        // That is to say, is it delimited by 3 quotes or not?
        int c1 = getContentHandler().step();
        int c2 = getContentHandler().step();

        if ((c1 == c2) && (c1 == '"')) {
            multiLines = true;
            getContentHandler().step();
            getContentHandler().discard();
            int[] tab = new int[3];
            int cpt = 0; // Number of consecutives '"' characters.
            int c = getContentHandler().getChar();
            while (c != RdfN3ContentHandler.EOF) {
                if (c == '"') {
                    tab[++cpt - 1] = c;
                } else {
                    cpt = 0;
                }
                if (cpt == 3) {
                    // End of the string reached.
                    getContentHandler().stepBack(2);
                    setValue(getContentHandler().getCurrentToken());
                    getContentHandler().step(3);
                    getContentHandler().discard();
                    break;
                }
                c = getContentHandler().step();
            }
        } else {
            multiLines = false;
            getContentHandler().stepBack(1);
            getContentHandler().discard();
            int c = getContentHandler().getChar();
            while (c != RdfN3ContentHandler.EOF && (c != '"')) {
                c = getContentHandler().step();
            }
            setValue(getContentHandler().getCurrentToken());
            getContentHandler().step();
            getContentHandler().discard();
        }
        System.out.println("String =" + getValue() + "=");

    }
}