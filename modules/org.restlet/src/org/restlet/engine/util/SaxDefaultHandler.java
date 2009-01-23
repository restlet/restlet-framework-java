package org.restlet.engine.util;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.helpers.DefaultHandler;

public class SaxDefaultHandler extends DefaultHandler implements
        LSResourceResolver {

    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        return null;
    }

}
