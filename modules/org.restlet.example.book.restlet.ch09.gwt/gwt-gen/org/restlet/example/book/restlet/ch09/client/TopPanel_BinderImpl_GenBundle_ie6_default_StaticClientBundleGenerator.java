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

public class TopPanel_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenBundle {
  private static TopPanel_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator _instance0 = new TopPanel_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator();
  private void logoInitializer() {
    logo = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "logo",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      0, 0, 140, 75, false, false
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
  private void logoIe6DataInitializer() {
    logoIe6Data = // file:/C:/Projets/framework/java-2.1/modules/org.restlet.example.book.restlet.ch09.gwt/src/org/restlet/example/book/restlet/ch09/client/logo_ie6.gif
    new com.google.gwt.resources.client.impl.DataResourcePrototype(
      "logoIe6Data",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(GWT.getModuleBaseURL() + "C4943CC50481C035E92103C8CAB306AF.cache.gif")
    );
  }
  private static class logoIe6DataInitializer {
    static {
      _instance0.logoIe6DataInitializer();
    }
    static com.google.gwt.resources.client.DataResource get() {
      return logoIe6Data;
    }
  }
  public com.google.gwt.resources.client.DataResource logoIe6Data() {
    return logoIe6DataInitializer.get();
  }
  private void styleInitializer() {
    style = new org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenCss_style() {
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
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? ((".GL0P3EKCFJ{text-align:" + ("left")  + ";margin:" + ("1em")  + ";}.GL0P3EKCDJ{text-align:" + ("left")  + ";}.GL0P3EKCEJ{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(TopPanel_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.logoIe6Data())).getUrl() + "')")  + ";width:" + ("140px")  + ";height:" + ("75px")  + ";position:" + ("absolute")  + ";}")) : ((".GL0P3EKCFJ{text-align:" + ("right")  + ";margin:" + ("1em")  + ";}.GL0P3EKCDJ{text-align:" + ("right")  + ";}.GL0P3EKCEJ{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(TopPanel_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.logoIe6Data())).getUrl() + "')")  + ";width:" + ("140px")  + ";height:" + ("75px")  + ";position:" + ("absolute")  + ";}"));
      }
      public java.lang.String linksDiv(){
        return "GL0P3EKCDJ";
      }
      public java.lang.String logo(){
        return "GL0P3EKCEJ";
      }
      public java.lang.String statusDiv(){
        return "GL0P3EKCFJ";
      }
    }
    ;
  }
  private static class styleInitializer {
    static {
      _instance0.styleInitializer();
    }
    static org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenCss_style get() {
      return style;
    }
  }
  public org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenCss_style style() {
    return styleInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String bundledImage_None = GWT.getModuleBaseURL() + "3BC9B6E44E4771F2F85B9094A2DA6416.cache.png";
  private static com.google.gwt.resources.client.ImageResource logo;
  private static com.google.gwt.resources.client.DataResource logoIe6Data;
  private static org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenCss_style style;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      logo(), 
      logoIe6Data(), 
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
        resourceMap.put("logoIe6Data", logoIe6Data());
        resourceMap.put("style", style());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'logo': return this.@org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenBundle::logo()();
      case 'logoIe6Data': return this.@org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenBundle::logoIe6Data()();
      case 'style': return this.@org.restlet.example.book.restlet.ch09.client.TopPanel_BinderImpl_GenBundle::style()();
    }
    return null;
  }-*/;
}
