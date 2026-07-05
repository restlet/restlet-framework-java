/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EditionTestCase {

    @AfterEach
    void tearDownEach() {
        Edition.CURRENT = Edition.JSE;
    }

    @Test
    void getFullName_returnsConfiguredName() {
        assertEquals("Java Standard Edition", Edition.JSE.getFullName());
    }

    @Test
    void getMediumName_returnsConfiguredName() {
        assertEquals("Java SE", Edition.JSE.getMediumName());
    }

    @Test
    void getShortName_returnsConfiguredName() {
        assertEquals("JSE", Edition.JSE.getShortName());
    }

    @Test
    void isCurrentEdition_matchesCurrentStaticField() {
        Edition.CURRENT = Edition.JSE;
        assertTrue(Edition.JSE.isCurrentEdition());
        assertFalse(Edition.ANDROID.isCurrentEdition());
    }

    @Test
    void isNotCurrentEdition_isInverseOfIsCurrentEdition() {
        Edition.CURRENT = Edition.JSE;
        assertFalse(Edition.JSE.isNotCurrentEdition());
        assertTrue(Edition.ANDROID.isNotCurrentEdition());
    }

    @Test
    void setCurrentEdition_updatesCurrentField() {
        Edition.ANDROID.setCurrentEdition();
        assertEquals(Edition.ANDROID, Edition.CURRENT);
    }

    @Test
    void isCurrentEditionOneOf_containingCurrentEdition_returnsTrue() {
        assertTrue(Edition.isCurrentEditionOneOf(Edition.ANDROID, Edition.JSE));
    }

    @Test
    void isCurrentEditionOneOf_notContainingCurrentEdition_returnsFalse() {
        Edition.CURRENT = Edition.JSE;
        assertFalse(Edition.isCurrentEditionOneOf(Edition.ANDROID));
    }

    @Test
    void isCurrentEditionOneOf_nullArray_returnsFalse() {
        assertFalse(Edition.isCurrentEditionOneOf((Edition[]) null));
    }
}
