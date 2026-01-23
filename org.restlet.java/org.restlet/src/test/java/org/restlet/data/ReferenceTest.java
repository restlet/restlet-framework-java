/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ReferenceTest {

    @Test
    public void shouldFailWhenParsingMultiPortReference() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Reference("http://192.168.1.1:1111:2222/"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://[192.168.0.1]127.0.0.1/",
            "http://[192.168.0.1]vulndetector.com/",
            "http://[normal.com@]vulndetector.com/",
            "http://normal.com[user@vulndetector].com/",
            "http://normal.com[@]vulndetector.com/"})
    public void shouldFailWhenParsingIncorrectHosts(String reference) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Reference(reference));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://[0:0::vulndetector.com]:80", "http://[2001:db8::vulndetector.com]"})
    public void shouldFailWhenParsingIncorrectIPv6Hosts(String reference) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Reference(reference));
    }

}
