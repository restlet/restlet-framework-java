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
import com.google.gwt.resources.client.ResourcePrototype;

public class Mail_GlobalResources_default_InlineClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.Mail.GlobalResources {
  private static Mail_GlobalResources_default_InlineClientBundleGenerator _instance0 = new Mail_GlobalResources_default_InlineClientBundleGenerator();
  private void cssInitializer() {
    css = new com.google.gwt.resources.client.CssResource() {
      private boolean injected;
      public boolean ensureInjected() {
        if (!injected) {
          injected = true;
          com.google.gwt.dom.client.StyleInjector.inject(getText());
          return true;
        }
        return false;
      }
      public String getName() {
        return "css";
      }
      public String getText() {
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? (("body{font-size:" + ("small")  + ";font-family:" + ("Helvetica"+ ","+ " " +"Arial"+ ","+ " " +"sans-serif")  + ";color:" + ("#000")  + ";background:" + ("#fff")  + ";}table{font-size:" + ("small")  + ";}a:link,a:visited,a:hover,a:active{color:" + ("#000")  + ";}.dialogTopLeftInner,.dialogMiddleLeftInner,.dialogBottomLeftInner,.dialogTopRightInner,.dialogMiddleRightInner,.dialogBottomRightInner{display:" + ("none")  + ";}.gwt-DialogBox{background-color:" + ("white")  + ";border:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";z-index:" + ("2")  + ";}.gwt-DialogBox .Caption{background:") + (("#d3d6dd"+ " " +"url(gradient_bg_th.png)"+ " " +"repeat-x"+ " " +"bottom"+ " " +"right")  + ";font-weight:" + ("bold")  + ";text-shadow:" + ("#fff"+ " " +"0"+ " " +"2px"+ " " +"2px")  + ";cursor:" + ("default")  + ";padding:" + ("5px"+ " " +"10px")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";text-align:" + ("right")  + ";}.gwt-DialogBox .gwt-Button{margin:" + ("10px")  + ";}.gwt-PopupPanelGlass{background-color:" + ("#000")  + ";opacity:" + ("0.3")  + ";filter:" + ("alpha(opacity=30)") ) + (";z-index:" + ("2")  + ";}.gwt-Tree .gwt-TreeItem{padding:" + ("0")  + ";cursor:" + ("hand")  + ";cursor:" + ("pointer")  + ";display:" + ("block")  + " !important;}.gwt-Tree .gwt-TreeItem-selected{background:" + ("#ccc")  + ";}.gwt-SplitLayoutPanel-HDragger{background-color:" + ("white")  + ";cursor:" + ("col-resize")  + ";}.gwt-SplitLayoutPanel-VDragger{background-color:" + ("white")  + ";cursor:" + ("row-resize")  + ";}")) : (("body{font-size:" + ("small")  + ";font-family:" + ("Helvetica"+ ","+ " " +"Arial"+ ","+ " " +"sans-serif")  + ";color:" + ("#000")  + ";background:" + ("#fff")  + ";}table{font-size:" + ("small")  + ";}a:link,a:visited,a:hover,a:active{color:" + ("#000")  + ";}.dialogTopLeftInner,.dialogMiddleLeftInner,.dialogBottomLeftInner,.dialogTopRightInner,.dialogMiddleRightInner,.dialogBottomRightInner{display:" + ("none")  + ";}.gwt-DialogBox{background-color:" + ("white")  + ";border:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";z-index:" + ("2")  + ";}.gwt-DialogBox .Caption{background:") + (("#d3d6dd"+ " " +"url(gradient_bg_th.png)"+ " " +"repeat-x"+ " " +"bottom"+ " " +"left")  + ";font-weight:" + ("bold")  + ";text-shadow:" + ("#fff"+ " " +"0"+ " " +"2px"+ " " +"2px")  + ";cursor:" + ("default")  + ";padding:" + ("5px"+ " " +"10px")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";text-align:" + ("left")  + ";}.gwt-DialogBox .gwt-Button{margin:" + ("10px")  + ";}.gwt-PopupPanelGlass{background-color:" + ("#000")  + ";opacity:" + ("0.3")  + ";filter:" + ("alpha(opacity=30)") ) + (";z-index:" + ("2")  + ";}.gwt-Tree .gwt-TreeItem{padding:" + ("0")  + ";cursor:" + ("hand")  + ";cursor:" + ("pointer")  + ";display:" + ("block")  + " !important;}.gwt-Tree .gwt-TreeItem-selected{background:" + ("#ccc")  + ";}.gwt-SplitLayoutPanel-HDragger{background-color:" + ("white")  + ";cursor:" + ("col-resize")  + ";}.gwt-SplitLayoutPanel-VDragger{background-color:" + ("white")  + ";cursor:" + ("row-resize")  + ";}"));
      }
    }
    ;
  }
  private static class cssInitializer {
    static {
      _instance0.cssInitializer();
    }
    static com.google.gwt.resources.client.CssResource get() {
      return css;
    }
  }
  public com.google.gwt.resources.client.CssResource css() {
    return cssInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static com.google.gwt.resources.client.CssResource css;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      css(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("css", css());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'css': return this.@org.restlet.example.book.restlet.ch09.client.Mail.GlobalResources::css()();
    }
    return null;
  }-*/;
}
