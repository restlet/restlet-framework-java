package org.restlet.ext.sip;

import java.io.IOException;

import org.restlet.engine.http.header.HeaderReader;

public class OptionTagReader extends HeaderReader<OptionTag> {

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public OptionTagReader(String header) {
        super(header);
    }

    @Override
    public OptionTag readValue() throws IOException {
        String token = readToken();
        if(token != null){
            return OptionTag.valueOf(token); 
        }
        return null;
    }
}
