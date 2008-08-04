/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.internal.util;

/**
 * Line reader.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class LineReader extends CharacterReader {

    private int savedNextChar;

    /**
     * Constructor.
     * 
     * @param text
     *            The source text to read.
     */
    public LineReader(String text) {
        super(text);
        this.savedNextChar = -2;
    }

    private int getNextChar() {
        int result = -1;

        if (this.savedNextChar != -2) {
            result = this.savedNextChar;
            this.savedNextChar = -2;
        } else {
            result = read();
        }

        return result;
    }

    public String readLine() {
        StringBuilder sb = null;
        boolean eol = false;
        int nextChar = getNextChar();

        while (!eol && (nextChar != -1)) {
            if (nextChar == 10) {
                eol = true;
            } else if (nextChar == 13) {
                eol = true;

                // Check if there is a immediate LF following the CR
                nextChar = getNextChar();
                if (nextChar != 10) {
                    setSavedNextChar(nextChar);
                }
            }

            if (!eol) {
                if (sb == null) {
                    sb = new StringBuilder();
                }

                sb.append((char) nextChar);
                nextChar = getNextChar();
            }

        }

        return (sb == null) ? null : sb.toString();
    }

    private void setSavedNextChar(int nextChar) {
        this.savedNextChar = nextChar;
    }

}
