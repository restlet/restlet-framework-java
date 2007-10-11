/*
 * Copyright 2005-2007 Noelios Consulting.
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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Tag;
import org.restlet.util.DateUtils;
import org.restlet.util.WrapperList;

/**
 * Descriptor for available representations of a resource. It contains all the
 * important metadata about a representation but is not able to actually serve
 * the representation's content itself. For this, you need to use the
 * Representation subclass.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Variant {
    /**
     * Indicates that the size of the representation can't be known in advance.
     * 
     * @deprecated Use the {@link Representation#UNKNOWN_SIZE} constant instead.
     */
    public static final long UNKNOWN_SIZE = -1L;

    /** The natural language(s) of the intended audience for this variant. */
    private List<Language> languages;

    /** The media type. */
    private MediaType mediaType;

    /** The character set or null if not applicable. */
    private CharacterSet characterSet;

    /** The additional content codings applied to the entity-body. */
    private List<Encoding> encodings;

    /**
     * The expected size. Dynamic representations can have any size, but
     * sometimes we can know in advance the expected size. If this expected size
     * is specified by the user, it has a higher priority than any size that can
     * be guessed by the representation (like a file size).
     */
    private long size;

    /** The expiration date. */
    private Date expirationDate;

    /** The modification date. */
    private Date modificationDate;

    /** The tag. */
    private Tag tag;

    /**
     * The identifier.
     */
    private Reference identifier;

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
     *                The media type.
     */
    public Variant(MediaType mediaType) {
        this.characterSet = null;
        this.encodings = null;
        this.size = UNKNOWN_SIZE;
        this.expirationDate = null;
        this.languages = null;
        this.mediaType = mediaType;
        this.modificationDate = null;
        this.tag = null;
        this.identifier = null;
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
     * Returns the list of encodings applied to the entity-body. Creates a new
     * instance if no one has been set. An "IllegalArgumentException" exception
     * is thrown when adding a null encoding to this list.
     * 
     * @return The list of encodings applied to the entity-body.
     */
    public List<Encoding> getEncodings() {
        if (this.encodings == null) {
            encodings = new WrapperList<Encoding>() {

                @Override
                public boolean add(Encoding element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    } else {
                        return super.add(element);
                    }
                }

                @Override
                public void add(int index, Encoding element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    } else {
                        super.add(index, element);
                    }
                }

                @Override
                public boolean addAll(Collection<? extends Encoding> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (Iterator<? extends Encoding> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    } else {
                        return super.addAll(elements);
                    }
                }

                @Override
                public boolean addAll(int index,
                        Collection<? extends Encoding> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (Iterator<? extends Encoding> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    } else {
                        return super.addAll(index, elements);
                    }
                }
            };
        }

        return this.encodings;
    }

    /**
     * Returns the future date when this representation expire. If this
     * information is not known, returns null.
     * 
     * @return The expiration date.
     * @deprecated Use the {@link Representation#getExpirationDate()} method
     *             instead.
     */
    @Deprecated
    public Date getExpirationDate() {
        return this.expirationDate;
    }

    /**
     * Returns an optional identifier. This is useful when the representation is
     * accessible from a location separate from the representation's resource
     * URI, for example when content negotiation occurs.
     * 
     * @return The identifier.
     */
    public Reference getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the list of languages. Creates a new instance if no one has been
     * set. An "IllegalArgumentException" exception is thrown when adding a null
     * language to this list.
     * 
     * @return The list of languages.
     */
    public List<Language> getLanguages() {
        if (languages == null)
            languages = new WrapperList<Language>() {

                @Override
                public void add(int index, Language element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    } else {
                        super.add(index, element);
                    }
                }

                @Override
                public boolean add(Language element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    } else {
                        return super.add(element);
                    }
                }

                @Override
                public boolean addAll(Collection<? extends Language> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (Iterator<? extends Language> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    } else {
                        return super.addAll(elements);
                    }
                }

                @Override
                public boolean addAll(int index,
                        Collection<? extends Language> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (Iterator<? extends Language> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    } else {
                        return super.addAll(index, elements);
                    }
                }

            };
        return this.languages;
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
     * @deprecated Use the {@link Representation#getModificationDate()} method
     *             instead.
     */
    @Deprecated
    public Date getModificationDate() {
        return this.modificationDate;
    }

    /**
     * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     * @deprecated Use the {@link Representation#getSize()} method instead.
     */
    @Deprecated
    public long getSize() {
        return this.size;
    }

    /**
     * Returns the tag.
     * 
     * @return The tag.
     * @deprecated Use the {@link Representation#getTag()} method instead.
     */
    @Deprecated
    public Tag getTag() {
        return this.tag;
    }

    /**
     * Sets the character set or null if not applicable.
     * 
     * @param characterSet
     *                The character set or null if not applicable.
     */
    public void setCharacterSet(CharacterSet characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * Sets the list of encodings applied to the entity-body.
     * 
     * @param encodings
     *                The list of encodings applied to the entity-body.
     */
    public void setEncodings(List<Encoding> encodings) {
        this.encodings = encodings;
    }

    /**
     * Sets the future date when this representation expire. If this information
     * is not known, pass null.
     * 
     * @param expirationDate
     *                The expiration date.
     * @deprecated Use the {@link Representation#setExpirationDate(Date)} method
     *             instead.
     */
    @Deprecated
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = DateUtils.unmodifiable(expirationDate);
    }

    /**
     * Sets the optional identifier. This is useful when the representation is
     * accessible from a location separate from the representation's resource
     * URI, for example when content negotiation occurs.
     * 
     * @param identifier
     *                The identifier.
     */
    public void setIdentifier(Reference identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the identifier from a URI string.
     * 
     * @param identifierUri
     *                The identifier to parse.
     */
    public void setIdentifier(String identifierUri) {
        setIdentifier(new Reference(identifierUri));
    }

    /**
     * Sets the list of languages.
     * 
     * @param languages
     *                The list of languages.
     */
    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    /**
     * Sets the media type.
     * 
     * @param mediaType
     *                The media type.
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Sets the last date when this representation was modified. If this
     * information is not known, pass null.
     * 
     * @param modificationDate
     *                The modification date.
     * @deprecated Use the {@link Representation#setModificationDate(Date)}
     *             method instead.
     */
    @Deprecated
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = DateUtils.unmodifiable(modificationDate);
    }

    /**
     * Sets the expected size in bytes if known, -1 otherwise.
     * 
     * @param expectedSize
     *                The expected size in bytes if known, -1 otherwise.
     * @deprecated Use the {@link Representation#setSize(long)} method instead.
     */
    @Deprecated
    public void setSize(long expectedSize) {
        this.size = expectedSize;
    }

    /**
     * Sets the tag.
     * 
     * @param tag
     *                The tag.
     * @deprecated Use the {@link Representation#setTag(Tag)} method instead.
     */
    @Deprecated
    public void setTag(Tag tag) {
        this.tag = tag;
    }

}
