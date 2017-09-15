/**
 * Copyright 2005-2014 Restlet
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
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.odata.deepexpand;

import org.junit.Assert;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.odata.Query;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.odata.deepexpand.model.Benefits;
import org.restlet.test.ext.odata.deepexpand.model.Department;
import org.restlet.test.ext.odata.deepexpand.model.Division;
import org.restlet.test.ext.odata.deepexpand.model.Job;
import org.restlet.test.ext.odata.deepexpand.model.JobPart;
import org.restlet.test.ext.odata.deepexpand.model.JobPosting;
import org.restlet.test.ext.odata.deepexpand.model.Language;
import org.restlet.test.ext.odata.deepexpand.model.Literal;
import org.restlet.test.ext.odata.deepexpand.model.Multilingual;
import org.restlet.test.ext.odata.deepexpand.model.University;

public class ODataDeepExpandTestCase extends RestletTestCase {

    /** Inner component. */
    private Component component = new Component();

    /** OData service used for all tests. */
    private ContainerService service;

    /**
     * This checks that a multilingual text field (containing texts for many
     * languages) has been fully expanded to contain all literals for every
     * language and to contain the language definitions themselves too.
     * 
     * @param multilingual
     *            The field containing the multiple texts for each language.
     * @param basePath
     *            The descriptive path of the multilingual field. Used in
     *            assertion error messages.
     */
    private void assertFullExpansionOfMultilingualField(
            Multilingual multilingual, String basePath) {

        String baseMessage = "Should have fetched " + basePath;

        Assert.assertNotNull(baseMessage, multilingual);

        Assert.assertNotNull(baseMessage + "/literals",
                multilingual.getLiterals());

        for (Literal literal : multilingual.getLiterals()) {

            Assert.assertNotNull(baseMessage + "/literals(?)", literal);

            Language language = literal.getLanguage();

            Assert.assertNotNull(String.format("%s/literals(%d)/language",
                    baseMessage, literal.getId()), language);

            Assert.assertTrue(String.format(
                    "%s/literals(%d)/language/@id should be greater than zero",
                    basePath, literal.getId()), language.getId() > 0);

            String languagePath = String.format("%s/literals(%d)/language(%d)",
                    basePath, literal.getId(), language.getId());

            switch (language.getId()) {

            case 1:
                Assert.assertTrue(languagePath
                        + "/@localeCode shoule be \"el\"",
                        "el".equals(language.getLocaleCode()));

                break;

            case 2:
                Assert.assertTrue(languagePath
                        + "/@localeCode shoule be \"en\"",
                        "en".equals(language.getLocaleCode()));

                Assert.assertTrue(
                        languagePath + "/@name shoule be \"English\"",
                        "English".equals(language.getName()));

                break;
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        component.getServers().add(Protocol.HTTP, 8111);
        component.getClients().add(Protocol.CLAP);
        component
                .getDefaultHost()
                .attach("/CoopOData.svc",
                        new org.restlet.test.ext.odata.deepexpand.feed.CoOpApplication());
        component.start();

        service = new ContainerService();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
        component = null;
        super.tearDown();
    }

    /**
     * This test performs expansion and checks parsing of date forms with
     * omitted seconds (Issue #946) in the expanded hierarchy.
     */
    public void testJobExpansionAndParsingOfShortDates() {

        Query<Job> query = service
                .createJobQuery("/Job")
                .expand("jobParts/description/literals/language,jobPosting/name/literals/language");

        for (Job job : query) {
            Assert.assertNotNull(
                    "Failed to parse date, resulting to null value",
                    job.getStartDate());
            Assert.assertNotNull(
                    "Failed to parse date, resulting to null value",
                    job.getEndDate());

            if (job.getJobParts() != null) {

                for (JobPart jobPart : job.getJobParts()) {
                    Assert.assertNotNull(
                            "Failed to parse date, resulting to null value",
                            jobPart.getStartDate());
                    Assert.assertNotNull(
                            "Failed to parse date, resulting to null value",
                            jobPart.getEndDate());
                }
            }
        }

    }

    /**
     * This is a test which expands a path with cardinalities 1-1-*-1. At the
     * same time, it tests for correct reading of a property inside a complex
     * property.
     */
    public void testQueryJobPostingsDeepExpandWithComplexProperty() {
        Query<JobPosting> jobPostingQuery = service
                .createJobPostingQuery("/JobPosting")
                .expand("name/literals/language")
                .filter("benefits/transportationOffered eq true").top(10);

        for (JobPosting jobPosting : jobPostingQuery) {

            String jobPostingPath = String.format("/JobPostings(%d)",
                    jobPosting.getId());

            assertFullExpansionOfMultilingualField(jobPosting.getName(),
                    jobPostingPath + "/name");

            Benefits benefits = jobPosting.getBenefits();

            Assert.assertNotNull("jobPosting.benefits shouldn't be null",
                    benefits);

            Assert.assertTrue(
                    "jobPosting.benefits.transportationOffered should be true",
                    benefits.getTransportationOffered());
        }
    }

    /**
     * This is a test which expands two paths with cardinalities 1-*-1-*-1 and
     * 1-1-1-*-1.
     */
    public void testQueryJobsDeepExpandAndMultiple() {
        Query<Job> query = service
                .createJobQuery("/Job")
                .expand("jobParts/description/literals/language,jobPosting/name/literals/language");

        for (Job job : query) {
            String jobPostingPath = String.format("/Job(%d)/jobPosting",
                    job.getId());

            JobPosting jobPosting = job.getJobPosting();

            Assert.assertNotNull("Should have fetched " + jobPostingPath,
                    jobPosting);

            assertFullExpansionOfMultilingualField(jobPosting.getName(),
                    jobPostingPath + "/name");

            if (job.getJobParts() != null) {

                for (JobPart jobPart : job.getJobParts()) {

                    String jobPartDescriptionPath = String.format(
                            "/Job(%d)/jobParts(%d)/description", job.getId(),
                            jobPart.getId());

                    assertFullExpansionOfMultilingualField(
                            jobPart.getDescription(), jobPartDescriptionPath);
                }
            }
        }
    }

    /**
     * This is a test which expands three parallel and partially overlapping
     * paths with cardinalities 1-1-*-1, 1*-1-*-1 and 1-*-*-1-*-1.
     */
    public void testQueryUniversitiesExpandDepartmentsDivisionsAndAllNames() {

        Query<University> query = service
                .createUniversityQuery("University")
                .expand("name/literals/language,departments/name/literals/language,departments/divisions/name/literals/language");

        for (University university : query) {

            String universityPath = String.format("/University(%d)",
                    university.getId());

            assertFullExpansionOfMultilingualField(university.getName(),
                    universityPath + "/name");

            for (Department department : university.getDepartments()) {

                String departmentPath = String.format("%s/departments(%d)",
                        universityPath, department.getId());

                assertFullExpansionOfMultilingualField(department.getName(),
                        departmentPath + "/name");

                for (Division division : department.getDivisions()) {

                    String divisionPath = String.format("%s/divisions(%d)",
                            departmentPath, division.getId());

                    assertFullExpansionOfMultilingualField(division.getName(),
                            divisionPath + "/name");
                }

            }
        }
    }

}
