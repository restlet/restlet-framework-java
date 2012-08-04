package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.Widget;

public class Contacts_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.Widget, org.restlet.example.book.restlet.ch09.client.Contacts>, org.restlet.example.book.restlet.ch09.client.Contacts.Binder {

  public com.google.gwt.user.client.ui.Widget createAndBindUi(final org.restlet.example.book.restlet.ch09.client.Contacts owner) {

    org.restlet.example.book.restlet.ch09.client.Contacts_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.Contacts_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.Contacts_BinderImpl_GenBundle.class);
    org.restlet.example.book.restlet.ch09.client.Contacts_BinderImpl_GenCss_style style = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style();
    com.google.gwt.user.client.ui.FlowPanel panel = (com.google.gwt.user.client.ui.FlowPanel) GWT.create(com.google.gwt.user.client.ui.FlowPanel.class);

    panel.setStyleName("" + style.contacts() + "");



    owner.panel = panel;
    owner.style = style;
    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style().ensureInjected();

    return panel;
  }
}
