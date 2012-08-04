package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class Mail_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.DockLayoutPanel, org.restlet.example.book.restlet.ch09.client.Mail>, org.restlet.example.book.restlet.ch09.client.Mail.Binder {

  public com.google.gwt.user.client.ui.DockLayoutPanel createAndBindUi(final org.restlet.example.book.restlet.ch09.client.Mail owner) {

    org.restlet.example.book.restlet.ch09.client.Mail_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.Mail_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.Mail_BinderImpl_GenBundle.class);
    org.restlet.example.book.restlet.ch09.client.TopPanel topPanel = (org.restlet.example.book.restlet.ch09.client.TopPanel) GWT.create(org.restlet.example.book.restlet.ch09.client.TopPanel.class);
    org.restlet.example.book.restlet.ch09.client.Shortcuts shortcuts = (org.restlet.example.book.restlet.ch09.client.Shortcuts) GWT.create(org.restlet.example.book.restlet.ch09.client.Shortcuts.class);
    org.restlet.example.book.restlet.ch09.client.MailList mailList = (org.restlet.example.book.restlet.ch09.client.MailList) GWT.create(org.restlet.example.book.restlet.ch09.client.MailList.class);
    org.restlet.example.book.restlet.ch09.client.MailDetail mailDetail = (org.restlet.example.book.restlet.ch09.client.MailDetail) GWT.create(org.restlet.example.book.restlet.ch09.client.MailDetail.class);
    com.google.gwt.user.client.ui.SplitLayoutPanel f_SplitLayoutPanel2 = (com.google.gwt.user.client.ui.SplitLayoutPanel) GWT.create(com.google.gwt.user.client.ui.SplitLayoutPanel.class);
    com.google.gwt.user.client.ui.DockLayoutPanel f_DockLayoutPanel1 = new com.google.gwt.user.client.ui.DockLayoutPanel(com.google.gwt.dom.client.Style.Unit.EM);

    f_DockLayoutPanel1.addNorth(topPanel, 5);
    f_SplitLayoutPanel2.addWest(shortcuts, 192);
    f_SplitLayoutPanel2.addNorth(mailList, 200);
    f_SplitLayoutPanel2.add(mailDetail);
    f_DockLayoutPanel1.add(f_SplitLayoutPanel2);



    owner.mailDetail = mailDetail;
    owner.mailList = mailList;
    owner.shortcuts = shortcuts;
    owner.topPanel = topPanel;

    return f_DockLayoutPanel1;
  }
}
