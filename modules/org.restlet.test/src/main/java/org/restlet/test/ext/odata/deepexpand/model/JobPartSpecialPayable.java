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

import org.restlet.test.ext.odata.deepexpand.model.FinancialSource;
import org.restlet.test.ext.odata.deepexpand.model.JobPart;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class JobPartSpecialPayable {

    private int id;

    private int paidDays;

    private FinancialSource financialSource;

    private JobPart jobPart;

    /**
     * Constructor without parameter.
     * 
     */
    public JobPartSpecialPayable() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public JobPartSpecialPayable(int id) {
        this();
        this.id = id;
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
     * Returns the value of the "paidDays" attribute.
     * 
     * @return The value of the "paidDays" attribute.
     */
    public int getPaidDays() {
        return paidDays;
    }

    /**
     * Returns the value of the "financialSource" attribute.
     * 
     * @return The value of the "financialSource" attribute.
     */
    public FinancialSource getFinancialSource() {
        return financialSource;
    }

    /**
     * Returns the value of the "jobPart" attribute.
     * 
     * @return The value of the "jobPart" attribute.
     */
    public JobPart getJobPart() {
        return jobPart;
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
     * Sets the value of the "paidDays" attribute.
     * 
     * @param paidDays
     *            The value of the "paidDays" attribute.
     */
    public void setPaidDays(int paidDays) {
        this.paidDays = paidDays;
    }

    /**
     * Sets the value of the "financialSource" attribute.
     * 
     * @param financialSource
     *            " The value of the "financialSource" attribute.
     */
    public void setFinancialSource(FinancialSource financialSource) {
        this.financialSource = financialSource;
    }

    /**
     * Sets the value of the "jobPart" attribute.
     * 
     * @param jobPart
     *            " The value of the "jobPart" attribute.
     */
    public void setJobPart(JobPart jobPart) {
        this.jobPart = jobPart;
    }

}