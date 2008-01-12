/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.ext.atom;

import org.restlet.data.MediaType;

/**
 * A Text construct contains human-readable text, usually in small quantities.
 * The content of Text constructs is Language-Sensitive.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Text {
    /**
     * The content type.
     */
    private MediaType type;

    /**
     * The content.
     */
    private String content;

    /**
     * Constructor.
     * 
     * @param type
     *            The content type.
     * @param content
     *            The content.
     */
    public Text(MediaType type, String content) {
        this.type = type;
        this.content = content;
    }

    /**
     * Constructor.
     * 
     * @param type
     *            The content type.
     */
    public Text(MediaType type) {
        this(type, null);
    }

    /**
     * Returns the content type.
     * 
     * @return The content type.
     */
    public MediaType getType() {
        return this.type;
    }

    /**
     * Sets the content type.
     * 
     * @param type
     *            The content type.
     */
    public void setType(MediaType type) {
        this.type = type;
    }

    /**
     * Returns the content.
     * 
     * @return The content.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Sets the content.
     * 
     * @param content
     *            The content.
     */
    public void setContent(String content) {
        this.content = content;
    }

}
