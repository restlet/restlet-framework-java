/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
