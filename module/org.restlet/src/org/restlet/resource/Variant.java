/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.resource;

import java.util.Date;
import java.util.logging.Logger;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.util.DateUtils;

/**
 * Descriptor for available representations of a resource. It containts all the
 * important metadata about a representation but is not able to actually serve
 * the representation's content itself. For this you need to use the
 * Representation subclass.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Variant extends Resource {
    /**
     * Inidicates that the size of the representation can't be known in advance.
     */
    public static final long UNKNOWN_SIZE = -1L;

    /** The character set or null if not applicable. */
    private CharacterSet characterSet;

    /** The encoding or null if not identity encoding applies. */
    private Encoding encoding;

    /**
     * The expected size. Dynamic representations can have any size, but
     * sometimes we can know in advance the expected size. If this expected size
     * is specified by the user, it has a higher priority than any size that can
     * be guessed by the representation (like a file size).
     */
    private long size;

    /** The expiration date. */
    private Date expirationDate;

    /** The language or null if not applicable. */
    private Language language;

    /** The media type. */
    private MediaType mediaType;

    /** The modification date. */
    private Date modificationDate;

    /** The tag. */
    private Tag tag;

    /**
     * Default constructor.
     */
    public Variant() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     */
    public Variant(MediaType mediaType) {
        super((Logger) null);
        this.characterSet = null;
        this.encoding = null;
        this.size = UNKNOWN_SIZE;
        this.expirationDate = null;
        this.language = null;
        this.mediaType = mediaType;
        this.modificationDate = null;
        this.tag = null;

        // A representation is also a resource whose only
        // variant is the representation itself
        if ((getVariants() != null) && !getVariants().contains(this)) {
            getVariants().add(this);
        }
    }

    /**
     * Returns the character set or null if not applicable.
     * 
     * @return The character set or null if not applicable.
     */
    public CharacterSet getCharacterSet() {
        return this.characterSet;
    }

    /**
     * Returns the encoding or null if identity encoding applies.
     * 
     * @return The encoding or null if identity encoding applies.
     */
    public Encoding getEncoding() {
        return this.encoding;
    }

    /**
     * Returns the future date when this representation expire. If this
     * information is not known, returns null.
     * 
     * @return The expiration date.
     */
    public Date getExpirationDate() {
        return this.expirationDate;
    }

    /**
     * Returns the language or null if not applicable.
     * 
     * @return The language or null if not applicable.
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Returns the media type.
     * 
     * @return The media type.
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }

    /**
     * Returns the last date when this representation was modified. If this
     * information is not known, returns null.
     * 
     * @return The modification date.
     */
    public Date getModificationDate() {
        return this.modificationDate;
    }

    /**
     * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     */
    public long getSize() {
        return this.size;
    }

    /**
     * Returns the tag.
     * 
     * @return The tag.
     */
    public Tag getTag() {
        return this.tag;
    }

    /**
     * Sets the character set or null if not applicable.
     * 
     * @param characterSet
     *            The character set or null if not applicable.
     */
    public void setCharacterSet(CharacterSet characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * Sets the encoding or null if identity encoding applies.
     * 
     * @param encoding
     *            The encoding or null if identity encoding applies.
     */
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the future date when this representation expire. If this information
     * is not known, pass null.
     * 
     * @param expirationDate
     *            The expiration date.
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = DateUtils.unmodifiable(expirationDate);
    }

    /**
     * Sets the language or null if not applicable.
     * 
     * @param language
     *            The language or null if not applicable.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Sets the media type.
     * 
     * @param mediaType
     *            The media type.
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Sets the last date when this representation was modified. If this
     * information is not known, pass null.
     * 
     * @param modificationDate
     *            The modification date.
     */
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = DateUtils.unmodifiable(modificationDate);
    }

    /**
     * Sets the expected size in bytes if known, -1 otherwise.
     * 
     * @param expectedSize
     *            The expected size in bytes if known, -1 otherwise.
     */
    public void setSize(long expectedSize) {
        this.size = expectedSize;
    }

    /**
     * Sets the tag.
     * 
     * @param tag
     *            The tag.
     */
    public void setTag(Tag tag) {
        this.tag = tag;
    }

}
