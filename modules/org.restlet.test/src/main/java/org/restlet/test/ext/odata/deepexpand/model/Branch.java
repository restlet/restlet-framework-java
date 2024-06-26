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

import org.restlet.test.ext.odata.deepexpand.model.Company;
import org.restlet.test.ext.odata.deepexpand.model.CompanyPerson;
import org.restlet.test.ext.odata.deepexpand.model.JobPart;
import org.restlet.test.ext.odata.deepexpand.model.JobPostingPart;
import org.restlet.test.ext.odata.deepexpand.model.Multilingual;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Branch {

    private String fax;

    private int id;

    private String telephone;

    private EmbeddableAddress address;

    private Tracking tracking;

    private Company company;

    private List<JobPart> jobParts;

    private List<JobPostingPart> jobPostingParts;

    private Multilingual name;

    private List<CompanyPerson> persons;

    /**
     * Constructor without parameter.
     * 
     */
    public Branch() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Branch(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "fax" attribute.
     * 
     * @return The value of the "fax" attribute.
     */
    public String getFax() {
        return fax;
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
     * Returns the value of the "telephone" attribute.
     * 
     * @return The value of the "telephone" attribute.
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Returns the value of the "address" attribute.
     * 
     * @return The value of the "address" attribute.
     */
    public EmbeddableAddress getAddress() {
        return address;
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
     * Returns the value of the "company" attribute.
     * 
     * @return The value of the "company" attribute.
     */
    public Company getCompany() {
        return company;
    }

    /**
     * Returns the value of the "jobParts" attribute.
     * 
     * @return The value of the "jobParts" attribute.
     */
    public List<JobPart> getJobParts() {
        return jobParts;
    }

    /**
     * Returns the value of the "jobPostingParts" attribute.
     * 
     * @return The value of the "jobPostingParts" attribute.
     */
    public List<JobPostingPart> getJobPostingParts() {
        return jobPostingParts;
    }

    /**
     * Returns the value of the "name" attribute.
     * 
     * @return The value of the "name" attribute.
     */
    public Multilingual getName() {
        return name;
    }

    /**
     * Returns the value of the "persons" attribute.
     * 
     * @return The value of the "persons" attribute.
     */
    public List<CompanyPerson> getPersons() {
        return persons;
    }

    /**
     * Sets the value of the "fax" attribute.
     * 
     * @param fax
     *            The value of the "fax" attribute.
     */
    public void setFax(String fax) {
        this.fax = fax;
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
     * Sets the value of the "telephone" attribute.
     * 
     * @param telephone
     *            The value of the "telephone" attribute.
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Sets the value of the "address" attribute.
     * 
     * @param address
     *            The value of the "address" attribute.
     */
    public void setAddress(EmbeddableAddress address) {
        this.address = address;
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
     * Sets the value of the "company" attribute.
     * 
     * @param company
     *            " The value of the "company" attribute.
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * Sets the value of the "jobParts" attribute.
     * 
     * @param jobParts
     *            " The value of the "jobParts" attribute.
     */
    public void setJobParts(List<JobPart> jobParts) {
        this.jobParts = jobParts;
    }

    /**
     * Sets the value of the "jobPostingParts" attribute.
     * 
     * @param jobPostingParts
     *            " The value of the "jobPostingParts" attribute.
     */
    public void setJobPostingParts(List<JobPostingPart> jobPostingParts) {
        this.jobPostingParts = jobPostingParts;
    }

    /**
     * Sets the value of the "name" attribute.
     * 
     * @param name
     *            " The value of the "name" attribute.
     */
    public void setName(Multilingual name) {
        this.name = name;
    }

    /**
     * Sets the value of the "persons" attribute.
     * 
     * @param persons
     *            " The value of the "persons" attribute.
     */
    public void setPersons(List<CompanyPerson> persons) {
        this.persons = persons;
    }

}