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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.data.Parameter;

class ChallengeWriterTestCase {

    @Test
    void append_challengeRequest_isNoOp() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.append((org.restlet.data.ChallengeRequest) null);
        assertEquals("", writer.toString());
    }

    @Test
    void isFirstChallengeParameter_initiallyTrue() {
        assertTrue(new ChallengeWriter().isFirstChallengeParameter());
    }

    @Test
    void appendChallengeParameter_name_writesTokenWithoutLeadingComma() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.appendChallengeParameter("realm");
        assertEquals("realm", writer.toString());
        assertFalse(writer.isFirstChallengeParameter());
    }

    @Test
    void appendChallengeParameter_nameAndValue_joinsWithEquals() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.appendChallengeParameter("realm", "test");
        assertEquals("realm=test", writer.toString());
    }

    @Test
    void appendChallengeParameter_secondParameter_prefixedWithComma() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.appendChallengeParameter("a", "1");
        writer.appendChallengeParameter("b", "2");
        assertEquals("a=1, b=2", writer.toString());
    }

    @Test
    void appendChallengeParameter_fromParameterObject_delegatesToNameValueOverload() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.appendChallengeParameter(new Parameter("realm", "test"));
        assertEquals("realm=test", writer.toString());
    }

    @Test
    void appendQuotedChallengeParameter_quotesValue() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.appendQuotedChallengeParameter("realm", "test realm");
        assertEquals("realm=\"test realm\"", writer.toString());
    }

    @Test
    void appendQuotedChallengeParameter_fromParameterObject_delegatesToNameValueOverload() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.appendQuotedChallengeParameter(new Parameter("realm", "test realm"));
        assertEquals("realm=\"test realm\"", writer.toString());
    }

    @Test
    void setFirstChallengeParameter_updatesState() {
        ChallengeWriter writer = new ChallengeWriter();
        writer.setFirstChallengeParameter(false);
        assertFalse(writer.isFirstChallengeParameter());
    }
}
