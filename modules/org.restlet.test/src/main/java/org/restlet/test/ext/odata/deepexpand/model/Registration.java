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

import org.restlet.test.ext.odata.deepexpand.model.Category;
import org.restlet.test.ext.odata.deepexpand.model.CoOp;
import org.restlet.test.ext.odata.deepexpand.model.Group;
import org.restlet.test.ext.odata.deepexpand.model.InsuranceContract;
import org.restlet.test.ext.odata.deepexpand.model.Invitation;
import org.restlet.test.ext.odata.deepexpand.model.JobPosting;
import org.restlet.test.ext.odata.deepexpand.model.Location;
import org.restlet.test.ext.odata.deepexpand.model.Payment;
import org.restlet.test.ext.odata.deepexpand.model.Report;
import org.restlet.test.ext.odata.deepexpand.model.Requirement;
import org.restlet.test.ext.odata.deepexpand.model.Student;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Registration {

    private double grade;

    private Date gradeDate;

    private int hostSatisfactionRate;

    private int id;

    private boolean passed;

    private Date preferredEnd;

    private Date preferredStart;

    private double priority;

    private boolean qualifiedForAssigmnent;

    private boolean qualifiedForCompletion;

    private Date registrationDate;

    private int subjectSatisfactionRate;

    private int supervisionSatisfactionRate;

    private Tracking tracking;

    private CoOp coop;

    private Group group;

    private InsuranceContract insuranceContract;

    private List<Requirement> meetsRequirements;

    private List<Payment> payments;

    private List<Category> preferredCategories;

    private List<JobPosting> preferredJobPostings;

    private List<Location> preferredLocations;

    private List<Invitation> receivedInvitations;

    private List<Report> reports;

    private List<Invitation> sentInvitations;

    private Student student;

    /**
     * Constructor without parameter.
     * 
     */
    public Registration() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Registration(int id) {
        this();
        this.id = id;
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
     * Returns the value of the "gradeDate" attribute.
     * 
     * @return The value of the "gradeDate" attribute.
     */
    public Date getGradeDate() {
        return gradeDate;
    }

    /**
     * Returns the value of the "hostSatisfactionRate" attribute.
     * 
     * @return The value of the "hostSatisfactionRate" attribute.
     */
    public int getHostSatisfactionRate() {
        return hostSatisfactionRate;
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
     * Returns the value of the "passed" attribute.
     * 
     * @return The value of the "passed" attribute.
     */
    public boolean getPassed() {
        return passed;
    }

    /**
     * Returns the value of the "preferredEnd" attribute.
     * 
     * @return The value of the "preferredEnd" attribute.
     */
    public Date getPreferredEnd() {
        return preferredEnd;
    }

    /**
     * Returns the value of the "preferredStart" attribute.
     * 
     * @return The value of the "preferredStart" attribute.
     */
    public Date getPreferredStart() {
        return preferredStart;
    }

    /**
     * Returns the value of the "priority" attribute.
     * 
     * @return The value of the "priority" attribute.
     */
    public double getPriority() {
        return priority;
    }

    /**
     * Returns the value of the "qualifiedForAssigmnent" attribute.
     * 
     * @return The value of the "qualifiedForAssigmnent" attribute.
     */
    public boolean getQualifiedForAssigmnent() {
        return qualifiedForAssigmnent;
    }

    /**
     * Returns the value of the "qualifiedForCompletion" attribute.
     * 
     * @return The value of the "qualifiedForCompletion" attribute.
     */
    public boolean getQualifiedForCompletion() {
        return qualifiedForCompletion;
    }

    /**
     * Returns the value of the "registrationDate" attribute.
     * 
     * @return The value of the "registrationDate" attribute.
     */
    public Date getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Returns the value of the "subjectSatisfactionRate" attribute.
     * 
     * @return The value of the "subjectSatisfactionRate" attribute.
     */
    public int getSubjectSatisfactionRate() {
        return subjectSatisfactionRate;
    }

    /**
     * Returns the value of the "supervisionSatisfactionRate" attribute.
     * 
     * @return The value of the "supervisionSatisfactionRate" attribute.
     */
    public int getSupervisionSatisfactionRate() {
        return supervisionSatisfactionRate;
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
     * Returns the value of the "coop" attribute.
     * 
     * @return The value of the "coop" attribute.
     */
    public CoOp getCoop() {
        return coop;
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
     * Returns the value of the "insuranceContract" attribute.
     * 
     * @return The value of the "insuranceContract" attribute.
     */
    public InsuranceContract getInsuranceContract() {
        return insuranceContract;
    }

    /**
     * Returns the value of the "meetsRequirements" attribute.
     * 
     * @return The value of the "meetsRequirements" attribute.
     */
    public List<Requirement> getMeetsRequirements() {
        return meetsRequirements;
    }

    /**
     * Returns the value of the "payments" attribute.
     * 
     * @return The value of the "payments" attribute.
     */
    public List<Payment> getPayments() {
        return payments;
    }

    /**
     * Returns the value of the "preferredCategories" attribute.
     * 
     * @return The value of the "preferredCategories" attribute.
     */
    public List<Category> getPreferredCategories() {
        return preferredCategories;
    }

    /**
     * Returns the value of the "preferredJobPostings" attribute.
     * 
     * @return The value of the "preferredJobPostings" attribute.
     */
    public List<JobPosting> getPreferredJobPostings() {
        return preferredJobPostings;
    }

    /**
     * Returns the value of the "preferredLocations" attribute.
     * 
     * @return The value of the "preferredLocations" attribute.
     */
    public List<Location> getPreferredLocations() {
        return preferredLocations;
    }

    /**
     * Returns the value of the "receivedInvitations" attribute.
     * 
     * @return The value of the "receivedInvitations" attribute.
     */
    public List<Invitation> getReceivedInvitations() {
        return receivedInvitations;
    }

    /**
     * Returns the value of the "reports" attribute.
     * 
     * @return The value of the "reports" attribute.
     */
    public List<Report> getReports() {
        return reports;
    }

    /**
     * Returns the value of the "sentInvitations" attribute.
     * 
     * @return The value of the "sentInvitations" attribute.
     */
    public List<Invitation> getSentInvitations() {
        return sentInvitations;
    }

    /**
     * Returns the value of the "student" attribute.
     * 
     * @return The value of the "student" attribute.
     */
    public Student getStudent() {
        return student;
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
     * Sets the value of the "gradeDate" attribute.
     * 
     * @param gradeDate
     *            The value of the "gradeDate" attribute.
     */
    public void setGradeDate(Date gradeDate) {
        this.gradeDate = gradeDate;
    }

    /**
     * Sets the value of the "hostSatisfactionRate" attribute.
     * 
     * @param hostSatisfactionRate
     *            The value of the "hostSatisfactionRate" attribute.
     */
    public void setHostSatisfactionRate(int hostSatisfactionRate) {
        this.hostSatisfactionRate = hostSatisfactionRate;
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
     * Sets the value of the "passed" attribute.
     * 
     * @param passed
     *            The value of the "passed" attribute.
     */
    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    /**
     * Sets the value of the "preferredEnd" attribute.
     * 
     * @param preferredEnd
     *            The value of the "preferredEnd" attribute.
     */
    public void setPreferredEnd(Date preferredEnd) {
        this.preferredEnd = preferredEnd;
    }

    /**
     * Sets the value of the "preferredStart" attribute.
     * 
     * @param preferredStart
     *            The value of the "preferredStart" attribute.
     */
    public void setPreferredStart(Date preferredStart) {
        this.preferredStart = preferredStart;
    }

    /**
     * Sets the value of the "priority" attribute.
     * 
     * @param priority
     *            The value of the "priority" attribute.
     */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * Sets the value of the "qualifiedForAssigmnent" attribute.
     * 
     * @param qualifiedForAssigmnent
     *            The value of the "qualifiedForAssigmnent" attribute.
     */
    public void setQualifiedForAssigmnent(boolean qualifiedForAssigmnent) {
        this.qualifiedForAssigmnent = qualifiedForAssigmnent;
    }

    /**
     * Sets the value of the "qualifiedForCompletion" attribute.
     * 
     * @param qualifiedForCompletion
     *            The value of the "qualifiedForCompletion" attribute.
     */
    public void setQualifiedForCompletion(boolean qualifiedForCompletion) {
        this.qualifiedForCompletion = qualifiedForCompletion;
    }

    /**
     * Sets the value of the "registrationDate" attribute.
     * 
     * @param registrationDate
     *            The value of the "registrationDate" attribute.
     */
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Sets the value of the "subjectSatisfactionRate" attribute.
     * 
     * @param subjectSatisfactionRate
     *            The value of the "subjectSatisfactionRate" attribute.
     */
    public void setSubjectSatisfactionRate(int subjectSatisfactionRate) {
        this.subjectSatisfactionRate = subjectSatisfactionRate;
    }

    /**
     * Sets the value of the "supervisionSatisfactionRate" attribute.
     * 
     * @param supervisionSatisfactionRate
     *            The value of the "supervisionSatisfactionRate" attribute.
     */
    public void setSupervisionSatisfactionRate(int supervisionSatisfactionRate) {
        this.supervisionSatisfactionRate = supervisionSatisfactionRate;
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
     * Sets the value of the "coop" attribute.
     * 
     * @param coop
     *            " The value of the "coop" attribute.
     */
    public void setCoop(CoOp coop) {
        this.coop = coop;
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
     * Sets the value of the "insuranceContract" attribute.
     * 
     * @param insuranceContract
     *            " The value of the "insuranceContract" attribute.
     */
    public void setInsuranceContract(InsuranceContract insuranceContract) {
        this.insuranceContract = insuranceContract;
    }

    /**
     * Sets the value of the "meetsRequirements" attribute.
     * 
     * @param meetsRequirements
     *            " The value of the "meetsRequirements" attribute.
     */
    public void setMeetsRequirements(List<Requirement> meetsRequirements) {
        this.meetsRequirements = meetsRequirements;
    }

    /**
     * Sets the value of the "payments" attribute.
     * 
     * @param payments
     *            " The value of the "payments" attribute.
     */
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    /**
     * Sets the value of the "preferredCategories" attribute.
     * 
     * @param preferredCategories
     *            " The value of the "preferredCategories" attribute.
     */
    public void setPreferredCategories(List<Category> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }

    /**
     * Sets the value of the "preferredJobPostings" attribute.
     * 
     * @param preferredJobPostings
     *            " The value of the "preferredJobPostings" attribute.
     */
    public void setPreferredJobPostings(List<JobPosting> preferredJobPostings) {
        this.preferredJobPostings = preferredJobPostings;
    }

    /**
     * Sets the value of the "preferredLocations" attribute.
     * 
     * @param preferredLocations
     *            " The value of the "preferredLocations" attribute.
     */
    public void setPreferredLocations(List<Location> preferredLocations) {
        this.preferredLocations = preferredLocations;
    }

    /**
     * Sets the value of the "receivedInvitations" attribute.
     * 
     * @param receivedInvitations
     *            " The value of the "receivedInvitations" attribute.
     */
    public void setReceivedInvitations(List<Invitation> receivedInvitations) {
        this.receivedInvitations = receivedInvitations;
    }

    /**
     * Sets the value of the "reports" attribute.
     * 
     * @param reports
     *            " The value of the "reports" attribute.
     */
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    /**
     * Sets the value of the "sentInvitations" attribute.
     * 
     * @param sentInvitations
     *            " The value of the "sentInvitations" attribute.
     */
    public void setSentInvitations(List<Invitation> sentInvitations) {
        this.sentInvitations = sentInvitations;
    }

    /**
     * Sets the value of the "student" attribute.
     * 
     * @param student
     *            " The value of the "student" attribute.
     */
    public void setStudent(Student student) {
        this.student = student;
    }

}