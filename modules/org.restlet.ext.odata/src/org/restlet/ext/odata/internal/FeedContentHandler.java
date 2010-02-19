/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.odata.internal;

import org.restlet.ext.odata.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Content handler for Atom Feed that takes care of odata specific needs, such
 * as parsing XML content from other namespaces than Atom.
 * 
 * @author Thierry Boileau
 * 
 */
public class FeedContentHandler extends DefaultHandler {

    /** True if we are parsing the count tag. */
    private boolean bCount = false;

    /** The value retrieved from the "count" tag. */
    private int count = -1;

    /** Gleans text content. */
    StringBuilder sb = null;

    /**
     * Constructor.
     */
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

    /**
     * Returns the value of the "count" tag, that is to say the size of the
     * current entity set.
     * 
     * @return The size of the current entity set, as specified by the Atom
     *         document.
     */
    public int getCount() {
        return count;
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

}
