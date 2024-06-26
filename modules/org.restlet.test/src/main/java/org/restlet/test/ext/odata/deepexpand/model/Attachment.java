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

import org.restlet.test.ext.odata.deepexpand.model.InsuranceContract;
import org.restlet.test.ext.odata.deepexpand.model.Report;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Attachment {

    private byte[] content;

    private String contentType;

    private int id;

    private String name;

    private Tracking tracking;

    private InsuranceContract insuranceContract;

    private Report report;

    /**
     * Constructor without parameter.
     * 
     */
    public Attachment() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Attachment(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "content" attribute.
     * 
     * @return The value of the "content" attribute.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns the value of the "contentType" attribute.
     * 
     * @return The value of the "contentType" attribute.
     */
    public String getContentType() {
        return contentType;
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
     * Returns the value of the "name" attribute.
     * 
     * @return The value of the "name" attribute.
     */
    public String getName() {
        return name;
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
     * Returns the value of the "insuranceContract" attribute.
     * 
     * @return The value of the "insuranceContract" attribute.
     */
    public InsuranceContract getInsuranceContract() {
        return insuranceContract;
    }

    /**
     * Returns the value of the "report" attribute.
     * 
     * @return The value of the "report" attribute.
     */
    public Report getReport() {
        return report;
    }

    /**
     * Sets the value of the "content" attribute.
     * 
     * @param content
     *            The value of the "content" attribute.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Sets the value of the "contentType" attribute.
     * 
     * @param contentType
     *            The value of the "contentType" attribute.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
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
     * Sets the value of the "name" attribute.
     * 
     * @param name
     *            The value of the "name" attribute.
     */
    public void setName(String name) {
        this.name = name;
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
     * Sets the value of the "insuranceContract" attribute.
     * 
     * @param insuranceContract
     *            " The value of the "insuranceContract" attribute.
     */
    public void setInsuranceContract(InsuranceContract insuranceContract) {
        this.insuranceContract = insuranceContract;
    }

    /**
     * Sets the value of the "report" attribute.
     * 
     * @param report
     *            " The value of the "report" attribute.
     */
    public void setReport(Report report) {
        this.report = report;
    }

}