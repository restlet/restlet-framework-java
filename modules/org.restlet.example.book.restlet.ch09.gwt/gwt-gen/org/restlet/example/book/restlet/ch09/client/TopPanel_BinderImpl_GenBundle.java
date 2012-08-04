package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.CssResource.Import;

public interface TopPanel_BinderImpl_GenBundle extends ClientBundle {
  @Source("uibinder:org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenCss_style.css")
  TopPanel_BinderImpl_GenCss_style style();

  @Source("logo_ie6.gif")
  DataResource logoIe6Data();

  @Source("logo.png")
  ImageResource logo();
}
