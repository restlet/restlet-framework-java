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
