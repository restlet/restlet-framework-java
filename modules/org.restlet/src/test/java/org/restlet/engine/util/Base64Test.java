package org.restlet.engine.util;

import junit.framework.TestCase;

public class Base64Test extends TestCase {

    private static final String REFERENCE_HUMAN_READABLE_TEXT = "Man is distinguished, not only by his reason, but by this singular passion from " +
            "other animals, which is a lust of the mind, that by a perseverance of delight " +
            "in the continued and indefatigable generation of knowledge, exceeds the short " +
            "vehemence of any carnal pleasure.";
    private static final byte[] REFERENCE_TEXT_AS_BYTES_ARRAY = REFERENCE_HUMAN_READABLE_TEXT.getBytes();

    private static final String REFERENCE_TEXT_ENCODED = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz" +
            "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg" +
            "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu" +
            "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo" +
            "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";

    private static final String REFERENCE_TEXT_ENCODED_WITH_NEWLINES_FORMATTING =
            "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz\n" +
                    "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg\n" +
                    "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu\n" +
                    "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo\n" +
                    "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";


    public void test_restlet_base64_encoding_without_newlines_should_be_equal_jdk_base64_encoding() {
        assertEquals(REFERENCE_TEXT_ENCODED, Base64.encode(REFERENCE_TEXT_AS_BYTES_ARRAY, false));
        assertEquals(REFERENCE_TEXT_ENCODED, java.util.Base64.getEncoder().encodeToString(REFERENCE_TEXT_AS_BYTES_ARRAY));
    }

    public void test_restlet_base64_encoding_with_newlines_should_be_equal_jdk_mime_base64_encoding() {
        assertEquals(REFERENCE_TEXT_ENCODED_WITH_NEWLINES_FORMATTING, Base64.encode(REFERENCE_TEXT_AS_BYTES_ARRAY, true));
    }

    public void test_restlet_base64_decoding_should_be_equal_jdk_base64_decoding() {
        byte[] rf_decoded_bytes = Base64.decode(REFERENCE_TEXT_ENCODED);
        byte[] jdk_decoded_bytes = java.util.Base64.getDecoder().decode(REFERENCE_TEXT_ENCODED);

        assertEquals(REFERENCE_TEXT_AS_BYTES_ARRAY.length, rf_decoded_bytes.length);
        assertEquals(REFERENCE_TEXT_AS_BYTES_ARRAY.length, jdk_decoded_bytes.length);

        for (int i = 0; i < REFERENCE_TEXT_AS_BYTES_ARRAY.length; i++) {
            assertEquals(REFERENCE_TEXT_AS_BYTES_ARRAY[i], rf_decoded_bytes[i]);
            assertEquals(REFERENCE_TEXT_AS_BYTES_ARRAY[i], jdk_decoded_bytes[i]);
        }
    }

}
