package org.restlet.example.book.restlet.ch07.sec2.server;

import org.restlet.data.MediaType;
import org.restlet.example.book.restlet.ch03.sect5.sub5.common.RootResource;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Root resource implementation.
 */
public class RootServerResource extends WadlServerResource implements
        RootResource {

    @Override
    protected RepresentationInfo describe(MethodInfo methodInfo,
            Class<?> representationClass, Variant variant) {
        RepresentationInfo result = new RepresentationInfo(MediaType.TEXT_PLAIN);
        result.setIdentifier("root");

        DocumentationInfo doc = new DocumentationInfo();
        doc.setTitle("Mail application");
        doc
                .setTextContent("Simple string welcoming the user to the mail application");
        result.getDocumentations().add(doc);
        return result;
    }

    @Override
    protected void doInit() throws ResourceException {
        setAutoDescribing(false);
        setName("Root resource");
        setDescription("The root resource of the mail server application");
    }

    public String represent() {
        return "Welcome to the " + getApplication().getName() + " !";
    }

}
