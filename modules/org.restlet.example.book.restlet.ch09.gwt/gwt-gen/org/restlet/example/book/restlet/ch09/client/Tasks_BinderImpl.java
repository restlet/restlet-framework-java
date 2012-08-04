package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.Widget;

public class Tasks_BinderImpl implements UiBinder<com.google.gwt.user.client.ui.Widget, org.restlet.example.book.restlet.ch09.client.Tasks>, org.restlet.example.book.restlet.ch09.client.Tasks.Binder {

  interface Template extends SafeHtmlTemplates {
    @Template("Get groceries")
    SafeHtml html1();
     
    @Template("Walk the dog")
    SafeHtml html2();
     
    @Template("Start Web 2.0 company")
    SafeHtml html3();
     
    @Template("Write cool app in GWT")
    SafeHtml html4();
     
    @Template("Get funding")
    SafeHtml html5();
     
    @Template("Take a vacation")
    SafeHtml html6();
     
  }

  Template template = GWT.create(Template.class);

  public com.google.gwt.user.client.ui.Widget createAndBindUi(final org.restlet.example.book.restlet.ch09.client.Tasks owner) {

    org.restlet.example.book.restlet.ch09.client.Tasks_BinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (org.restlet.example.book.restlet.ch09.client.Tasks_BinderImpl_GenBundle) GWT.create(org.restlet.example.book.restlet.ch09.client.Tasks_BinderImpl_GenBundle.class);
    org.restlet.example.book.restlet.ch09.client.Tasks_BinderImpl_GenCss_style style = clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style();
    com.google.gwt.user.client.ui.CheckBox f_CheckBox2 = (com.google.gwt.user.client.ui.CheckBox) GWT.create(com.google.gwt.user.client.ui.CheckBox.class);
    com.google.gwt.user.client.ui.CheckBox f_CheckBox3 = (com.google.gwt.user.client.ui.CheckBox) GWT.create(com.google.gwt.user.client.ui.CheckBox.class);
    com.google.gwt.user.client.ui.CheckBox f_CheckBox4 = (com.google.gwt.user.client.ui.CheckBox) GWT.create(com.google.gwt.user.client.ui.CheckBox.class);
    com.google.gwt.user.client.ui.CheckBox f_CheckBox5 = (com.google.gwt.user.client.ui.CheckBox) GWT.create(com.google.gwt.user.client.ui.CheckBox.class);
    com.google.gwt.user.client.ui.CheckBox f_CheckBox6 = (com.google.gwt.user.client.ui.CheckBox) GWT.create(com.google.gwt.user.client.ui.CheckBox.class);
    com.google.gwt.user.client.ui.CheckBox f_CheckBox7 = (com.google.gwt.user.client.ui.CheckBox) GWT.create(com.google.gwt.user.client.ui.CheckBox.class);
    com.google.gwt.user.client.ui.FlowPanel f_FlowPanel1 = (com.google.gwt.user.client.ui.FlowPanel) GWT.create(com.google.gwt.user.client.ui.FlowPanel.class);

    f_CheckBox2.setHTML(template.html1().asString());
    f_CheckBox2.setStyleName("" + style.item() + "");
    f_FlowPanel1.add(f_CheckBox2);
    f_CheckBox3.setHTML(template.html2().asString());
    f_CheckBox3.setStyleName("" + style.item() + "");
    f_FlowPanel1.add(f_CheckBox3);
    f_CheckBox4.setHTML(template.html3().asString());
    f_CheckBox4.setStyleName("" + style.item() + "");
    f_FlowPanel1.add(f_CheckBox4);
    f_CheckBox5.setHTML(template.html4().asString());
    f_CheckBox5.setStyleName("" + style.item() + "");
    f_FlowPanel1.add(f_CheckBox5);
    f_CheckBox6.setHTML(template.html5().asString());
    f_CheckBox6.setStyleName("" + style.item() + "");
    f_FlowPanel1.add(f_CheckBox6);
    f_CheckBox7.setHTML(template.html6().asString());
    f_CheckBox7.setStyleName("" + style.item() + "");
    f_FlowPanel1.add(f_CheckBox7);
    f_FlowPanel1.setStyleName("" + style.tasks() + "");



    clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay.style().ensureInjected();

    return f_FlowPanel1;
  }
}
