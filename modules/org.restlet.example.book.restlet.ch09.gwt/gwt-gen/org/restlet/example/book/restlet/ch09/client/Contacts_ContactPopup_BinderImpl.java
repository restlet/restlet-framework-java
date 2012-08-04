package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.Widget;

public class Contacts_ContactPopup_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.Widget, org.restlet.example.book.restlet.ch09.client.Contacts.ContactPopup>, org.restlet.example.book.restlet.ch09.client.Contacts.ContactPopup.Binder {

  interface Template extends SafeHtmlTemplates {
    @Template("<div class='{0}'></div> <div class='{1}'> <div id='{2}'></div> <div class='{3}' id='{4}'></div> </div>")
    SafeHtml html1(String arg0, String arg1, String arg2, String arg3, String arg4);
     
  }

  Template template = GWT.create(Template.class);

  public com.google.gwt.user.client.ui.Widget createAndBindUi(final org.restlet.example.book.restlet.ch09.client.Contacts.ContactPopup owner) {

    org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenBundle.class);
    com.google.gwt.resources.client.ImageResource photo = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.photo();
    org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenCss_style style = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style();
    com.google.gwt.dom.client.DivElement nameDiv = null;
    java.lang.String domId0 = com.google.gwt.dom.client.Document.get().createUniqueId();
    com.google.gwt.dom.client.DivElement emailDiv = null;
    java.lang.String domId1 = com.google.gwt.dom.client.Document.get().createUniqueId();
    com.google.gwt.user.client.ui.HTMLPanel f_HTMLPanel1 = new com.google.gwt.user.client.ui.HTMLPanel(template.html1("" + style.photo() + "", "" + style.right() + "", domId0, "" + style.email() + "", domId1).asString());

    f_HTMLPanel1.setStyleName("" + style.popup() + "");

    UiBinderUtil.TempAttachment attachRecord0 = UiBinderUtil.attachToDom(f_HTMLPanel1.getElement());
    nameDiv = com.google.gwt.dom.client.Document.get().getElementById(domId0).cast();
    nameDiv.removeAttribute("id");
    emailDiv = com.google.gwt.dom.client.Document.get().getElementById(domId1).cast();
    emailDiv.removeAttribute("id");
    attachRecord0.detach();


    owner.emailDiv = emailDiv;
    owner.nameDiv = nameDiv;
    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style().ensureInjected();

    return f_HTMLPanel1;
  }
}
