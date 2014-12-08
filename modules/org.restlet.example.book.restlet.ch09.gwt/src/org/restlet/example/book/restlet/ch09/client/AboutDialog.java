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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple example of an 'about' dialog box.
 */
public class AboutDialog extends DialogBox {

  interface Binder extends UiBinder<Widget, AboutDialog> { }
  private static final Binder binder = GWT.create(Binder.class);

  @UiField Button closeButton;

  public AboutDialog() {
    // Use this opportunity to set the dialog's caption.
    setText("About the Mail Sample");
    setWidget(binder.createAndBindUi(this));

    setAnimationEnabled(true);
    setGlassEnabled(true);
  }

  @Override
  protected void onPreviewNativeEvent(NativePreviewEvent preview) {
    super.onPreviewNativeEvent(preview);

    NativeEvent evt = preview.getNativeEvent();
    if (evt.getType().equals("keydown")) {
      // Use the popup's key preview hooks to close the dialog when either
      // enter or escape is pressed.
      switch (evt.getKeyCode()) {
        case KeyCodes.KEY_ENTER:
        case KeyCodes.KEY_ESCAPE:
          hide();
          break;
      }
    }
  }

  @UiHandler("closeButton")
  void onSignOutClicked(ClickEvent event) {
    hide();
  }
}
