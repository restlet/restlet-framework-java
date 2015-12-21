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
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.Widget;

public class MailList_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.Widget, org.restlet.example.book.restlet.ch09.client.MailList>, org.restlet.example.book.restlet.ch09.client.MailList.Binder {

  public com.google.gwt.user.client.ui.Widget createAndBindUi(final org.restlet.example.book.restlet.ch09.client.MailList owner) {

    org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenBundle.class);
    com.google.gwt.resources.client.ImageResource gradient = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.gradient();
    org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_style style = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style();
    org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_selectionStyle selectionStyle = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.selectionStyle();
    com.google.gwt.user.client.ui.FlexTable header = (com.google.gwt.user.client.ui.FlexTable) GWT.create(com.google.gwt.user.client.ui.FlexTable.class);
    com.google.gwt.user.client.ui.FlexTable table = (com.google.gwt.user.client.ui.FlexTable) GWT.create(com.google.gwt.user.client.ui.FlexTable.class);
    com.google.gwt.user.client.ui.ScrollPanel f_ScrollPanel2 = (com.google.gwt.user.client.ui.ScrollPanel) GWT.create(com.google.gwt.user.client.ui.ScrollPanel.class);
    com.google.gwt.user.client.ui.DockLayoutPanel f_DockLayoutPanel1 = new com.google.gwt.user.client.ui.DockLayoutPanel(com.google.gwt.dom.client.Style.Unit.EM);

    header.setStyleName("" + style.header() + "");
    header.setCellSpacing(0);
    header.setCellPadding(0);
    f_DockLayoutPanel1.addNorth(header, 2);
    table.setStyleName("" + style.table() + "");
    table.setCellSpacing(0);
    table.setCellPadding(0);
    f_ScrollPanel2.add(table);
    f_DockLayoutPanel1.add(f_ScrollPanel2);
    f_DockLayoutPanel1.setStyleName("" + style.outer() + "");



    final com.google.gwt.event.dom.client.ClickHandler handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1 = new com.google.gwt.event.dom.client.ClickHandler() {
      public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
        owner.onTableClicked(event);
      }
    };
    table.addClickHandler(handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1);

    owner.header = header;
    owner.selectionStyle = selectionStyle;
    owner.table = table;
    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style().ensureInjected();
    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.selectionStyle().ensureInjected();

    return f_DockLayoutPanel1;
  }
}
