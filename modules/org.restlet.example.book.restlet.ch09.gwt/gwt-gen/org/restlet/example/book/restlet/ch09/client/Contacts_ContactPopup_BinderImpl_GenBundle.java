package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.CssResource.Import;

public interface Contacts_ContactPopup_BinderImpl_GenBundle extends ClientBundle {
  @Source("uibinder:org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenCss_style.css")
  Contacts_ContactPopup_BinderImpl_GenCss_style style();

  @Source("default_photo.jpg")
  ImageResource photo();
}
