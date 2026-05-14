/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test {@link org.restlet.data.Method}.
 *
 * <p>Note: this test purposefully does *not* extend RestletTestCase. The regression previously
 * present in Restlet (described in https://github.com/restlet/restlet-framework-java/issues/1130)
 * depends on class initialization order and vanishes when the Restlet/Engine class is initialized
 * before the class Method.
 *
 * @author Andreas Wundsam
 */
class MethodTestCase {

    /**
     * validate that Method caching works, i.e., the value returned by Method.valueOf("GET") is the
     * cached constant Method.GET.
     */
    @Test
    void testCaching() {
        Assertions.assertEquals(
                Method.GET,
                Method.valueOf("GET"),
                "Method.valueOf('GET') should return cached constant Method.GET ");
    }
}
