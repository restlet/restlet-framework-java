/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.restlet.data.Reference;

class HttpAwsS3HostNameTestCase {

    private String checkAddress(final String host, final String path) {
        return AwsUtils.getCanonicalizedResourceName(
                new Reference() {
                    public String getHostDomain() {
                        return host;
                    }

                    public String getPath() {
                        return path;
                    }
                });
    }

    @Test
    void testGetCanonicalizedResourceName1() {
        // http://s3-website-eu-west-1.amazonaws.com/reiabucket/louvel_cover150.jpg
        assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress(
                        "s3-website-eu-west-1.amazonaws.com", "/reiabucket/louvel_cover150.jpg"));
    }

    @Test
    void testGetCanonicalizedResourceName2() {
        // http://reiabucket.s3.amazonaws.com/louvel_cover150.jpg
        assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("reiabucket.s3.amazonaws.com", "/louvel_cover150.jpg"));
    }

    @Test
    void testGetCanonicalizedResourceName3() {
        // http://s3.amazonaws.com/reiabucket/louvel_cover150.jpg
        assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("s3.amazonaws.com", "/reiabucket/louvel_cover150.jpg"));
    }
}
