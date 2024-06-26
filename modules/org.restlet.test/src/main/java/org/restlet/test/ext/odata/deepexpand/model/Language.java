/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.odata.deepexpand.model;

import java.util.List;

import org.restlet.test.ext.odata.deepexpand.model.Person;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Language {

    private boolean _default;

    private int id;

    private String localeCode;

    private String name;

    private List<Person> preferredByPersons;

    /**
     * Constructor without parameter.
     * 
     */
    public Language() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Language(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "_default" attribute.
     * 
     * @return The value of the "_default" attribute.
     */
    public boolean get_default() {
        return _default;
    }

    /**
     * Returns the value of the "id" attribute.
     * 
     * @return The value of the "id" attribute.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the value of the "localeCode" attribute.
     * 
     * @return The value of the "localeCode" attribute.
     */
    public String getLocaleCode() {
        return localeCode;
    }

    /**
     * Returns the value of the "name" attribute.
     * 
     * @return The value of the "name" attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the "preferredByPersons" attribute.
     * 
     * @return The value of the "preferredByPersons" attribute.
     */
    public List<Person> getPreferredByPersons() {
        return preferredByPersons;
    }

    /**
     * Sets the value of the "_default" attribute.
     * 
     * @param _default
     *            The value of the "_default" attribute.
     */
    public void set_default(boolean _default) {
        this._default = _default;
    }

    /**
     * Sets the value of the "id" attribute.
     * 
     * @param id
     *            The value of the "id" attribute.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the value of the "localeCode" attribute.
     * 
     * @param localeCode
     *            The value of the "localeCode" attribute.
     */
    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    /**
     * Sets the value of the "name" attribute.
     * 
     * @param name
     *            The value of the "name" attribute.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value of the "preferredByPersons" attribute.
     * 
     * @param preferredByPersons
     *            " The value of the "preferredByPersons" attribute.
     */
    public void setPreferredByPersons(List<Person> preferredByPersons) {
        this.preferredByPersons = preferredByPersons;
    }

}