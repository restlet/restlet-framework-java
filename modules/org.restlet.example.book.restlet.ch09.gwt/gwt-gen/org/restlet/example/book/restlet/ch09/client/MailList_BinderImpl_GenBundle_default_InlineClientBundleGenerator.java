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

public class MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenBundle {
  private static MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator _instance0 = new MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator();
  private void gradientInitializer() {
    gradient = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "gradient",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage),
      0, 0, 1, 64, false, true
    );
  }
  private static class gradientInitializer {
    static {
      _instance0.gradientInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return gradient;
    }
  }
  public com.google.gwt.resources.client.ImageResource gradient() {
    return gradientInitializer.get();
  }
  private void selectionStyleInitializer() {
    selectionStyle = new org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_selectionStyle() {
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
        return "selectionStyle";
      }
      public String getText() {
        return (".GL0P3EKCKI{background:" + ("#adcce7")  + ";}.GL0P3EKCKI td{border-top:" + ("1px"+ " " +"solid"+ " " +"#88a4d6")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#7b97d0")  + ";}");
      }
      public java.lang.String selectedRow(){
        return "GL0P3EKCKI";
      }
    }
    ;
  }
  private static class selectionStyleInitializer {
    static {
      _instance0.selectionStyleInitializer();
    }
    static org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_selectionStyle get() {
      return selectionStyle;
    }
  }
  public org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_selectionStyle selectionStyle() {
    return selectionStyleInitializer.get();
  }
  private void styleInitializer() {
    style = new org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_style() {
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
        return "style";
      }
      public String getText() {
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? ((".GL0P3EKCMI{border-right:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";cursor:" + ("pointer")  + ";cursor:" + ("hand")  + ";}.GL0P3EKCLI{height:" + ((MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getHeight() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getSafeUri().asString() + "\") -" + (MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getLeft() + "px -" + (MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getTop() + "px  repeat-x")  + ";background-color:" + ("#d3d6dd")  + ";table-layout:" + ("fixed")  + ";width:" + ("100%")  + ";height:") + (("100%")  + ";}.GL0P3EKCLI td{font-weight:" + ("bold")  + ";text-shadow:" + ("#fff"+ " " +"0"+ " " +"2px"+ " " +"2px")  + ";padding:" + ("2px"+ " " +"10px"+ " " +"1px"+ " " +"0")  + ";border-top:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";}.GL0P3EKCNI{table-layout:" + ("fixed")  + ";width:" + ("100%")  + ";}.GL0P3EKCNI td{border-top:" + ("1px"+ " " +"solid"+ " " +"#fff")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#fff")  + ";padding:" + ("2px"+ " " +"10px"+ " " +"2px"+ " " +"0") ) + (";}")) : ((".GL0P3EKCMI{border-left:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";cursor:" + ("pointer")  + ";cursor:" + ("hand")  + ";}.GL0P3EKCLI{height:" + ((MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getHeight() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getSafeUri().asString() + "\") -" + (MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getLeft() + "px -" + (MailList_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.gradient()).getTop() + "px  repeat-x")  + ";background-color:" + ("#d3d6dd")  + ";table-layout:" + ("fixed")  + ";width:" + ("100%")  + ";height:") + (("100%")  + ";}.GL0P3EKCLI td{font-weight:" + ("bold")  + ";text-shadow:" + ("#fff"+ " " +"0"+ " " +"2px"+ " " +"2px")  + ";padding:" + ("2px"+ " " +"0"+ " " +"1px"+ " " +"10px")  + ";border-top:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";}.GL0P3EKCNI{table-layout:" + ("fixed")  + ";width:" + ("100%")  + ";}.GL0P3EKCNI td{border-top:" + ("1px"+ " " +"solid"+ " " +"#fff")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#fff")  + ";padding:" + ("2px"+ " " +"0"+ " " +"2px"+ " " +"10px") ) + (";}"));
      }
      public java.lang.String header(){
        return "GL0P3EKCLI";
      }
      public java.lang.String outer(){
        return "GL0P3EKCMI";
      }
      public java.lang.String table(){
        return "GL0P3EKCNI";
      }
    }
    ;
  }
  private static class styleInitializer {
    static {
      _instance0.styleInitializer();
    }
    static org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_style get() {
      return style;
    }
  }
  public org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_style style() {
    return styleInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String externalImage = "data:image/png;base64,/9j/4AAQSkZJRgABAgAAZABkAAD/7AARRHVja3kAAQAEAAAAZAAA/+4ADkFkb2JlAGTAAAAAAf/bAIQAAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQICAgICAgICAgICAwMDAwMDAwMDAwEBAQEBAQECAQECAgIBAgIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMD/8AAEQgAQAABAwERAAIRAQMRAf/EAGUAAQEBAAAAAAAAAAAAAAAAAAYFCgEBAQEBAAAAAAAAAAAAAAAAAgMBABAAAgECBgMAAAAAAAAAAAAAAAEU8FGRobHRAmJSohMRAAICAgMBAAAAAAAAAAAAAAABERIhUfBh4QL/2gAMAwEAAhEDEQA/ANNkjtkLHH4YRZap8RV6OlBmVx8tBV+tBlBmT2eW4pYcaQZmKmitUSsE5Tv7FsaDZBuS7aCqwyQvs71gPJtGf//Z";
  private static com.google.gwt.resources.client.ImageResource gradient;
  private static org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_selectionStyle selectionStyle;
  private static org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenCss_style style;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      gradient(), 
      selectionStyle(), 
      style(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("gradient", gradient());
        resourceMap.put("selectionStyle", selectionStyle());
        resourceMap.put("style", style());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'gradient': return this.@org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenBundle::gradient()();
      case 'selectionStyle': return this.@org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenBundle::selectionStyle()();
      case 'style': return this.@org.restlet.example.book.restlet.ch09.client.MailList_BinderImpl_GenBundle::style()();
    }
    return null;
  }-*/;
}
