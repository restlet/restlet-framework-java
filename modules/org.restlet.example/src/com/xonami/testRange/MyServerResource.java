package com.xonami.testRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class MyServerResource extends ServerResource {

    @Get
    public Representation toRep() {
        final int size = 1623;
        final Vector<URL> urls = new Vector<URL>();
        try {
            for (URL u : new URL[] {
                    new URL(
                            "http://mm04.nasaimages.org/MediaManager/srvr?mediafile=/Size1/NVA2-8-NA/15614/full_tif.jpg&userid=1&username=admin&resolution=1&servertype=JVA&cid=8&iid=NVA2&vcid=NA&usergroup=NASA_Hubble_Space_Telescope_Collection-8-Admin&profileid=36"),
                    new URL(
                            "http://mm04.nasaimages.org/MediaManager/srvr?mediafile=/Size1/nasaNAS-4-NA/17226/PIA09579.jpg&userid=1&username=admin&resolution=1&servertype=JVA&cid=4&iid=nasaNAS&vcid=NA&usergroup=photojournal_%28nasa%29_3-4-Admin&profileid=16"), }) {
                urls.add(u);
            }
        } catch (MalformedURLException mue) {
            throw new RuntimeException(mue);
        }

        OutputRepresentation os = new OutputRepresentation(
                MediaType.APPLICATION_OCTET_STREAM) {
            @Override
            public void write(OutputStream outputStream) throws IOException {
                Iterator<URL> urlIt = urls.iterator();
                byte[] b = new byte[1024];
//                long cur = 0;
//                long amt = 0;
                
                try {
                    while (urlIt.hasNext()) {
                        InputStream is = urlIt.next().openStream();
                        try {
                            while (true) {
                                int r;
                                r = is.read(b);
                                if (r == -1)
                                    break;
                                // cur += r;
                                // amt += r;
                                outputStream.write(b, 0, r);
                            }
                        } finally {
                            is.close();
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    throw ioe;
                } finally {
                    outputStream.close();
                    // long s = chaine.length * multiple;
                    // if( amt != s )
                    // throw new IOException( "Error reading requested data: " +
                    // s );
                }
            }
        };
        os.setSize(size);
        return os;
    }
}
