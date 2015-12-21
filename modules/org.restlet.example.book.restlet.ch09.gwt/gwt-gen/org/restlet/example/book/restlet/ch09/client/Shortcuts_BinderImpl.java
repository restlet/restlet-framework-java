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
import com.google.gwt.user.client.ui.StackLayoutPanel;

public class Shortcuts_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.StackLayoutPanel, org.restlet.example.book.restlet.ch09.client.Shortcuts>, org.restlet.example.book.restlet.ch09.client.Shortcuts.Binder {

  interface Template extends SafeHtmlTemplates {
    @Template("<div class='{0}'><div class='{1}'></div> Mailboxes</div>")
    SafeHtml html1(String arg0, String arg1);
     
    @Template("<div class='{0}'><div class='{1}'></div> Tasks</div>")
    SafeHtml html2(String arg0, String arg1);
     
    @Template("<div class='{0}'><div class='{1}'></div> Contacts</div>")
    SafeHtml html3(String arg0, String arg1);
     
  }

  Template template = GWT.create(Template.class);

  public com.google.gwt.user.client.ui.StackLayoutPanel createAndBindUi(final org.restlet.example.book.restlet.ch09.client.Shortcuts owner) {

    org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle.class);
    com.google.gwt.resources.client.DataResource mailboxesgroupIe6Data = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.mailboxesgroupIe6Data();
    com.google.gwt.resources.client.DataResource tasksgroupIe6Data = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.tasksgroupIe6Data();
    com.google.gwt.resources.client.DataResource contactsgroupIe6Data = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.contactsgroupIe6Data();
    org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenCss_style style = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style();
    com.google.gwt.resources.client.ImageResource mailboxesgroup = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.mailboxesgroup();
    com.google.gwt.resources.client.ImageResource contactsgroup = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.contactsgroup();
    com.google.gwt.resources.client.ImageResource tasksgroup = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.tasksgroup();
    com.google.gwt.resources.client.ImageResource gradient = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.gradient();
    org.restlet.example.book.restlet.ch09.client.Mailboxes mailboxes = (org.restlet.example.book.restlet.ch09.client.Mailboxes) GWT.create(org.restlet.example.book.restlet.ch09.client.Mailboxes.class);
    org.restlet.example.book.restlet.ch09.client.Tasks tasks = (org.restlet.example.book.restlet.ch09.client.Tasks) GWT.create(org.restlet.example.book.restlet.ch09.client.Tasks.class);
    org.restlet.example.book.restlet.ch09.client.Contacts contacts = (org.restlet.example.book.restlet.ch09.client.Contacts) GWT.create(org.restlet.example.book.restlet.ch09.client.Contacts.class);
    com.google.gwt.user.client.ui.StackLayoutPanel f_StackLayoutPanel1 = new com.google.gwt.user.client.ui.StackLayoutPanel(com.google.gwt.dom.client.Style.Unit.EM);

    f_StackLayoutPanel1.add(mailboxes, template.html1("" + style.stackHeader() + "", "" + style.mailboxesIcon() + "").asString(), true, 3);
    f_StackLayoutPanel1.add(tasks, template.html2("" + style.stackHeader() + "", "" + style.tasksIcon() + "").asString(), true, 3);
    f_StackLayoutPanel1.add(contacts, template.html3("" + style.stackHeader() + "", "" + style.contactsIcon() + "").asString(), true, 3);
    f_StackLayoutPanel1.setStyleName("" + style.shortcuts() + "");



    owner.contacts = contacts;
    owner.mailboxes = mailboxes;
    owner.tasks = tasks;
    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style().ensureInjected();

    return f_StackLayoutPanel1;
  }
}
