/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.test.ext.odata.deepexpand.model;

import org.restlet.test.ext.odata.deepexpand.model.Location;
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
public class Address {

    private String city;

    private String country;

    private int id;

    private String number;

    private String poBox;

    private String street;

    private String type;

    private GeoLocation geoLocation;

    private Tracking tracking;

    private Location location;

    private Person person;

    /**
     * Constructor without parameter.
     * 
     */
    public Address() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Address(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "city" attribute.
     * 
     * @return The value of the "city" attribute.
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the value of the "country" attribute.
     * 
     * @return The value of the "country" attribute.
     */
    public String getCountry() {
        return country;
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
     * Returns the value of the "number" attribute.
     * 
     * @return The value of the "number" attribute.
     */
    public String getNumber() {
        return number;
    }

    /**
     * Returns the value of the "poBox" attribute.
     * 
     * @return The value of the "poBox" attribute.
     */
    public String getPoBox() {
        return poBox;
    }

    /**
     * Returns the value of the "street" attribute.
     * 
     * @return The value of the "street" attribute.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Returns the value of the "type" attribute.
     * 
     * @return The value of the "type" attribute.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the value of the "geoLocation" attribute.
     * 
     * @return The value of the "geoLocation" attribute.
     */
    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Returns the value of the "tracking" attribute.
     * 
     * @return The value of the "tracking" attribute.
     */
    public Tracking getTracking() {
        return tracking;
    }

    /**
     * Returns the value of the "location" attribute.
     * 
     * @return The value of the "location" attribute.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the value of the "person" attribute.
     * 
     * @return The value of the "person" attribute.
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Sets the value of the "city" attribute.
     * 
     * @param city
     *            The value of the "city" attribute.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Sets the value of the "country" attribute.
     * 
     * @param country
     *            The value of the "country" attribute.
     */
    public void setCountry(String country) {
        this.country = country;
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
     * Sets the value of the "number" attribute.
     * 
     * @param number
     *            The value of the "number" attribute.
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Sets the value of the "poBox" attribute.
     * 
     * @param poBox
     *            The value of the "poBox" attribute.
     */
    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    /**
     * Sets the value of the "street" attribute.
     * 
     * @param street
     *            The value of the "street" attribute.
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Sets the value of the "type" attribute.
     * 
     * @param type
     *            The value of the "type" attribute.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the value of the "geoLocation" attribute.
     * 
     * @param geoLocation
     *            The value of the "geoLocation" attribute.
     */
    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    /**
     * Sets the value of the "tracking" attribute.
     * 
     * @param tracking
     *            The value of the "tracking" attribute.
     */
    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
    }

    /**
     * Sets the value of the "location" attribute.
     * 
     * @param location
     *            " The value of the "location" attribute.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Sets the value of the "person" attribute.
     * 
     * @param person
     *            " The value of the "person" attribute.
     */
    public void setPerson(Person person) {
        this.person = person;
    }

}