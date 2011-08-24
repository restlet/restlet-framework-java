/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.ext.crypto;

import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("s3-website-eu-west-1.amazonaws.com",
                        "/reiabucket/louvel_cover150.jpg"));
    }

    @Test
    public void testGetCanonicalizedResourceName2() {
        // http://reiabucket.s3.amazonaws.com/louvel_cover150.jpg
        Assert.assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("reiabucket.s3.amazonaws.com",
                        "/louvel_cover150.jpg"));
    }

    @Test
    public void testGetCanonicalizedResourceName3() {
        // http://s3.amazonaws.com/reiabucket/louvel_cover150.jpg
        Assert.assertEquals(
                "/reiabucket/louvel_cover150.jpg",
                checkAddress("s3.amazonaws.com",
                        "/reiabucket/louvel_cover150.jpg"));
    }

}
