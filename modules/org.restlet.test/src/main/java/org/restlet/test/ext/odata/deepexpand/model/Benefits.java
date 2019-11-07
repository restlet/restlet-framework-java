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

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Benefits {

    private boolean accommodationOffered;

    private boolean insuranceOffered;

    private boolean salaryOffered;

    private boolean transportationOffered;

    /**
     * Constructor without parameter.
     * 
     */
    public Benefits() {
        super();
    }

    /**
     * Returns the value of the "accommodationOffered" attribute.
     * 
     * @return The value of the "accommodationOffered" attribute.
     */
    public boolean getAccommodationOffered() {
        return accommodationOffered;
    }

    /**
     * Returns the value of the "insuranceOffered" attribute.
     * 
     * @return The value of the "insuranceOffered" attribute.
     */
    public boolean getInsuranceOffered() {
        return insuranceOffered;
    }

    /**
     * Returns the value of the "salaryOffered" attribute.
     * 
     * @return The value of the "salaryOffered" attribute.
     */
    public boolean getSalaryOffered() {
        return salaryOffered;
    }

    /**
     * Returns the value of the "transportationOffered" attribute.
     * 
     * @return The value of the "transportationOffered" attribute.
     */
    public boolean getTransportationOffered() {
        return transportationOffered;
    }

    /**
     * Sets the value of the "accommodationOffered" attribute.
     * 
     * @param accommodationOffered
     *            The value of the "accommodationOffered" attribute.
     */
    public void setAccommodationOffered(boolean accommodationOffered) {
        this.accommodationOffered = accommodationOffered;
    }

    /**
     * Sets the value of the "insuranceOffered" attribute.
     * 
     * @param insuranceOffered
     *            The value of the "insuranceOffered" attribute.
     */
    public void setInsuranceOffered(boolean insuranceOffered) {
        this.insuranceOffered = insuranceOffered;
    }

    /**
     * Sets the value of the "salaryOffered" attribute.
     * 
     * @param salaryOffered
     *            The value of the "salaryOffered" attribute.
     */
    public void setSalaryOffered(boolean salaryOffered) {
        this.salaryOffered = salaryOffered;
    }

    /**
     * Sets the value of the "transportationOffered" attribute.
     * 
     * @param transportationOffered
     *            The value of the "transportationOffered" attribute.
     */
    public void setTransportationOffered(boolean transportationOffered) {
        this.transportationOffered = transportationOffered;
    }

}