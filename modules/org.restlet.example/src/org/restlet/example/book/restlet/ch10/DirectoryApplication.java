/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
