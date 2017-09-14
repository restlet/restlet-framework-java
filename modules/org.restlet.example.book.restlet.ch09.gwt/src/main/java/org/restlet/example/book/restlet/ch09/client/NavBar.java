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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple widget representing prev/next page navigation.
 */
class NavBar extends Composite {
  @UiTemplate("NavBar.ui.xml")
  interface Binder extends UiBinder<Widget, NavBar> { }
  private static final Binder binder = GWT.create(Binder.class);

  @UiField Element countLabel;
  @UiField Anchor newerButton;
  @UiField Anchor olderButton;

  private final MailList outer;

  public NavBar(MailList outer) {
    initWidget(binder.createAndBindUi(this));
    this.outer = outer;
  }

  public void update(int startIndex, int count, int max) {
    setVisibility(newerButton, startIndex != 0);
    setVisibility(olderButton,
        startIndex + MailList.VISIBLE_EMAIL_COUNT < count);
    countLabel.setInnerText("" + (startIndex + 1) + " - " + max + " of "
        + count);
  }

  @UiHandler("newerButton")
  void onNewerClicked(ClickEvent event) {
    outer.newer();
  }

  @UiHandler("olderButton")
  void onOlderClicked(ClickEvent event) {
    outer.older();
  }

  private void setVisibility(Widget widget, boolean visible) {
    widget.getElement().getStyle().setVisibility(
        visible ? Visibility.VISIBLE : Visibility.HIDDEN);
  }
}
