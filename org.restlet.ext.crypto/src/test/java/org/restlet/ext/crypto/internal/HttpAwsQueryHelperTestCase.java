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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Reference;

/** Unit tests for {@link HttpAwsQueryHelper}. */
class HttpAwsQueryHelperTestCase {

    @Test
    void constructor_registersAwsQueryScheme() {
        HttpAwsQueryHelper helper = new HttpAwsQueryHelper();
        assertEquals(ChallengeScheme.HTTP_AWS_QUERY, helper.getChallengeScheme());
    }

    @Test
    void updateReference_withoutActionParameter_returnsSameReference() {
        HttpAwsQueryHelper helper = new HttpAwsQueryHelper();
        Request request = new Request(Method.GET, "http://sdb.amazonaws.com/");
        Reference resourceRef = new Reference("http://sdb.amazonaws.com/");

        Reference result = helper.updateReference(resourceRef, null, request);

        assertSame(resourceRef, result);
    }

    @Test
    void updateReference_withActionParameter_signsAndAppendsParameters() {
        HttpAwsQueryHelper helper = new HttpAwsQueryHelper();
        ChallengeResponse challengeResponse =
                new ChallengeResponse(
                        ChallengeScheme.HTTP_AWS_QUERY, "myaccesskey", "mysecret".toCharArray());
        Request request = new Request(Method.GET, "http://sdb.amazonaws.com/?Action=ListDomains");
        request.setChallengeResponse(challengeResponse);
        Reference resourceRef = new Reference("http://sdb.amazonaws.com/?Action=ListDomains");

        Reference result = helper.updateReference(resourceRef, challengeResponse, request);

        assertNotSame(resourceRef, result);
        String query = result.getQuery();
        assertTrue(query.contains("AWSAccessKeyId=myaccesskey"));
        assertTrue(query.contains("SignatureMethod=HmacSHA256"));
        assertTrue(query.contains("SignatureVersion=2"));
        assertTrue(query.contains("Version=2009-04-15"));
        assertTrue(query.contains("Timestamp="));
        assertTrue(query.contains("Signature="));
    }
}
