package org.restlet.engine.util;

import junit.framework.TestCase;

public class Base64Test extends TestCase {

    public void test_restlet_base64_encoding_without_newlines_should_be_equal_jdk_base64_encoding() {
        String source =
                "Man is distinguished, not only by his reason, but by this singular passion from " +
                        "other animals, which is a lust of the mind, that by a perseverance of delight " +
                        "in the continued and indefatigable generation of knowledge, exceeds the short " +
                        "vehemence of any carnal pleasure.";
        String expected =
                "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz" +
                        "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg" +
                        "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu" +
                        "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo" +
                        "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";

        assertEquals(expected, Base64.encode(source.getBytes(), false));
        assertEquals(expected, java.util.Base64.getEncoder().encodeToString(source.getBytes()));
    }

    public void test_restlet_base64_encoding_with_newlines_should_be_equal_jdk_mime_base64_encoding() {
        String source =
                "Man is distinguished, not only by his reason, but by this singular passion from " +
                        "other animals, which is a lust of the mind, that by a perseverance of delight " +
                        "in the continued and indefatigable generation of knowledge, exceeds the short " +
                        "vehemence of any carnal pleasure.";
        String expected =
                "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz\n" +
                        "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg\n" +
                        "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu\n" +
                        "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo\n" +
                        "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";
        assertEquals(expected, Base64.encode(source.getBytes(), true));
    }

    public void test_restlet_base64_decoding_should_be_equal_jdk_base64_decoding() {
        byte[] expected =
                ("Man is distinguished, not only by his reason, but by this singular passion from " +
                        "other animals, which is a lust of the mind, that by a perseverance of delight " +
                        "in the continued and indefatigable generation of knowledge, exceeds the short " +
                        "vehemence of any carnal pleasure.").getBytes();
        String source =
                "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz" +
                        "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg" +
                        "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu" +
                        "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo" +
                        "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";


        byte[] rf_decoded_bytes = Base64.decode(source);
        byte[] jdk_decoded_bytes = java.util.Base64.getDecoder().decode(source);

        assertEquals(expected.length, rf_decoded_bytes.length);
        assertEquals(expected.length, jdk_decoded_bytes.length);

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], rf_decoded_bytes[i]);
            assertEquals(expected[i], jdk_decoded_bytes[i]);
        }
    }

}
