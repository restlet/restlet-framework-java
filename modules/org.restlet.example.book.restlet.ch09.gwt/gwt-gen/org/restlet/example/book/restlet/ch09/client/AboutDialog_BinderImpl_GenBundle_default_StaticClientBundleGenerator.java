package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.AboutDialog_BinderImpl_GenBundle {
  private static AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator _instance0 = new AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator();
  private void logoInitializer() {
    logo = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "logo",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
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
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? ((".GL0P3EKCCK{padding:" + ("10px")  + ";}.GL0P3EKCPJ{text-align:" + ("right")  + ";}.GL0P3EKCBK{height:" + ((AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getHeight() + "px")  + ";width:" + ((AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getWidth() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getSafeUri().asString() + "\") -" + (AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getLeft() + "px -" + (AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getTop() + "px  no-repeat")  + ";float:" + ("right")  + ";padding-left:" + ("1em")  + ";}.GL0P3EKCAK{text-align:" + ("left")  + ";}")) : ((".GL0P3EKCCK{padding:" + ("10px")  + ";}.GL0P3EKCPJ{text-align:" + ("left")  + ";}.GL0P3EKCBK{height:" + ((AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getHeight() + "px")  + ";width:" + ((AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getWidth() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getSafeUri().asString() + "\") -" + (AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getLeft() + "px -" + (AboutDialog_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.logo()).getTop() + "px  no-repeat")  + ";float:" + ("left")  + ";padding-right:" + ("1em")  + ";}.GL0P3EKCAK{text-align:" + ("right")  + ";}"));
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
  private static final java.lang.String bundledImage_None = GWT.getModuleBaseURL() + "196F0C6B8AACCA73AFF0D609FFAB5378.cache.png";
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
