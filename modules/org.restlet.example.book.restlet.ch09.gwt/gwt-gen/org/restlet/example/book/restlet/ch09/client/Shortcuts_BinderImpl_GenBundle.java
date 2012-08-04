package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.CssResource.Import;

public interface Shortcuts_BinderImpl_GenBundle extends ClientBundle {
  @Source("uibinder:org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenCss_style.css")
  Shortcuts_BinderImpl_GenCss_style style();

  @Source("mailboxesgroup_ie6.gif")
  DataResource mailboxesgroupIe6Data();

  @Source("tasksgroup_ie6.gif")
  DataResource tasksgroupIe6Data();

  @Source("contactsgroup_ie6.gif")
  DataResource contactsgroupIe6Data();

  @Source("mailboxesgroup.png")
  ImageResource mailboxesgroup();
  @Source("contactsgroup.png")
  ImageResource contactsgroup();
  @Source("tasksgroup.png")
  ImageResource tasksgroup();
  @Source("gradient_bg_dark.png")
  @ImageOptions(repeatStyle=ImageResource.RepeatStyle.Horizontal)
  ImageResource gradient();
}
