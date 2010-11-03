package org.restlet.example.book.restlet.misc;

import org.restlet.data.Range;
import org.restlet.resource.ClientResource;

public class RangeClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8182/");
        // Requesting the first five characters.
        resource.getRanges().add(new Range(0, 5));
        // Get the representation of the resource
        resource.get().write(System.out);
    }
}
