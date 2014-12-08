/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.atom;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Content reader for entries that is able to transmit events to another
 * EntryReader.
 * 
 * @author Thierry Boileau
 */
public class EntryReader extends DefaultHandler {

    /** Extra entry reader. */
    private EntryReader entryReader;

    /**
     * Constructor.
     */
    public EntryReader() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param entryReader
     *            Additional feed reader that will receive all events.
     */
    public EntryReader(EntryReader entryReader) {
        super();
        this.entryReader = entryReader;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.characters(ch, start, length);
        }
    }

    /**
     * Called at the end of the XML block that defines the given content
     * element. By default, it relays the event to the extra handler.
     * 
     * @param content
     *            The current content element.
     */
    public void endContent(Content content) {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.endContent(content);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.endDocument();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.endElement(uri, localName, qName);
        }
    }

    /**
     * Called at the end of the XML block that defines the given entry.
     * 
     * @param entry
     *            The current entry.
     */
    public void endEntry(Entry entry) {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.endEntry(entry);
        }
    }

    /**
     * Called at the end of the XML block that defines the given link.
     * 
     * @param link
     *            The current link.
     */
    public void endLink(Link link) {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.endLink(link);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.endPrefixMapping(prefix);
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.error(e);
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.fatalError(e);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.notationDecl(name, publicId, systemId);
        }
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.processingInstruction(target, data);
        }
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            return this.entryReader.resolveEntity(publicId, systemId);
        }
        return null;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.setDocumentLocator(locator);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.skippedEntity(name);
        }
    }

    /**
     * Called when a new content element has been detected in the Atom document.
     * 
     * @param content
     *            The current content element.
     */
    public void startContent(Content content) {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.startContent(content);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.startDocument();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.startElement(uri, localName, qName, attributes);
        }
    }

    /**
     * Called when a new entry has been detected in the Atom document.
     * 
     * @param entry
     *            The current entry.
     */
    public void startEntry(Entry entry) {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.startEntry(entry);
        }
    }

    /**
     * Called when a new link has been detected in the Atom document.
     * 
     * @param link
     *            The current link.
     */
    public void startLink(Link link) {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.startLink(link);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.startPrefixMapping(prefix, uri);
        }
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.unparsedEntityDecl(name, publicId, systemId,
                    notationName);
        }
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        // Send the event to the extra handler.
        if (this.entryReader != null) {
            this.entryReader.warning(e);
        }
    }
}
