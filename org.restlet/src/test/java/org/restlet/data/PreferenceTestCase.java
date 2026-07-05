/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.restlet.util.Series;

/** Test {@link org.restlet.data.Preference}. */
class PreferenceTestCase {

    @Test
    void defaultConstructor_hasNullMetadataAndFullQuality() {
        Preference<MediaType> preference = new Preference<>();
        assertNull(preference.getMetadata());
        assertEquals(1F, preference.getQuality());
    }

    @Test
    void constructor_withMetadata_hasFullQuality() {
        Preference<MediaType> preference = new Preference<>(MediaType.TEXT_PLAIN);
        assertEquals(MediaType.TEXT_PLAIN, preference.getMetadata());
        assertEquals(1F, preference.getQuality());
    }

    @Test
    void constructor_withMetadataAndQuality() {
        Preference<MediaType> preference = new Preference<>(MediaType.TEXT_PLAIN, 0.5F);
        assertEquals(0.5F, preference.getQuality());
    }

    @Test
    void constructor_withParameters() {
        Series<Parameter> parameters = new Form();
        parameters.add("q", "0.8");
        Preference<MediaType> preference = new Preference<>(MediaType.TEXT_PLAIN, 0.8F, parameters);
        assertEquals(1, preference.getParameters().size());
    }

    @Test
    void getParameters_lazilyCreatesEmptySeries() {
        Preference<MediaType> preference = new Preference<>(MediaType.TEXT_PLAIN);
        assertNotNull(preference.getParameters());
        assertEquals(0, preference.getParameters().size());
    }

    @Test
    void settersUpdateState() {
        Preference<MediaType> preference = new Preference<>();
        preference.setMetadata(MediaType.APPLICATION_JSON);
        preference.setQuality(0.3F);
        Series<Parameter> parameters = new Form();
        preference.setParameters(parameters);

        assertEquals(MediaType.APPLICATION_JSON, preference.getMetadata());
        assertEquals(0.3F, preference.getQuality());
        assertSame(parameters, preference.getParameters());
    }

    @Test
    void toString_withMetadata_includesNameAndQuality() {
        Preference<MediaType> preference = new Preference<>(MediaType.TEXT_PLAIN, 0.5F);
        assertEquals("text/plain:0.5", preference.toString());
    }

    @Test
    void toString_withoutMetadata_returnsEmptyString() {
        Preference<MediaType> preference = new Preference<>();
        assertEquals("", preference.toString());
    }
}
