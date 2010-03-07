package org.restlet.example.book.restlet.misc;

import java.security.NoSuchAlgorithmException;

import org.restlet.representation.DigesterRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class VerifiedServerResource extends ServerResource {

    @Get
    public Representation represent() throws NoSuchAlgorithmException {
        // Wraps the StringRepresentation
        DigesterRepresentation result = new DigesterRepresentation(
                new StringRepresentation("hello, world"));
        // Compute's representation's digest.
        result.setDigest(result.computeDigest());

        return result;
    }
}
