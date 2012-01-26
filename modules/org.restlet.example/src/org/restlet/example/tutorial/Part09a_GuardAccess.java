/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.tutorial;

import static org.restlet.example.tutorial.Constants.ROOT_URI;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

/**
 * Guard access to a Restlet.
 * 
 * @author Jerome Louvel
 */
public class Part09a_GuardAccess extends Application {

    /**
     * Run the example as a standalone component.
     * 
     * @param args
     *            The optional arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Create a component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8111);
        component.getClients().add(Protocol.FILE);

        // Create an application
        Application application = new Part09a_GuardAccess();

        // Attach the application to the component and start it
        component.getDefaultHost().attachDefault(application);
        component.start();
    }

    @Override
    public Restlet createInboundRoot() {
        // Create a simple password verifier
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("scott", "tiger".toCharArray());

        // Create a Guard
        ChallengeAuthenticator authenticator = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_BASIC, "Tutorial");
        authenticator.setVerifier(verifier);

        // Create a Directory able to return a deep hierarchy of files
        Directory directory = new Directory(getContext(), ROOT_URI);
        directory.setListingAllowed(true);
        authenticator.setNext(directory);
        return authenticator;
    }

}
