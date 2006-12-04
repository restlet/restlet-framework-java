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

package org.restlet.data;

/**
 * Language used in representations and preferences.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Language extends Metadata {
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

    /**
     * Returns the language associated to a name. If an existing constant exists
     * then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated language.
     */
    public static Language valueOf(String name) {
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
    public Language(String name) {
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
    public Language(String name, String description) {
        super(name, description);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        return (object instanceof Language)
                && getName().equalsIgnoreCase(((Language) object).getName());
    }

    /**
     * Returns the main tag.
     * 
     * @return The main tag.
     */
    public String getMainTag() {
        int separator = getName().indexOf('-');

        if (separator == -1) {
            return getName();
        } else {
            return getName().substring(0, separator);
        }
    }

    /**
     * Returns the sub tag.
     * 
     * @return The sub tag.
     */
    public String getSubTag() {
        int separator = getName().indexOf('-');

        if (separator == -1) {
            return null;
        } else {
            return getName().substring(separator + 1);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (getName() == null) ? 0 : getName().toLowerCase().hashCode();
    }
}
