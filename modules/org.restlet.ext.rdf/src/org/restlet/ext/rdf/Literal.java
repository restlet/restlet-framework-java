/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.rdf;

import org.restlet.data.Language;
import org.restlet.data.Reference;

/**
 * Literal as defined by RDF. Composed of the literal value, optional datatype
 * reference and language properties.
 * 
 * @author Jerome Louvel
 * @see <a href="http://www.w3.org/TR/rdf-concepts/#section-Graph-Literal">RDF
 *      literals</a>
 */
public class Literal {

    /** The optional datatype reference. */
    private Reference datatypeRef;

    /** The optional language. */
    private Language language;

    /** The value. */
    private String value;

    /**
     * Constructor.
     * 
     * @param value
     *            The value.
     */
    public Literal(String value) {
        this(value, null, null);
    }

    /**
     * Constructor.
     * 
     * @param value
     *            The value.
     * @param datatypeRef
     *            The optional datatype reference.
     */
    public Literal(String value, Reference datatypeRef) {
        this(value, datatypeRef, null);
    }

    /**
     * Constructor.
     * 
     * @param value
     *            The value.
     * @param datatypeRef
     *            The optional datatype reference.
     * @param language
     *            The optional language.
     */
    public Literal(String value, Reference datatypeRef, Language language) {
        this.value = value;
        this.datatypeRef = datatypeRef;
        this.language = language;
    }

    /**
     * Returns the optional datatype reference.
     * 
     * @return The datatype reference or null.
     */
    public Reference getDatatypeRef() {
        return datatypeRef;
    }

    /**
     * Returns the optional language.
     * 
     * @return The language or null.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Indicates if the literal is plain. Plain literals have a value and an
     * optional language tag.
     * 
     * @return True if the literal is plain.
     */
    public boolean isPlain() {
        return (getValue() != null) && (getDatatypeRef() == null);
    }

    /**
     * Indicates if the literal is types. Typed literals have a value and a
     * datatype reference.
     * 
     * @return True if the literal is typed.
     */
    public boolean isTyped() {
        return (getValue() != null) && (getDatatypeRef() != null);
    }

    /**
     * Sets the datatype reference.
     * 
     * @param datatypeRef
     *            The datatype reference.
     */
    public void setDatatypeRef(Reference datatypeRef) {
        this.datatypeRef = datatypeRef;
    }

    /**
     * Sets the language.
     * 
     * @param language
     *            The language.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
