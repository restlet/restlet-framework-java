/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.ext.atom.internal;

import org.restlet.ext.atom.Collection;
import org.restlet.ext.atom.MemberType;
import org.restlet.ext.atom.Service;
import org.restlet.ext.atom.Workspace;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Content reader for services.
 * 
 * @author Thierry Boileau
 */
public class ServiceContentReader extends DefaultHandler {

    private final static int IN_COLLECTION = 3;

    private final static int IN_MEMBER_TYPE = 4;

    private final static int IN_NONE = 0;

    private final static int IN_SERVICE = 1;

    private final static int IN_WORKSPACE = 2;

    private StringBuilder contentBuffer = null;

    private Collection currentCollection = null;

    private Service currentService = null;

    private Workspace currentWorkspace = null;

    private int state = IN_NONE;

    /**
     * Constructor.
     * 
     * @param service
     *            The parent service.
     */
    public ServiceContentReader(Service service) {
        this.currentService = service;
    }

    /**
     * Receive notification of character data.
     * 
     * @param ch
     *            The characters from the XML document.
     * @param start
     *            The start position in the array.
     * @param length
     *            The number of characters to read from the array.
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (this.state == IN_MEMBER_TYPE) {
            this.contentBuffer.append(ch, start, length);
        }
    }

    /**
     * Receive notification of the end of a document.
     */
    @Override
    public void endDocument() throws SAXException {
        this.state = IN_NONE;
        this.currentWorkspace = null;
        this.currentCollection = null;
        this.contentBuffer = null;
    }

    /**
     * Receive notification of the end of an element.
     * 
     * @param uri
     *            The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param qName
     *            The qualified XML name (with prefix), or the empty string if
     *            qualified names are not available.
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (localName.equalsIgnoreCase("service")) {
            this.state = IN_NONE;
        } else if (localName.equalsIgnoreCase("workspace")) {
            if (this.state == IN_WORKSPACE) {
                currentService.getWorkspaces().add(this.currentWorkspace);
                this.currentWorkspace = null;
                this.state = IN_SERVICE;
            }
        } else if (localName.equalsIgnoreCase("collection")) {
            if (this.state == IN_COLLECTION) {
                this.currentWorkspace.getCollections().add(
                        this.currentCollection);
                this.currentCollection = null;
                this.state = IN_WORKSPACE;
            }
        } else if (localName.equalsIgnoreCase("member-type")) {
            if (this.state == IN_MEMBER_TYPE) {
                final String memberType = this.contentBuffer.toString();

                if (memberType.equalsIgnoreCase("entry")) {
                    this.currentCollection.setMemberType(MemberType.ENTRY);
                } else if (memberType.equalsIgnoreCase("media")) {
                    this.currentCollection.setMemberType(MemberType.MEDIA);
                }

                this.state = IN_COLLECTION;
            }
        }
    }

    /**
     * Receive notification of the beginning of a document.
     */
    @Override
    public void startDocument() throws SAXException {
        this.state = IN_NONE;
        this.currentWorkspace = null;
        this.currentCollection = null;
        this.contentBuffer = null;
    }

    /**
     * Receive notification of the beginning of an element.
     * 
     * @param uri
     *            The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param qName
     *            The qualified name (with prefix), or the empty string if
     *            qualified names are not available.
     * @param attrs
     *            The attributes attached to the element. If there are no
     *            attributes, it shall be an empty Attributes object. The value
     *            of this object after startElement returns is undefined.
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        if (uri.equalsIgnoreCase(Service.APP_NAMESPACE)) {
            if (localName.equalsIgnoreCase("service")) {
                this.state = IN_SERVICE;
            } else if (localName.equalsIgnoreCase("workspace")) {
                if (this.state == IN_SERVICE) {
                    this.currentWorkspace = new Workspace(this.currentService,
                            attrs.getValue("title"));
                    this.state = IN_WORKSPACE;
                }
            } else if (localName.equalsIgnoreCase("collection")) {
                if (this.state == IN_WORKSPACE) {
                    this.currentCollection = new Collection(
                            this.currentWorkspace, attrs.getValue("title"),
                            attrs.getValue("href"));
                    this.state = IN_COLLECTION;
                }
            } else if (localName.equalsIgnoreCase("member-type")) {
                if (this.state == IN_COLLECTION) {
                    this.contentBuffer = new StringBuilder();
                    this.state = IN_MEMBER_TYPE;
                }
            }
        }
    }
}