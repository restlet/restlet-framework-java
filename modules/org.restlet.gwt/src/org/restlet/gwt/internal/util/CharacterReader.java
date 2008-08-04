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
 * Character reader.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class CharacterReader {

    /** The text to read. */
    private final String text;

    /** The next position to read. */
    private int position;

    /**
     * Constructor.
     * 
     * @param text
     *            The source text to read.
     */
    public CharacterReader(String text) {
        this.text = text;
        this.position = 0;
    }

    /**
     * Reads the next character in the source text.
     * 
     * @return The next character or -1 if end of text is reached.
     */
    public int read() {
        return (this.position == this.text.length()) ? -1 : this.text
                .charAt(this.position++);
    }

}
