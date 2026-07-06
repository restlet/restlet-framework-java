/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.restlet.data.LocalReference.RIAP_APPLICATION;
import static org.restlet.data.LocalReference.createRiapReference;

import java.io.Serializable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

/**
 * Unit test case for the RIAP Internal routing protocol.
 *
 * @author Marc Portier
 */
class RiapTestCase {

    private static final String DEFAULT_MSG = "no-default";

    // Just some serializable dummy object handle...
    public static class Dummy implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private static final Serializable JUST_SOME_OBJ = new Dummy();

    private static final String ECHO_TEST_MSG = JUST_SOME_OBJ.toString();

    private static String buildAggregate(String echoMessage, String echoCopy) {
        return String.format("ORIGINAL: %s%nECHOCOPY: %s%n", echoMessage, echoCopy);
    }

    static Restlet dispatcher;
    static String localBase;

    @BeforeAll
    static void setUp() {
        Engine.clearThreadLocalVariables();
        final Component comp = new Component();
        final Application localOnly =
                new Application() {
                    @Override
                    public Restlet createInboundRoot() {
                        return new Restlet(getContext()) {
                            @Override
                            public void handle(Request request, Response response) {
                                final Reference ref = request.getResourceRef();
                                final String remainder = ref.getRemainingPart();

                                Representation result = new StringRepresentation(DEFAULT_MSG);

                                if (remainder.startsWith("/echo/")) {
                                    result = new StringRepresentation(remainder.substring(6));
                                } else if (remainder.equals("/object")) {
                                    result = new ObjectRepresentation<>(JUST_SOME_OBJ);
                                } else if (remainder.equals("/null")) {
                                    result = new ObjectRepresentation<>((Serializable) null);
                                } else if (remainder.equals("/self-aggregated")) {
                                    final String echoMessage = ECHO_TEST_MSG;
                                    final Reference echoRef =
                                            createRiapReference(
                                                    RIAP_APPLICATION, "/echo/" + echoMessage);
                                    try {
                                        String echoCopy =
                                                new ClientResource(echoRef).get().getText();
                                        assertEquals(
                                                echoMessage, echoCopy, "expected echoMessage back");
                                        result =
                                                new StringRepresentation(
                                                        buildAggregate(echoMessage, echoCopy));
                                    } catch (Exception e) {
                                        fail("Error getting internal reference to " + echoRef, e);
                                    }
                                }
                                response.setEntity(result);
                            }
                        };
                    }
                };

        comp.getInternalRouter().attach("/local", localOnly);

        localBase = createRiapReference(LocalReference.RIAP_COMPONENT, "/local").toString();
        dispatcher = comp.getContext().getClientDispatcher();
    }

    @AfterAll
    static void tearDown() {
        Engine.clearThreadLocalVariables();
    }

    @Test
    void testEcho() throws Exception {
        String msg = "this%20message";
        Representation echoRep = sendLocalRequestAndGetRepresentation("/echo/" + msg);
        assertEquals(msg, echoRep.getText(), "expected echo of uri-remainder");
    }

    @Test
    void testObject() throws Exception {
        final Representation objRep = sendLocalRequestAndGetRepresentation("/object");
        assertSame(
                JUST_SOME_OBJ,
                ((ObjectRepresentation<?>) objRep).getObject(),
                "expected specific test-object");
    }

    @Test
    void testNull() throws Exception {
        final Representation nullRep = sendLocalRequestAndGetRepresentation("/null");
        assertNull(((ObjectRepresentation<?>) nullRep).getObject(), "expected null");
    }

    @Test
    void testWhatever() throws Exception {
        final Representation anyRep = sendLocalRequestAndGetRepresentation("/whatever");
        assertEquals(DEFAULT_MSG, anyRep.getText(), "expected echo of uri-remainder");
    }

    @Test
    void testSelfAggregated() throws Exception {
        final Representation aggRep = sendLocalRequestAndGetRepresentation("/self-aggregated");
        final String expectedResult = buildAggregate(ECHO_TEST_MSG, ECHO_TEST_MSG);
        assertEquals(expectedResult, aggRep.getText(), "expected specific aggregated message");
    }

    private static Representation sendLocalRequestAndGetRepresentation(final String url) {
        return dispatcher.handle(new Request(Method.GET, localBase + url)).getEntity();
    }
}
