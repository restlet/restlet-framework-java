/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.representation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.engine.util.SystemUtils;
import org.restlet.util.WrapperList;

/**
 * Descriptor for available representations of a resource. It contains all the
 * important metadata about a representation but is not able to actually serve
 * the representation's content itself.<br>
 * <br>
 * For this, you need to use on of the {@link Representation} subclasses.
 * 
 * @author Jerome Louvel
 */
public class Variant {

    /** The character set or null if not applicable. */
    private volatile CharacterSet characterSet;

    /** The additional content codings applied to the entity-body. */
    private volatile List<Encoding> encodings;

    /** The location reference. */
    private volatile Reference locationRef;

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
        this(mediaType, null);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     * @param language
     *            The language.
     */
    public Variant(MediaType mediaType, Language language) {
        this.characterSet = null;
        this.encodings = null;

        if (language != null) {
            getLanguages().add(language);
        } else {
            this.languages = null;
        }

        this.mediaType = mediaType;
        this.locationRef = null;
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
     * Indicates if the current variant is equal to the given variant.
     * 
     * @param other
     *            The other variant.
     * @return True if the current variant includes the other.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Variant)) {
            return false;
        }

        Variant that = (Variant) other;

        return Objects.equals(getCharacterSet(), that.getCharacterSet())
                && Objects.equals(getMediaType(), that.getMediaType())
                && getLanguages().equals(that.getLanguages())
                && getEncodings().equals(that.getEncodings())
                && Objects.equals(getLocationRef(), that.getLocationRef());
    }

    /**
     * Returns the character set or null if not applicable. Note that when used
     * with HTTP connectors, this property maps to the "Content-Type" header.
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
     * encoding to this list.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Encoding" header.
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
     * Returns the modifiable list of languages. Creates a new instance if no
     * one has been set. An "IllegalArgumentException" exception is thrown when
     * adding a null language to this list.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Language" header.
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
     * Returns an optional location reference. This is useful when the
     * representation is accessible from a location separate from the
     * representation's resource URI, for example when content negotiation
     * occurs.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Location" header.
     * 
     * @return The identifier.
     */
    public Reference getLocationRef() {
        return this.locationRef;
    }

    /**
     * Returns the media type.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Type" header.
     * 
     * @return The media type.
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }
    
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(super.hashCode(), characterSet, encodings, locationRef, languages, mediaType);
    }

    /**
     * Indicates if the current variant includes the given variant.
     * 
     * @param other
     *            The other variant.
     * @return True if the current variant includes the other.
     */
    public boolean includes(Variant other) {
        boolean result = other != null;

        // Compare the character set
        if (result) {
            result = (getCharacterSet() == null)
                    || getCharacterSet().includes(other.getCharacterSet());
        }

        // Compare the media type
        if (result) {
            result = (getMediaType() == null)
                    || getMediaType().includes(other.getMediaType());
        }

        // Compare the languages
        if (result) {
            result = (getLanguages().isEmpty())
                    || getLanguages().contains(Language.ALL)
                    || getLanguages().containsAll(other.getLanguages());
        }

        // Compare the encodings
        if (result) {
            result = (getEncodings().isEmpty())
                    || getEncodings().contains(Encoding.ALL)
                    || getEncodings().containsAll(other.getEncodings());
        }

        return result;
    }

    /**
     * Indicates if the current variant is compatible with the given variant.
     * 
     * @param other
     *            The other variant.
     * @return True if the current variant is compatible with the other.
     */
    public boolean isCompatible(Variant other) {
        return (other != null) && (includes(other) || other.includes(this));
    }

    /**
     * Sets the character set or null if not applicable.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Type" header.
     * 
     * @param characterSet
     *            The character set or null if not applicable.
     */
    public void setCharacterSet(CharacterSet characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * Sets the list of encodings applied to the entity-body.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Encoding" header.
     * 
     * @param encodings
     *            The list of encodings applied to the entity-body.
     */
    public void setEncodings(List<Encoding> encodings) {
        this.encodings = encodings;
    }

    /**
     * Sets the list of languages.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Language" header.
     * 
     * @param languages
     *            The list of languages.
     */
    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    /**
     * Sets the optional identifier. This is useful when the representation is
     * accessible from a location separate from the representation's resource
     * URI, for example when content negotiation occurs.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Location" header.
     * 
     * @param location
     *            The location reference.
     */
    public void setLocationRef(Reference location) {
        this.locationRef = location;
    }

    /**
     * Sets the identifier from a URI string.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Location" header.
     * 
     * @param locationUri
     *            The location URI to parse.
     */
    public void setLocationRef(String locationUri) {
        setLocationRef(new Reference(locationUri));
    }

    /**
     * Sets the media type.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Content-Type" header.
     * 
     * @param mediaType
     *            The media type.
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        if (getMediaType() != null) {
            first = false;
            sb.append(getMediaType());
        }

        if (getCharacterSet() != null) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }

            sb.append(getCharacterSet());
        }

        if (!getLanguages().isEmpty()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }

            sb.append(getLanguages());
        }

        if (!getEncodings().isEmpty()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }

            sb.append(getEncodings());
        }

        sb.append("]");
        return sb.toString();
    }
}
