package org.restlet.ext.odata.internal;

import org.restlet.ext.odata.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FeedContentHandler extends DefaultHandler {

    private boolean bCount = false;

    private int count = -1;

    StringBuilder sb = null;

    public FeedContentHandler() {
        super();
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (bCount) {
            sb.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (bCount) {
            this.count = Integer.parseInt(sb.toString());
            bCount = false;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (Service.WCF_DATASERVICES_METADATA_NAMESPACE.equals(uri)
                && "count".equals(localName)) {
            sb = new StringBuilder();
            bCount = true;
        }
    }

    public int getCount() {
        return count;
    }

}
