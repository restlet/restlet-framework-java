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
import org.restlet.data.Reference;
import org.restlet.resource.Representation;

/**
 * Either contains or links to the content of the entry.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Content {
    /** Representation for inline content. */
    private Representation inlineContent;

    /** Reference to the external representation. */
    private Reference externalRef;

    /** Expected media type of the external content. */
    private MediaType externalType;

    /**
     * Constructor.
     */
    public Content() {
        this.inlineContent = null;
        this.externalRef = null;
        this.externalType = null;
    }

    /**
     * Indicates if the content is available inline.
     * 
     * @return True if the content is available inline.
     */
    public boolean isInline() {
        return (this.inlineContent != null);
    }

    /**
     * Indicates if the content is available externally.
     * 
     * @return True if the content is available externally.
     */
    public boolean isExternal() {
        return (this.externalRef != null);
    }

    /**
     * Returns the representation for inline content.
     * 
     * @return The representation for inline content.
     */
    public Representation getInlineContent() {
        return this.inlineContent;
    }

    /**
     * Sets the representation for inline content.
     * 
     * @param inlineContent
     *            The representation for inline content.
     */
    public void setInlineContent(Representation inlineContent) {
        this.inlineContent = inlineContent;
    }

    /**
     * Returns the reference to the external representation.
     * 
     * @return The reference to the external representation.
     */
    public Reference getExternalRef() {
        return this.externalRef;
    }

    /**
     * Sets the reference to the external representation.
     * 
     * @param externalRef
     *            The reference to the external representation.
     */
    public void setExternalRef(Reference externalRef) {
        this.externalRef = externalRef;
    }

    /**
     * Returns the expected media type of the external content.
     * 
     * @return The expected media type of the external content.
     */
    public MediaType getExternalType() {
        return this.externalType;
    }

    /**
     * Sets the expected media type of the external content.
     * 
     * @param externalType
     *            The expected media type of the external content.
     */
    public void setExternalType(MediaType externalType) {
        this.externalType = externalType;
    }

}
