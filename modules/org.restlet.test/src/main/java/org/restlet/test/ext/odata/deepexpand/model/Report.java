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

import java.util.Date;
import java.util.List;

import org.restlet.test.ext.odata.deepexpand.model.Attachment;
import org.restlet.test.ext.odata.deepexpand.model.AuthenticatedUser;
import org.restlet.test.ext.odata.deepexpand.model.CoOp;
import org.restlet.test.ext.odata.deepexpand.model.Group;
import org.restlet.test.ext.odata.deepexpand.model.JobPart;
import org.restlet.test.ext.odata.deepexpand.model.Registration;
import org.restlet.test.ext.odata.deepexpand.model.ReportType;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Report {

    private String comments;

    private Date dateSubmitted;

    private double grade;

    private int id;

    private String title;

    private Tracking tracking;

    private List<Attachment> attachments;

    private CoOp coOp;

    private Group group;

    private JobPart jobPart;

    private Registration registration;

    private AuthenticatedUser reportedBy;

    private ReportType reportType;

    /**
     * Constructor without parameter.
     * 
     */
    public Report() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Report(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "comments" attribute.
     * 
     * @return The value of the "comments" attribute.
     */
    public String getComments() {
        return comments;
    }

    /**
     * Returns the value of the "dateSubmitted" attribute.
     * 
     * @return The value of the "dateSubmitted" attribute.
     */
    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    /**
     * Returns the value of the "grade" attribute.
     * 
     * @return The value of the "grade" attribute.
     */
    public double getGrade() {
        return grade;
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
     * Returns the value of the "title" attribute.
     * 
     * @return The value of the "title" attribute.
     */
    public String getTitle() {
        return title;
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
     * Returns the value of the "attachments" attribute.
     * 
     * @return The value of the "attachments" attribute.
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Returns the value of the "coOp" attribute.
     * 
     * @return The value of the "coOp" attribute.
     */
    public CoOp getCoOp() {
        return coOp;
    }

    /**
     * Returns the value of the "group" attribute.
     * 
     * @return The value of the "group" attribute.
     */
    public Group getGroup() {
        return group;
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
     * Returns the value of the "registration" attribute.
     * 
     * @return The value of the "registration" attribute.
     */
    public Registration getRegistration() {
        return registration;
    }

    /**
     * Returns the value of the "reportedBy" attribute.
     * 
     * @return The value of the "reportedBy" attribute.
     */
    public AuthenticatedUser getReportedBy() {
        return reportedBy;
    }

    /**
     * Returns the value of the "reportType" attribute.
     * 
     * @return The value of the "reportType" attribute.
     */
    public ReportType getReportType() {
        return reportType;
    }

    /**
     * Sets the value of the "comments" attribute.
     * 
     * @param comments
     *            The value of the "comments" attribute.
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Sets the value of the "dateSubmitted" attribute.
     * 
     * @param dateSubmitted
     *            The value of the "dateSubmitted" attribute.
     */
    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    /**
     * Sets the value of the "grade" attribute.
     * 
     * @param grade
     *            The value of the "grade" attribute.
     */
    public void setGrade(double grade) {
        this.grade = grade;
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
     * Sets the value of the "title" attribute.
     * 
     * @param title
     *            The value of the "title" attribute.
     */
    public void setTitle(String title) {
        this.title = title;
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
     * Sets the value of the "attachments" attribute.
     * 
     * @param attachments
     *            " The value of the "attachments" attribute.
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Sets the value of the "coOp" attribute.
     * 
     * @param coOp
     *            " The value of the "coOp" attribute.
     */
    public void setCoOp(CoOp coOp) {
        this.coOp = coOp;
    }

    /**
     * Sets the value of the "group" attribute.
     * 
     * @param group
     *            " The value of the "group" attribute.
     */
    public void setGroup(Group group) {
        this.group = group;
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

    /**
     * Sets the value of the "registration" attribute.
     * 
     * @param registration
     *            " The value of the "registration" attribute.
     */
    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    /**
     * Sets the value of the "reportedBy" attribute.
     * 
     * @param reportedBy
     *            " The value of the "reportedBy" attribute.
     */
    public void setReportedBy(AuthenticatedUser reportedBy) {
        this.reportedBy = reportedBy;
    }

    /**
     * Sets the value of the "reportType" attribute.
     * 
     * @param reportType
     *            " The value of the "reportType" attribute.
     */
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

}