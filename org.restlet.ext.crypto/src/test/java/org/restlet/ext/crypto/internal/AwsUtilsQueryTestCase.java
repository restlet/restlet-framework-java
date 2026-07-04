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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;

/** Unit tests for the query-signature related methods of {@link AwsUtils}. */
class AwsUtilsQueryTestCase {

    private List<Parameter> params() {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter("Version", "2009-04-15"));
        params.add(new Parameter("Action", "ListDomains"));
        return params;
    }

    @Test
    void getQueryStringToSign_sortsParametersAndConcatenatesFields() {
        Reference resourceRef = new Reference("http://sdb.amazonaws.com/");

        String result = AwsUtils.getQueryStringToSign(Method.GET, resourceRef, params());

        assertEquals("GET\nsdb.amazonaws.com\n/\nAction=ListDomains&Version=2009-04-15", result);
    }

    @Test
    void getQueryStringToSign_nullMethod_omitsMethodName() {
        Reference resourceRef = new Reference("http://sdb.amazonaws.com/");

        String result = AwsUtils.getQueryStringToSign(null, resourceRef, new ArrayList<>());

        assertEquals("\nsdb.amazonaws.com\n/\n", result);
    }

    @Test
    void getQuerySignature_matchesKnownVector() {
        Reference resourceRef = new Reference("http://sdb.amazonaws.com/");

        String signature =
                AwsUtils.getQuerySignature(
                        Method.GET, resourceRef, params(), "mysecret".toCharArray());

        assertEquals("DG2zC7EgmtLBDdwPHV71VBw4mPpvIl1zymtFXAl/trQ=", signature);
    }

    @Test
    void getHmacSha256Signature_isDeterministic() {
        String signature1 = AwsUtils.getHmacSha256Signature("hello", "secret".toCharArray());
        String signature2 = AwsUtils.getHmacSha256Signature("hello", "secret".toCharArray());
        assertEquals(signature1, signature2);
    }

    @Test
    void getHmacSha1Signature_isDeterministic() {
        String signature1 = AwsUtils.getHmacSha1Signature("hello", "secret".toCharArray());
        String signature2 = AwsUtils.getHmacSha1Signature("hello", "secret".toCharArray());
        assertEquals(signature1, signature2);
    }
}
