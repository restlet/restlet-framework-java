/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

class StatusInfoHtmlConverterTestCase {

    private final StatusInfoHtmlConverter converter = new StatusInfoHtmlConverter();

    @Test
    void getObjectClasses_compatibleVariant_returnsStatusInfo() {
        List<Class<?>> classes = converter.getObjectClasses(new Variant(MediaType.TEXT_HTML));
        assertTrue(classes.contains(StatusInfo.class));
    }

    @Test
    void getObjectClasses_incompatibleVariant_returnsNull() {
        assertNull(converter.getObjectClasses(new Variant(MediaType.APPLICATION_JSON)));
    }

    @Test
    void getVariants_statusInfoClass_returnsHtmlVariants() throws IOException {
        List<?> variants = converter.getVariants(StatusInfo.class);
        assertEquals(2, variants.size());
    }

    @Test
    void getVariants_otherClass_returnsEmptyList() throws IOException {
        assertTrue(converter.getVariants(String.class).isEmpty());
    }

    @Test
    void getVariants_nullClass_returnsEmptyList() throws IOException {
        assertTrue(converter.getVariants(null).isEmpty());
    }

    @Test
    void score_statusInfoSourceAndCompatibleTarget_returnsMaxScore() {
        StatusInfo status = new StatusInfo(Status.SUCCESS_OK);
        float score = converter.score(status, new Variant(MediaType.TEXT_HTML), null);
        assertEquals(1.0F, score);
    }

    @Test
    void score_nonStatusInfoSource_returnsNegative() {
        float score = converter.score("not status info", new Variant(MediaType.TEXT_HTML), null);
        assertTrue(score < 0);
    }

    @Test
    void score_representationSourceOverload_alwaysReturnsNegative() throws IOException {
        Representation rep = new org.restlet.representation.StringRepresentation("body");
        assertTrue(converter.score(rep, StatusInfo.class, null) < 0);
    }

    @Test
    void toObject_alwaysReturnsNull() throws IOException {
        Representation rep = new org.restlet.representation.StringRepresentation("body");
        assertNull(converter.toObject(rep, StatusInfo.class, null));
    }

    @Test
    void toRepresentation_statusInfoWithDescriptionAndContact_includesAllSections()
            throws IOException {
        StatusInfo status = new StatusInfo(Status.SERVER_ERROR_INTERNAL);
        status.setDescription("Something broke");
        status.setContactEmail("admin@example.com");
        status.setHomeRef("http://example.com/home");

        Representation result =
                converter.toRepresentation(status, new Variant(MediaType.TEXT_HTML), null);
        String html = result.getText();

        assertTrue(html.contains("Something broke"));
        assertTrue(html.contains("admin@example.com"));
        assertTrue(html.contains("home page"));
        assertEquals(MediaType.TEXT_HTML, result.getMediaType());
    }

    @Test
    void toRepresentation_statusInfoWithoutOptionalFields_omitsThoseSections() throws IOException {
        StatusInfo status = new StatusInfo(0, null, null);
        Representation result =
                converter.toRepresentation(status, new Variant(MediaType.TEXT_HTML), null);
        String html = result.getText();
        assertTrue(html.contains("No information available"));
    }

    @Test
    void toRepresentation_nonStatusInfoSource_returnsNull() throws IOException {
        assertNull(
                converter.toRepresentation(
                        "not status info", new Variant(MediaType.TEXT_HTML), null));
    }
}
