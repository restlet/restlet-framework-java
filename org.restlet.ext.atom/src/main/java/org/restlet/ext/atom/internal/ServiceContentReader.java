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

package org.restlet.ext.atom.internal;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Collection;
import org.restlet.ext.atom.Feed;
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

    private final static int IN_ACCEPT = 1;

    private final static int IN_COLLECTION = 2;

    private final static int IN_COLLECTION_TITLE = 3;

    private final static int IN_NONE = 0;

    private final static int IN_SERVICE = 4;

    private final static int IN_WORKSPACE = 5;

    private final static int IN_WORKSPACE_TITLE = 6;

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

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if ((this.state == IN_ACCEPT) || (this.state == IN_COLLECTION_TITLE)
                || (this.state == IN_WORKSPACE_TITLE)) {
            this.contentBuffer.append(ch, start, length);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        this.state = IN_NONE;
        this.currentWorkspace = null;
        this.currentCollection = null;
        this.contentBuffer = null;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (uri.equalsIgnoreCase(Service.APP_NAMESPACE)) {
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
            } else if (localName.equalsIgnoreCase("accept")) {
                if (this.state == IN_ACCEPT) {
                    List<MediaType> mediaTypes = null;
                    String accept = this.contentBuffer.toString();

                    if ((accept != null) && (accept.length() > 0)) {
                        String[] acceptTokens = accept.split(",");
                        mediaTypes = new ArrayList<MediaType>();

                        for (String acceptToken : acceptTokens) {
                            mediaTypes.add(MediaType.valueOf(acceptToken));
                        }
                    }

                    this.currentCollection.setAccept(mediaTypes);
                    this.state = IN_COLLECTION;
                }
            }
        } else if (uri.equalsIgnoreCase(Feed.ATOM_NAMESPACE)) {
            if (localName.equalsIgnoreCase("title")) {
                if (this.state == IN_COLLECTION_TITLE) {
                    String title = this.contentBuffer.toString();
                    this.currentCollection.setTitle(title);
                    this.state = IN_COLLECTION;
                } else if (this.state == IN_WORKSPACE_TITLE) {
                    String title = this.contentBuffer.toString();
                    this.currentWorkspace.setTitle(title);
                    this.state = IN_WORKSPACE;
                }
            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        this.state = IN_NONE;
        this.currentWorkspace = null;
        this.currentCollection = null;
        this.contentBuffer = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attrs) throws SAXException {
        if (uri.equalsIgnoreCase(Service.APP_NAMESPACE)) {
            if (localName.equalsIgnoreCase("service")) {
                String attr = attrs.getValue("xml:base");
                if (attr != null) {
                    this.currentService.setBaseReference(new Reference(attr));
                }
                this.state = IN_SERVICE;
            } else if (localName.equalsIgnoreCase("workspace")) {
                if (this.state == IN_SERVICE) {
                    this.currentWorkspace = new Workspace(this.currentService);
                    String attr = attrs.getValue("xml:base");
                    if (attr != null) {
                        this.currentWorkspace.setBaseReference(new Reference(
                                attr));
                    }
                    this.state = IN_WORKSPACE;
                }
            } else if (localName.equalsIgnoreCase("collection")) {
                if (this.state == IN_WORKSPACE) {
                    this.currentCollection = new Collection(
                            this.currentWorkspace, attrs.getValue("title"),
                            attrs.getValue("href"));
                    String attr = attrs.getValue("xml:base");
                    if (attr != null) {
                        this.currentCollection.setBaseReference(new Reference(
                                attr));
                    }

                    this.state = IN_COLLECTION;
                }
            } else if (localName.equalsIgnoreCase("accept")) {
                if (this.state == IN_COLLECTION) {
                    this.contentBuffer = new StringBuilder();
                    this.state = IN_ACCEPT;
                }
            }
        } else if (uri.equalsIgnoreCase(Feed.ATOM_NAMESPACE)) {
            if (localName.equalsIgnoreCase("title")) {
                if (this.state == IN_COLLECTION) {
                    this.contentBuffer = new StringBuilder();
                    this.state = IN_COLLECTION_TITLE;
                } else if (this.state == IN_WORKSPACE) {
                    this.contentBuffer = new StringBuilder();
                    this.state = IN_WORKSPACE_TITLE;
                }
            }
        }
    }
}
