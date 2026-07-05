/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusInfo;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

class ConverterUtilsTestCase {

    @Test
    void getBestHelper_objectSource_findsMatchingConverter() {
        StatusInfo source = new StatusInfo(Status.SUCCESS_OK);
        ConverterHelper helper =
                ConverterUtils.getBestHelper(source, new Variant(MediaType.TEXT_HTML), null);
        assertNotNull(helper);
        assertTrue(helper instanceof StatusInfoHtmlConverter);
    }

    @Test
    void getBestHelper_representationSource_findsMatchingConverter() {
        StringRepresentation source = new StringRepresentation("body", MediaType.TEXT_PLAIN);
        ConverterHelper helper = ConverterUtils.getBestHelper(source, String.class, null);
        assertNotNull(helper);
    }

    @Test
    void getVariants_statusInfoClass_returnsVariants() {
        List<VariantInfo> variants = ConverterUtils.getVariants(StatusInfo.class, null);
        assertNotNull(variants);
        assertTrue(variants.size() >= 2);
    }

    @Test
    void getVariants_unsupportedClass_returnsNull() {
        assertNull(ConverterUtils.getVariants(ConverterUtilsTestCase.class, null));
    }
}
