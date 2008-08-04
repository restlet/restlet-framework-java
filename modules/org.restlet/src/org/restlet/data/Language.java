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

package org.restlet.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Language used in representations and preferences. A language tag is composed
 * of one or more parts: A primary language tag and a possibly empty series of
 * subtags. When formatted as a string, parts are separated by hyphens.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class Language extends Metadata {
    /** All languages acceptable. */
    public static final Language ALL = new Language("*", "All languages");

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

    /** The metadata main list of subtags taken from the metadata name. */
    private List<String> subTags;

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

        if (name != null) {
            if (name.equalsIgnoreCase(ALL.getName()))
                result = ALL;
            else if (name.equalsIgnoreCase(ENGLISH.getName()))
                result = ENGLISH;
            else if (name.equalsIgnoreCase(ENGLISH_US.getName()))
                result = ENGLISH_US;
            else if (name.equalsIgnoreCase(FRENCH.getName()))
                result = FRENCH;
            else if (name.equalsIgnoreCase(FRENCH_FRANCE.getName()))
                result = FRENCH_FRANCE;
            else if (name.equalsIgnoreCase(SPANISH.getName()))
                result = SPANISH;
            else
                result = new Language(name);
        }

        return result;
    }

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

    /**
     * Returns the primary tag.
     * 
     * @return The primary tag.
     */
    public String getPrimaryTag() {
        int separator = getName().indexOf('-');

        if (separator == -1) {
            return getName();
        } else {
            return getName().substring(0, separator);
        }
    }

    /**
     * Returns the possibly empty list of subtags.
     * 
     * @return The list of subtags for this language Tag.
     */
    public List<String> getSubTags() {
        if (subTags == null) {
            String[] tags = getName().split("-");
            subTags = new ArrayList<String>();

            if (tags.length > 0) {
                for (int i = 1; i < tags.length; i++) {
                    subTags.add(tags[i]);
                }
            }
        }

        return subTags;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }
}
