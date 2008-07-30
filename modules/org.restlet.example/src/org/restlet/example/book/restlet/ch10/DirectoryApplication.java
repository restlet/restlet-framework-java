/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;

/**
 *
 */
public class DirectoryApplication extends Application {

    /**
     * Constructor.
     */
    public DirectoryApplication() {
        // Sets the facultative name of the application.
        setName("directoryApplication");
        // the application requires the following client connector.
        getConnectorService().getClientProtocols().add(Protocol.FILE);

        // Update the metadataService by setting preferred mappings
        // between extension and metadata
        // this.getMetadataService().addExtension("js",
        // MediaType.APPLICATION_JSON, true);
        // this.getMetadataService().addExtension("xml",
        // MediaType.APPLICATION_ATOM_XML, true);
    }

    @Override
    public Restlet createRoot() {
        // Instantiates the Directory with the path of the root directory
        final Directory directory = new Directory(getContext(), LocalReference
                .createFileReference("d:\\temp"));
        // Make sure the content negotiation is activated.
        directory.setNegotiateContent(true);

        // Filter all calls targeting the directory
        return new TweakingClientFilter(getContext(), directory);
    }
}
