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
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.Widget;

public class MailDetail_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.Widget, org.restlet.example.book.restlet.ch09.client.MailDetail>, org.restlet.example.book.restlet.ch09.client.MailDetail.Binder {

  interface Template extends SafeHtmlTemplates {
    @Template("<div class='{0}' id='{1}'></div> <div class='{2}'><b>From:</b> <span id='{3}'></span></div> <div class='{4}'><b>To:</b> <span id='{5}'></span></div>")
    SafeHtml html1(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5);
     
  }

  Template template = GWT.create(Template.class);

  public com.google.gwt.user.client.ui.Widget createAndBindUi(final org.restlet.example.book.restlet.ch09.client.MailDetail owner) {

    org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenBundle.class);
    org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenCss_style style = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style();
    com.google.gwt.dom.client.DivElement subject = null;
    java.lang.String domId0 = com.google.gwt.dom.client.Document.get().createUniqueId();
    com.google.gwt.dom.client.SpanElement sender = null;
    java.lang.String domId1 = com.google.gwt.dom.client.Document.get().createUniqueId();
    com.google.gwt.dom.client.SpanElement recipient = null;
    java.lang.String domId2 = com.google.gwt.dom.client.Document.get().createUniqueId();
    com.google.gwt.user.client.ui.HTMLPanel f_HTMLPanel2 = new com.google.gwt.user.client.ui.HTMLPanel(template.html1("" + style.headerItem() + "", domId0, "" + style.headerItem() + "", domId1, "" + style.headerItem() + "", domId2).asString());
    com.google.gwt.user.client.ui.HTML body = (com.google.gwt.user.client.ui.HTML) GWT.create(com.google.gwt.user.client.ui.HTML.class);
    com.google.gwt.user.client.ui.ScrollPanel f_ScrollPanel3 = (com.google.gwt.user.client.ui.ScrollPanel) GWT.create(com.google.gwt.user.client.ui.ScrollPanel.class);
    com.google.gwt.user.client.ui.DockLayoutPanel f_DockLayoutPanel1 = new com.google.gwt.user.client.ui.DockLayoutPanel(com.google.gwt.dom.client.Style.Unit.EM);

    f_HTMLPanel2.setStyleName("" + style.header() + "");
    f_DockLayoutPanel1.addNorth(f_HTMLPanel2, 6);
    body.setStyleName("" + style.body() + "");
    body.setWordWrap(true);
    f_ScrollPanel3.add(body);
    f_DockLayoutPanel1.add(f_ScrollPanel3);
    f_DockLayoutPanel1.setStyleName("" + style.detail() + "");

    UiBinderUtil.TempAttachment attachRecord0 = UiBinderUtil.attachToDom(f_HTMLPanel2.getElement());
    subject = com.google.gwt.dom.client.Document.get().getElementById(domId0).cast();
    subject.removeAttribute("id");
    sender = com.google.gwt.dom.client.Document.get().getElementById(domId1).cast();
    sender.removeAttribute("id");
    recipient = com.google.gwt.dom.client.Document.get().getElementById(domId2).cast();
    recipient.removeAttribute("id");
    attachRecord0.detach();


    owner.body = body;
    owner.recipient = recipient;
    owner.sender = sender;
    owner.subject = subject;
    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style().ensureInjected();

    return f_DockLayoutPanel1;
  }
}
