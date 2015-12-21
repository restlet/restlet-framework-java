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

public class AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenBundle {
  private static AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator _instance0 = new AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator();
  private void logoInitializer() {
    logo = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "logo",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage),
      0, 0, 100, 100, false, false
    );
  }
  private static class logoInitializer {
    static {
      _instance0.logoInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return logo;
    }
  }
  public com.google.gwt.resources.client.ImageResource logo() {
    return logoInitializer.get();
  }
  private void styleInitializer() {
    style = new org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenCss_style() {
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
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? ((".GL0P3EKCCK{padding:" + ("10px")  + ";}.GL0P3EKCPJ{text-align:" + ("right")  + ";}.GL0P3EKCBK{height:" + ((AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getHeight() + "px")  + ";width:" + ((AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getWidth() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getSafeUri().asString() + "\") -" + (AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getLeft() + "px -" + (AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getTop() + "px  no-repeat")  + ";float:" + ("right")  + ";padding-left:" + ("1em")  + ";}.GL0P3EKCAK{text-align:" + ("left")  + ";}")) : ((".GL0P3EKCCK{padding:" + ("10px")  + ";}.GL0P3EKCPJ{text-align:" + ("left")  + ";}.GL0P3EKCBK{height:" + ((AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getHeight() + "px")  + ";width:" + ((AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getWidth() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getSafeUri().asString() + "\") -" + (AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getLeft() + "px -" + (AboutDialog_BinderImpl_GenBundle_default_InlineClientBundleGenerator.this.logo()).getTop() + "px  no-repeat")  + ";float:" + ("left")  + ";padding-right:" + ("1em")  + ";}.GL0P3EKCAK{text-align:" + ("right")  + ";}"));
      }
      public java.lang.String aboutText(){
        return "GL0P3EKCPJ";
      }
      public java.lang.String buttons(){
        return "GL0P3EKCAK";
      }
      public java.lang.String logo(){
        return "GL0P3EKCBK";
      }
      public java.lang.String panel(){
        return "GL0P3EKCCK";
      }
    }
    ;
  }
  private static class styleInitializer {
    static {
      _instance0.styleInitializer();
    }
    static org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenCss_style get() {
      return style;
    }
  }
  public org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenCss_style style() {
    return styleInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String externalImage = "data:image/png;base64,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";
  private static com.google.gwt.resources.client.ImageResource logo;
  private static org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenCss_style style;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      logo(), 
      style(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("logo", logo());
        resourceMap.put("style", style());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'logo': return this.@org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenBundle::logo()();
      case 'style': return this.@org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenBundle::style()();
    }
    return null;
  }-*/;
}
