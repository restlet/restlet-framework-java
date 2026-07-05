/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/** Unit tests for {@link Realm}. */
class RealmTestCase {

    /** Minimal concrete fixture, since {@link Realm} is abstract. */
    private static class TestRealm extends Realm {

        TestRealm() {
            super();
        }

        TestRealm(Verifier verifier, Enroler enroler) {
            super(verifier, enroler);
        }
    }

    private static final Verifier NO_OP_VERIFIER = (request, response) -> Verifier.RESULT_VALID;

    private static final Enroler NO_OP_ENROLER = clientInfo -> {};

    @Test
    void defaultConstructor_hasNullNameVerifierAndEnroler() {
        TestRealm realm = new TestRealm();

        assertNull(realm.getName());
        assertNull(realm.getVerifier());
        assertNull(realm.getEnroler());
        assertTrue(realm.getParameters().isEmpty());
        assertFalse(realm.isStarted());
        assertTrue(realm.isStopped());
    }

    @Test
    void constructorWithVerifierAndEnroler_setsFields() {
        TestRealm realm = new TestRealm(NO_OP_VERIFIER, NO_OP_ENROLER);

        assertSame(NO_OP_VERIFIER, realm.getVerifier());
        assertSame(NO_OP_ENROLER, realm.getEnroler());
    }

    @Test
    void getNameSetName_roundTrip() {
        TestRealm realm = new TestRealm();
        realm.setName("my-realm");
        assertEquals("my-realm", realm.getName());
    }

    @Test
    void getVerifierSetVerifier_roundTrip() {
        TestRealm realm = new TestRealm();
        realm.setVerifier(NO_OP_VERIFIER);
        assertSame(NO_OP_VERIFIER, realm.getVerifier());
    }

    @Test
    void getEnrolerSetEnroler_roundTrip() {
        TestRealm realm = new TestRealm();
        realm.setEnroler(NO_OP_ENROLER);
        assertSame(NO_OP_ENROLER, realm.getEnroler());
    }

    @Test
    void getParameters_isModifiable() {
        TestRealm realm = new TestRealm();
        realm.getParameters().add("name", "value");
        assertEquals(1, realm.getParameters().size());
    }

    @Test
    void setParameters_withNull_clearsSeries() {
        TestRealm realm = new TestRealm();
        realm.getParameters().add("name", "value");

        realm.setParameters(null);

        assertTrue(realm.getParameters().isEmpty());
    }

    @Test
    void setParameters_withNewSeries_replacesContents() {
        TestRealm realm = new TestRealm();
        realm.getParameters().add("old", "value");

        Series<Parameter> newParameters =
                new Series<>(Parameter.class, new CopyOnWriteArrayList<Parameter>());
        newParameters.add("new", "value");
        realm.setParameters(newParameters);

        assertEquals(1, realm.getParameters().size());
        assertEquals("new", realm.getParameters().get(0).getName());
    }

    @Test
    void setParameters_withSameSeriesInstance_isNoOp() {
        TestRealm realm = new TestRealm();
        realm.getParameters().add("name", "value");

        realm.setParameters(realm.getParameters());

        assertEquals(1, realm.getParameters().size());
    }

    @Test
    void startStop_updateStartedAndStoppedState() throws Exception {
        TestRealm realm = new TestRealm();

        realm.start();
        assertTrue(realm.isStarted());
        assertFalse(realm.isStopped());

        realm.stop();
        assertFalse(realm.isStarted());
        assertTrue(realm.isStopped());
    }

    @Test
    void toString_returnsName() {
        TestRealm realm = new TestRealm();
        realm.setName("my-realm");
        assertEquals("my-realm", realm.toString());
    }
}
