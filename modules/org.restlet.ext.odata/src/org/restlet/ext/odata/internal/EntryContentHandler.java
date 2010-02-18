package org.restlet.ext.odata.internal;

import java.io.IOException;
import java.util.List;

import org.restlet.ext.odata.internal.edm.Metadata;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class EntryContentHandler<T> extends DefaultHandler {

    private enum State {
        ENTRY, ENTRY_CONTENT, ENTRY_LINK, ENTRY_SOURCE, ENTRY_SOURCE_LINK, FEED_ENTRY_SOURCE_TITLE, FEED_ENTRY_SOURCE_UPDATED, FEED_ENTRY_SUMMARY, FEED_ENTRY_TITLE, FEED_ENTRY_UPDATED, FEED_GENERATOR, FEED_ICON, FEED_ID, FEED_LINK, FEED_LOGO, FEED_RIGHTS, FEED_SUBTITLE, FEED_TITLE, FEED_UPDATED, NONE
    }
    
    /** Buffer for the current text content of the current tag. */
    private StringBuilder contentBuffer;

    /** The list of parsed entities. */
    private List<T> entities;

    /** The class of the entity targeted by this kind of entry. */
    private Class<?> entityClass;

    /** The metadata of the WCF service. */
    private Metadata metadata;

    /**
     * Contructor.
     * 
     * @param metadata
     *            The metadata of the WCF service.
     * @param entityClass
     *            The class of the entity targeted by this kind of entry.
     */
    public EntryContentHandler(Metadata metadata, Class<?> entityClass) {
        super();
        this.metadata = metadata;
        this.entityClass = entityClass;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
        super.characters(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub
        super.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // TODO Auto-generated method stub
        super.endElement(uri, localName, qName);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // TODO Auto-generated method stub
        super.endPrefixMapping(prefix);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub
        super.error(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub
        super.fatalError(e);
    }

    /**
     * Returns the list of parsed entities.
     * 
     * @return The list of parsed entities.
     */
    public List<T> getEntities() {
        return entities;
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
        // TODO Auto-generated method stub
        super.notationDecl(name, publicId, systemId);
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        // TODO Auto-generated method stub
        super.processingInstruction(target, data);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        // TODO Auto-generated method stub
        return super.resolveEntity(publicId, systemId);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // TODO Auto-generated method stub
        super.setDocumentLocator(locator);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        // TODO Auto-generated method stub
        super.skippedEntity(name);
    }

    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        // TODO Auto-generated method stub
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        // TODO Auto-generated method stub
        super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
        // TODO Auto-generated method stub
        super.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub
        super.warning(e);
    }

}
