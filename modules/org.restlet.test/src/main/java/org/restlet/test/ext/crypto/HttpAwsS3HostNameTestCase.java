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

package org.restlet.test.ext.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.restlet.data.Reference;
import org.restlet.ext.crypto.internal.AwsUtils;
import org.restlet.test.RestletTestCase;

public class HttpAwsS3HostNameTestCase extends RestletTestCase {

    private String checkAddress(final String host, final String path) {
        return AwsUtils.getCanonicalizedResourceName(new Reference() {
            public String getHostDomain() {
                return host;
            }

            public String getPath() {
                return path;
            }
        });
    }

    @Test
    public void testGetCanonicalizedResourceName1() {
        // http://s3-website-eu-west-1.amazonaws.com/reiabucket/louvel_cover150.jpg
        assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("s3-website-eu-west-1.amazonaws.com",
                        "/reiabucket/louvel_cover150.jpg"));
    }

    @Test
    public void testGetCanonicalizedResourceName2() {
        // http://reiabucket.s3.amazonaws.com/louvel_cover150.jpg
        assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("reiabucket.s3.amazonaws.com",
                        "/louvel_cover150.jpg"));
    }

    @Test
    public void testGetCanonicalizedResourceName3() {
        // http://s3.amazonaws.com/reiabucket/louvel_cover150.jpg
        assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("s3.amazonaws.com",
                        "/reiabucket/louvel_cover150.jpg"));
    }

}
