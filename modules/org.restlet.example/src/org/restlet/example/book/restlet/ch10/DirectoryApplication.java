package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;

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
