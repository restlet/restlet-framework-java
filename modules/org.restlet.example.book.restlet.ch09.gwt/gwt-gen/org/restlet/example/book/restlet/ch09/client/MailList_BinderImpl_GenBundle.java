package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.CssResource.Import;

public interface MailList_BinderImpl_GenBundle extends ClientBundle {
  @Source("uibinder:org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_style.css")
  MailList_BinderImpl_GenCss_style style();

  @Source("uibinder:org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_selectionStyle.css")
  MailList_BinderImpl_GenCss_selectionStyle selectionStyle();

  @Source("gradient_bg_dark.png")
  @ImageOptions(repeatStyle=ImageResource.RepeatStyle.Horizontal)
  ImageResource gradient();
}
