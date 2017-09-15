/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.emf;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.restlet.ext.emf.EmfRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.test.RestletTestCase;

/**
 * Unit test for the {@link EmfRepresentation} class.
 * 
 * @author Jerome Louvel
 */
public class EmfRepresentationTestCase extends RestletTestCase {

    public void testParsing() throws IOException {
        ClientResource cr = new ClientResource(
                "clap://class/org/restlet/test/ext/emf/Test.ecore");
        Representation emfFile = cr.get();

        EmfRepresentation<EObject> emfRep = new EmfRepresentation<EObject>(
                emfFile);
        EObject emfObj = emfRep.getObject();
        assertNotNull(emfObj);
    }

    public void testBomb() throws IOException {
        ClientResource cr = new ClientResource(
                "clap://class/org/restlet/test/ext/emf/TestBomb.ecore");
        Representation emfFile = cr.get();

        boolean error = false;
        try {
            EmfRepresentation<EObject> emfRep = new EmfRepresentation<EObject>(
                    emfFile);
            emfRep.getObject();
        } catch (Exception e) {
            error = true;
        }
        assertTrue(error);
    }
}
