/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.restlet.example.book.restlet.ch09.client;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.MediaType;
import org.restlet.client.ext.json.JsonRepresentation;
import org.restlet.client.ext.xml.DomRepresentation;
import org.restlet.client.resource.ClientResource;
import org.restlet.client.resource.Result;
import org.restlet.example.book.restlet.ch09.common.ContactRepresentation;
import org.restlet.example.book.restlet.ch09.common.ContactsRepresentation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;

/**
 * A component that displays a list of contacts.
 */
public class Contacts extends Composite {

    /**
     * A simple popup that displays a contact's information.
     */
    static class ContactPopup extends PopupPanel {
        @UiTemplate("ContactPopup.ui.xml")
        interface Binder extends UiBinder<Widget, ContactPopup> {
        }

        private static final Binder binder = GWT.create(Binder.class);

        @UiField
        Element nameDiv;

        @UiField
        Element emailDiv;

        public ContactPopup(ContactRepresentation contact) {
            // The popup's constructor's argument is a boolean specifying that
            // it
            // auto-close itself when the user clicks outside of it.
            super(true);
            add(binder.createAndBindUi(this));

            nameDiv.setInnerText(contact.getSenderName());
            emailDiv.setInnerText(contact.getEmail());
        }
    }

    interface Binder extends UiBinder<Widget, Contacts> {
    }

    interface Style extends CssResource {
        String item();
    }

    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    ComplexPanel panel;

    @UiField
    Style style;

    public Contacts() {
        initWidget(binder.createAndBindUi(this));

        // Remotely retrieve the contacts list in JSON format
        ClientResource clientResource = new ClientResource(
                "/accounts/chunkylover53/contacts/");
        clientResource.setOnResponse(new Uniform() {
            public void handle(Request request, Response response) {
                try {
                    JsonRepresentation representation = new JsonRepresentation(
                            response.getEntity());
                    JSONArray jsonContacts = (JSONArray) representation
                            .getValue();

                    for (int i = 0; i < jsonContacts.size(); i++) {
                        JSONObject jsonContact = (JSONObject) jsonContacts
                                .get(i);
                        ContactRepresentation contact = new ContactRepresentation();
                        contact.setFirstName(((JSONString) jsonContact
                                .get("firstName")).stringValue());
                        contact.setLastName(((JSONString) jsonContact
                                .get("lastName")).stringValue());
                        contact.setEmail(((JSONString) jsonContact.get("email"))
                                .stringValue());
                        contact.setLogin(((JSONString) jsonContact.get("login"))
                                .stringValue());
                        contact.setSenderName(((JSONString) jsonContact
                                .get("senderName")).stringValue());
                    }
                } catch (Exception ex) {
                    GWT.log("Unable to parse JSON", ex);
                }
            }
        });
        clientResource.get(MediaType.APPLICATION_JSON);

        // Remotely retrieve the contacts list in XML format (based on XStream
        // which doesn't work on GAE)
        clientResource.setOnResponse(new Uniform() {
            public void handle(Request request, Response response) {
                try {
                    DomRepresentation representation = new DomRepresentation(
                            response.getEntity());

                    Document document = representation.getDocument();
                    com.google.gwt.xml.client.Element listElement = (com.google.gwt.xml.client.Element) document
                            .getFirstChild();
                    NodeList nodes = listElement
                            .getElementsByTagName("org.restlet.example.book.restlet.ch09.common.ContactRepresentation");

                    for (int i = 0; i < nodes.getLength(); i++) {
                        com.google.gwt.xml.client.Element contactElement = (com.google.gwt.xml.client.Element) nodes
                                .item(i);

                        ContactRepresentation contact = new ContactRepresentation();
                        Element contactFirstNameElement = (Element) contactElement
                                .getElementsByTagName("firstName").item(0);
                        contact.setFirstName(contactFirstNameElement
                                .getFirstChild().getNodeValue());
                        Element contactLastNameElement = (Element) contactElement
                                .getElementsByTagName("lastName").item(0);
                        contact.setLastName(contactLastNameElement
                                .getFirstChild().getNodeValue());
                        Element contactEmailElement = (Element) contactElement
                                .getElementsByTagName("email").item(0);
                        contact.setEmail(contactEmailElement.getFirstChild()
                                .getNodeValue());
                        Element contactLoginElement = (Element) contactElement
                                .getElementsByTagName("login").item(0);
                        contact.setLogin(contactLoginElement.getFirstChild()
                                .getNodeValue());
                        Element contactSenderNameElement = (Element) contactElement
                                .getElementsByTagName("senderName").item(0);
                        contact.setSenderName(contactSenderNameElement
                                .getFirstChild().getNodeValue());
                    }
                } catch (Exception ex) {
                    GWT.log("Unable to parse XML", ex);
                }
            }
        });
        clientResource.get(MediaType.APPLICATION_XML);

        // Remotely retrieve the contacts list in GWT serialization format
        ContactsResourceProxy client = GWT.create(ContactsResourceProxy.class);
        client.getClientResource().setReference(
                "/accounts/chunkylover53/contacts/");
        client.retrieve(new Result<ContactsRepresentation>() {

            @Override
            public void onSuccess(ContactsRepresentation result) {
                // Add all the contacts to the UI list.
                for (ContactRepresentation contact : result.getContacts()) {
                    addContact(contact);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Unable to retrieve the contacts list");
            }
        });
    }

    private void addContact(final ContactRepresentation contact) {
        final Anchor link = new Anchor(contact.getSenderName());
        link.setStyleName(style.item());
        panel.add(link);

        // Add a click handler that displays a ContactPopup when it is clicked.
        link.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                ContactPopup popup = new ContactPopup(contact);
                int left = link.getAbsoluteLeft() + 14;
                int top = link.getAbsoluteTop() + 14;
                popup.setPopupPosition(left, top);
                popup.show();
            }
        });
    }
}
