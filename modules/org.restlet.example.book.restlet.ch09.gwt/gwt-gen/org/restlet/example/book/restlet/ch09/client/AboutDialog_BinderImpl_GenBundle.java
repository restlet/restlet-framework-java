package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.CssResource.Import;

public interface AboutDialog_BinderImpl_GenBundle extends ClientBundle {
  @Source("uibinder:org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenCss_style.css")
  AboutDialog_BinderImpl_GenCss_style style();

  @Source("gwt-logo.png")
  ImageResource logo();
}
