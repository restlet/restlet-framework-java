/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.representation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.util.WrapperList;

/**
 * Descriptor for available representations of a resource. It contains all the
 * important metadata about a representation but is not able to actually serve
 * the representation's content itself.<br>
 * <br>
 * For this, you need to use the Representation subclass.
 * 
 * @author Jerome Louvel
 */
public class Variant {

    /** The character set or null if not applicable. */
    private volatile CharacterSet characterSet;

    /** The additional content codings applied to the entity-body. */
    private volatile List<Encoding> encodings;

    /** The identifier. */
    private volatile Reference identifier;

    /** The natural language(s) of the intended audience for this variant. */
    private volatile List<Language> languages;

    /** The media type. */
    private volatile MediaType mediaType;

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
        this.characterSet = null;
        this.encodings = null;
        this.languages = null;
        this.mediaType = mediaType;
        this.identifier = null;
    }

    /**
     * Creates a {@link ClientInfo} instance with preferences matching exactly
     * the current variant.
     * 
     * @return The new {@link ClientInfo} instance.
     */
    public ClientInfo createClientInfo() {
        ClientInfo result = new ClientInfo();

        if (getCharacterSet() != null) {
            result.getAcceptedCharacterSets().add(
                    new Preference<CharacterSet>(getCharacterSet()));
        }

        if (getEncodings() != null) {
            for (Encoding encoding : getEncodings()) {
                result.getAcceptedEncodings().add(
                        new Preference<Encoding>(encoding));
            }
        }

        if (getLanguages() != null) {
            for (Language language : getLanguages()) {
                result.getAcceptedLanguages().add(
                        new Preference<Language>(language));
            }
        }

        if (getMediaType() != null) {
            result.getAcceptedMediaTypes().add(
                    new Preference<MediaType>(getMediaType()));
        }

        return result;
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
     * Returns the modifiable list of encodings applied to the entity-body.
     * Creates a new instance if no one has been set. An
     * "IllegalArgumentException" exception is thrown when adding a null
     * encoding to this list.
     * 
     * @return The list of encodings applied to the entity-body.
     */
    public List<Encoding> getEncodings() {
        if (this.encodings == null) {
            this.encodings = new WrapperList<Encoding>() {

                @Override
                public boolean add(Encoding element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    return super.add(element);
                }

                @Override
                public void add(int index, Encoding element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    super.add(index, element);
                }

                @Override
                public boolean addAll(Collection<? extends Encoding> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (final Iterator<? extends Encoding> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    return super.addAll(elements);
                }

                @Override
                public boolean addAll(int index,
                        Collection<? extends Encoding> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (final Iterator<? extends Encoding> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    return super.addAll(index, elements);
                }
            };
        }

        return this.encodings;
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
     * Returns the modifiable list of languages. Creates a new instance if no
     * one has been set. An "IllegalArgumentException" exception is thrown when
     * adding a null language to this list.
     * 
     * @return The list of languages.
     */
    public List<Language> getLanguages() {
        if (this.languages == null) {
            this.languages = new WrapperList<Language>() {

                @Override
                public void add(int index, Language element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    }

                    super.add(index, element);
                }

                @Override
                public boolean add(Language element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    }

                    return super.add(element);
                }

                @Override
                public boolean addAll(Collection<? extends Language> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (final Iterator<? extends Language> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    }

                    return super.addAll(elements);
                }

                @Override
                public boolean addAll(int index,
                        Collection<? extends Language> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (final Iterator<? extends Language> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null language.");
                    }

                    return super.addAll(index, elements);
                }

            };
        }
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
     * Sets the character set or null if not applicable.
     * 
     * @param characterSet
     *            The character set or null if not applicable.
     */
    public void setCharacterSet(CharacterSet characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * Sets the list of encodings applied to the entity-body.
     * 
     * @param encodings
     *            The list of encodings applied to the entity-body.
     */
    public void setEncodings(List<Encoding> encodings) {
        this.encodings = encodings;
    }

    /**
     * Sets the optional identifier. This is useful when the representation is
     * accessible from a location separate from the representation's resource
     * URI, for example when content negotiation occurs.
     * 
     * @param identifier
     *            The identifier.
     */
    public void setIdentifier(Reference identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the identifier from a URI string.
     * 
     * @param identifierUri
     *            The identifier to parse.
     */
    public void setIdentifier(String identifierUri) {
        setIdentifier(new Reference(identifierUri));
    }

    /**
     * Sets the list of languages.
     * 
     * @param languages
     *            The list of languages.
     */
    public void setLanguages(List<Language> languages) {
        this.languages = languages;
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
     * Indicates if the current variant is compatible with the given variant.
     * 
     * @param other
     *            The other variant.
     * @return True if the current variant includes the other.
     */
    public boolean isCompatible(Variant other) {
        boolean result = true;

        // Compare the character set
        if (result) {
            result = (getCharacterSet() == null)
                    || getCharacterSet().equals(CharacterSet.ALL)
                    || getCharacterSet().equals(other.getCharacterSet());
        }

        // Compare the media type
        if (result) {
            result = (getMediaType() == null)
                    || getMediaType().isCompatible(other.getMediaType());
        }

        // Compare the languages
        if (result) {
            result = (getLanguages().isEmpty())
                    || getLanguages().contains(Language.ALL)
                    || getLanguages().containsAll(other.getLanguages());
        }

        return result;
    }
}
