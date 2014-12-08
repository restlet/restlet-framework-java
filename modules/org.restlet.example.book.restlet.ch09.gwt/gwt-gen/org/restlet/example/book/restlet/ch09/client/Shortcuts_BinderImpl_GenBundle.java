/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

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
