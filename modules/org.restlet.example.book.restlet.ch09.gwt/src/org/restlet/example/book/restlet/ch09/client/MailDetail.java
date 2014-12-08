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

package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A composite for displaying the details of an email message.
 */
public class MailDetail extends ResizeComposite {

  interface Binder extends UiBinder<Widget, MailDetail> { }
  private static final Binder binder = GWT.create(Binder.class);

  @UiField Element subject;
  @UiField Element sender;
  @UiField Element recipient;
  @UiField HTML body;

  public MailDetail() {
    initWidget(binder.createAndBindUi(this));
  }

  public void setItem(MailItem item) {
    subject.setInnerText(item.subject);
    sender.setInnerText(item.sender);
    recipient.setInnerHTML("foo@example.com");

    // WARNING: For the purposes of this demo, we're using HTML directly, on
    // the assumption that the "server" would have appropriately scrubbed the
    // HTML. Failure to do so would open your application to XSS attacks.
    body.setHTML(item.body);
  }
}
