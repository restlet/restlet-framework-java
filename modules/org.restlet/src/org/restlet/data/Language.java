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

package org.restlet.data;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Language used in representations and preferences. A language tag is composed
 * of one or more parts: A primary language tag and a possibly empty series of
 * sub-tags. When formatted as a string, parts are separated by hyphens.
 * 
 * @author Jerome Louvel
 */
public final class Language extends Metadata {
    /** All languages acceptable. */
    public static final Language ALL = new Language("*", "All languages");

    // [ifndef gwt] member
    /**
     * The default language of the JVM.
     * 
     * @see java.util.Locale#getDefault()
     */
    public static final Language DEFAULT = new Language(java.util.Locale
            .getDefault().getLanguage());

    // [ifdef gwt] member uncomment
    // public static final Language DEFAULT = new Language("en",
    // "English language");

    /** English language. */
    public static final Language ENGLISH = new Language("en",
            "English language");

    /** English language spoken in USA. */
    public static final Language ENGLISH_US = new Language("en-us",
            "English language in USA");

    /** French language. */
    public static final Language FRENCH = new Language("fr", "French language");

    /** French language spoken in France. */
    public static final Language FRENCH_FRANCE = new Language("fr-fr",
            "French language in France");

    /** Spanish language. */
    public static final Language SPANISH = new Language("es",
            "Spanish language");

    /**
     * Returns the language associated to a name. If an existing constant exists
     * then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated language.
     */
    public static Language valueOf(final String name) {
        Language result = null;

        if ((name != null) && !name.equals("")) {
            if (name.equalsIgnoreCase(ALL.getName())) {
                result = ALL;
            } else if (name.equalsIgnoreCase(ENGLISH.getName())) {
                result = ENGLISH;
            } else if (name.equalsIgnoreCase(ENGLISH_US.getName())) {
                result = ENGLISH_US;
            } else if (name.equalsIgnoreCase(FRENCH.getName())) {
                result = FRENCH;
            } else if (name.equalsIgnoreCase(FRENCH_FRANCE.getName())) {
                result = FRENCH_FRANCE;
            } else if (name.equalsIgnoreCase(SPANISH.getName())) {
                result = SPANISH;
            } else {
                result = new Language(name);
            }
        }

        return result;
    }

    /** The metadata main list of subtags taken from the metadata name. */
    private volatile List<String> subTags;

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     */
    public Language(final String name) {
        this(name, "Language or range of languages");
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param description
     *            The description.
     */
    public Language(final String name, final String description) {
        super(name, description);
        this.subTags = null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof Language)
                && getName().equalsIgnoreCase(((Language) object).getName());
    }

    @Override
    public Language getParent() {
        Language result = null;

        if ((getSubTags() != null) && !getSubTags().isEmpty()) {
            result = Language.valueOf(getPrimaryTag());
        } else {
            result = equals(ALL) ? null : ALL;
        }

        return result;
    }

    /**
     * Returns the primary tag.
     * 
     * @return The primary tag.
     */
    public String getPrimaryTag() {
        final int separator = getName().indexOf('-');

        if (separator == -1) {
            return getName();
        }

        return getName().substring(0, separator);
    }

    /**
     * Returns the unmodifiable list of subtags. This list can be empty.
     * 
     * @return The list of subtags for this language Tag.
     */
    public List<String> getSubTags() {
        // Lazy initialization with double-check.
        List<String> v = this.subTags;
        if (v == null) {
            synchronized (this) {
                v = this.subTags;
                if (v == null) {
                    List<String> tokens = new CopyOnWriteArrayList<String>();
                    if (getName() != null) {
                        final String[] tags = getName().split("-");
                        if (tags.length > 0) {
                            for (int i = 1; i < tags.length; i++) {
                                tokens.add(tags[i]);
                            }
                        }
                    }

                    this.subTags = v = Collections.unmodifiableList(tokens);
                }
            }
        }
        return v;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }

    /**
     * Indicates if a given language is included in the current one. The test is
     * true if both languages are equal or if the given language is within the
     * range of the current one. For example, ALL includes all languages. A null
     * language is considered as included into the current one.
     * <p>
     * Examples:
     * <ul>
     * <li>ENGLISH.includes(ENGLISH_US) -> true</li>
     * <li>ENGLISH_US.includes(ENGLISH) -> false</li>
     * </ul>
     * 
     * @param included
     *            The language to test for inclusion.
     * @return True if the language type is included in the current one.
     * @see #isCompatible(Metadata)
     */
    public boolean includes(Metadata included) {
        boolean result = equals(ALL) || (included == null) || equals(included);

        if (!result && (included instanceof Language)) {
            Language includedLanguage = (Language) included;

            if (getPrimaryTag().equals(includedLanguage.getPrimaryTag())) {
                // Both languages are different
                if (getSubTags().equals(includedLanguage.getSubTags())) {
                    result = true;
                } else if (getSubTags().isEmpty()) {
                    result = true;
                }
            }
        }

        return result;
    }
}
