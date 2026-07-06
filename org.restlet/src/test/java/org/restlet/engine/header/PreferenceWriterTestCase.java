/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.util.Series;

class PreferenceWriterTestCase {

    @Test
    void isValidQuality_withinRange_returnsTrue() {
        assertTrue(PreferenceWriter.isValidQuality(0F));
        assertTrue(PreferenceWriter.isValidQuality(1F));
        assertTrue(PreferenceWriter.isValidQuality(0.5F));
    }

    @Test
    void isValidQuality_outsideRange_returnsFalse() {
        assertFalse(PreferenceWriter.isValidQuality(-0.1F));
        assertFalse(PreferenceWriter.isValidQuality(1.1F));
    }

    @Test
    void write_fullQuality_omitsQualityParameter() {
        Preference<MediaType> pref = new Preference<>(MediaType.TEXT_PLAIN, 1F);
        assertEquals("text/plain", PreferenceWriter.write(Arrays.asList(pref)));
    }

    @Test
    void write_partialQuality_appendsQParameter() {
        Preference<MediaType> pref = new Preference<>(MediaType.TEXT_PLAIN, 0.5F);
        assertEquals("text/plain;q=0.5", PreferenceWriter.write(Arrays.asList(pref)));
    }

    @Test
    void write_withParameters_appendsEachOne() {
        Series<Parameter> parameters = new Series<>(Parameter.class);
        parameters.add(new Parameter("level", "1"));
        Preference<MediaType> pref = new Preference<>(MediaType.TEXT_PLAIN, 1F, parameters);
        assertEquals("text/plain;level=1", PreferenceWriter.write(Arrays.asList(pref)));
    }

    @Test
    void appendQuality_invalidValue_throwsIllegalArgumentException() {
        PreferenceWriter preferenceWriter = new PreferenceWriter();
        assertThrows(IllegalArgumentException.class, () -> preferenceWriter.appendQuality(2F));
    }

    @Test
    void appendQuality_formatsWithMaxTwoDecimals() {
        String result = new PreferenceWriter().appendQuality(0.333F).toString();
        assertEquals("0.33", result);
    }
}
