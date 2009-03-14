/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.test;

import java.io.Serializable;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.LocalReference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit test case for the RIAP Internal routing protocol.
 * 
 * @author Marc Portier (mpo@outerthought.org)
 */
public class RiapTestCase extends TestCase {

    private static final String DEFAULT_MSG = "no-default";

    // Just Some Serializable dummy object handle...
    private static final Serializable JUST_SOME_OBJ = new Serializable() {
        private static final long serialVersionUID = 1L;
    };

    private static final String ECHO_TEST_MSG = JUST_SOME_OBJ.toString();

    private String buildAggregate(String echoMessage, String echoCopy) {
        return "ORIGINAL: " + echoMessage + "\n" + "ECHOCOPY: " + echoCopy
                + "\n";
    }

    @SuppressWarnings("unchecked")
    public void testRiap() throws Exception {
        final Component comp = new Component();
        final Application localOnly = new Application() {
            @Override
            public Restlet createRoot() {
                return new Restlet(getContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        final String selfBase = "riap://application";
                        final Reference ref = request.getResourceRef();
                        final String remainder = ref.getRemainingPart();

                        Representation result = new StringRepresentation(
                                DEFAULT_MSG);

                        if (remainder.startsWith("/echo/")) {
                            result = new StringRepresentation(remainder
                                    .substring(6));
                        } else if (remainder.equals("/object")) {
                            result = new ObjectRepresentation(JUST_SOME_OBJ);
                        } else if (remainder.equals("/null")) {
                            result = new ObjectRepresentation(
                                    (Serializable) null);
                        } else if (remainder.equals("/self-aggregated")) {
                            final String echoMessage = ECHO_TEST_MSG;
                            final Reference echoRef = new LocalReference(
                                    selfBase + "/echo/" + echoMessage);
                            String echoCopy = null;
                            try {
                                final Response respo = getContext()
                                        .getClientDispatcher().get(echoRef);
                                final Representation entity = respo.getEntity();
                                echoCopy = entity.getText();
                            } catch (Exception e) {
                                e.printStackTrace();
                                fail("Error getting internal reference to "
                                        + echoRef);
                            }
                            assertEquals("expected echoMessage back",
                                    echoMessage, echoCopy);
                            result = new StringRepresentation(buildAggregate(
                                    echoMessage, echoCopy));
                        }
                        response.setEntity(result);
                    }
                };
            }
        };

        comp.getInternalRouter().attach("/local", localOnly);
        final String localBase = "riap://component/local";

        final Uniform dispatcher = comp.getContext().getClientDispatcher();

        final String msg = "this%20message";
        final String echoURI = localBase + "/echo/" + msg;
        final Representation echoRep = dispatcher.get(echoURI).getEntity();
        assertEquals("expected echo of uri-remainder", msg, echoRep.getText());

        final String objURI = localBase + "/object";
        final Representation objRep = dispatcher.get(objURI).getEntity();
        assertSame("expected specific test-object", JUST_SOME_OBJ,
                ((ObjectRepresentation) objRep).getObject());

        final String nullURI = localBase + "/null";
        final Representation nullRep = dispatcher.get(nullURI).getEntity();
        assertNull("expected null", ((ObjectRepresentation) nullRep)
                .getObject());

        final String anyURI = localBase + "/whatever";
        final Representation anyRep = dispatcher.get(anyURI).getEntity();
        assertEquals("expected echo of uri-remainder", DEFAULT_MSG, anyRep
                .getText());

        final String aggURI = localBase + "/self-aggregated";
        final Representation aggRep = dispatcher.get(aggURI).getEntity();
        final String expectedResult = buildAggregate(ECHO_TEST_MSG,
                ECHO_TEST_MSG);
        assertEquals("expected specific aggregated message", expectedResult,
                aggRep.getText());
    }
}
