package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.Widget;

public class TopPanel_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.Widget, org.restlet.example.book.restlet.ch09.client.TopPanel>, org.restlet.example.book.restlet.ch09.client.TopPanel.Binder {

  interface Template extends SafeHtmlTemplates {
    @Template("Sign Out")
    SafeHtml html1();
     
    @Template("About")
    SafeHtml html2();
     
    @Template("<div class='{0}'></div> <div class='{1}'> <div> <b>Welcome back, foo@example.com</b> </div> <div class='{2}'> <span id='{3}'></span>   <span id='{4}'></span> </div> </div>")
    SafeHtml html3(String arg0, String arg1, String arg2, String arg3, String arg4);
     
  }

  Template template = GWT.create(Template.class);

  public com.google.gwt.user.client.ui.Widget createAndBindUi(final org.restlet.example.book.restlet.ch09.client.TopPanel owner) {

    org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenBundle.class);
    com.google.gwt.resources.client.ImageResource logo = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.logo();
    com.google.gwt.resources.client.DataResource logoIe6Data = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.logoIe6Data();
    org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenCss_style style = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style();
    java.lang.String domId0 = com.google.gwt.dom.client.Document.get().createUniqueId();
    com.google.gwt.user.client.ui.Anchor signOutLink = (com.google.gwt.user.client.ui.Anchor) GWT.create(com.google.gwt.user.client.ui.Anchor.class);
    java.lang.String domId1 = com.google.gwt.dom.client.Document.get().createUniqueId();
    com.google.gwt.user.client.ui.Anchor aboutLink = (com.google.gwt.user.client.ui.Anchor) GWT.create(com.google.gwt.user.client.ui.Anchor.class);
    com.google.gwt.user.client.ui.HTMLPanel f_HTMLPanel1 = new com.google.gwt.user.client.ui.HTMLPanel(template.html3("" + style.logo() + "", "" + style.statusDiv() + "", "" + style.linksDiv() + "", domId0, domId1).asString());

    signOutLink.setHTML(template.html1().asString());
    signOutLink.setHref("javascript:;");
    aboutLink.setHTML(template.html2().asString());
    aboutLink.setHref("javascript:;");

    UiBinderUtil.TempAttachment attachRecord0 = UiBinderUtil.attachToDom(f_HTMLPanel1.getElement());
    com.google.gwt.user.client.Element domId0Element = com.google.gwt.dom.client.Document.get().getElementById(domId0).cast();
    com.google.gwt.user.client.Element domId1Element = com.google.gwt.dom.client.Document.get().getElementById(domId1).cast();
    attachRecord0.detach();
    f_HTMLPanel1.addAndReplaceElement(signOutLink, domId0Element);
    f_HTMLPanel1.addAndReplaceElement(aboutLink, domId1Element);


    final com.google.gwt.event.dom.client.ClickHandler handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1 = new com.google.gwt.event.dom.client.ClickHandler() {
      public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
        owner.onAboutClicked(event);
      }
    };
    aboutLink.addClickHandler(handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1);

    final com.google.gwt.event.dom.client.ClickHandler handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames2 = new com.google.gwt.event.dom.client.ClickHandler() {
      public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
        owner.onSignOutClicked(event);
      }
    };
    signOutLink.addClickHandler(handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames2);

    owner.aboutLink = aboutLink;
    owner.signOutLink = signOutLink;
    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style().ensureInjected();

    return f_HTMLPanel1;
  }
}
