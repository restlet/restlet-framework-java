package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenBundle {
  private static Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator _instance0 = new Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator();
  private void photoInitializer() {
    photo = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "photo",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage),
      0, 0, 32, 32, false, true
    );
  }
  private static class photoInitializer {
    static {
      _instance0.photoInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return photo;
    }
  }
  public com.google.gwt.resources.client.ImageResource photo() {
    return photoInitializer.get();
  }
  private void styleInitializer() {
    style = new org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenCss_style() {
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
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? ((".GL0P3EKCLJ{background:" + ("#fff")  + ";border:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";padding:" + ("0.5em")  + ";width:" + ("14em")  + ";height:" + ("2.5em")  + ";}.GL0P3EKCKJ{height:" + ((Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getHeight() + "px")  + ";width:" + ((Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getWidth() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getSafeUri().asString() + "\") -" + (Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getLeft() + "px -" + (Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getTop() + "px  no-repeat")  + ";float:" + ("right")  + ";margin-left:") + (("4px")  + ";}.GL0P3EKCMJ{white-space:" + ("nowrap")  + ";}.GL0P3EKCJJ{font-style:" + ("italic")  + ";}")) : ((".GL0P3EKCLJ{background:" + ("#fff")  + ";border:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";padding:" + ("0.5em")  + ";width:" + ("14em")  + ";height:" + ("2.5em")  + ";}.GL0P3EKCKJ{height:" + ((Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getHeight() + "px")  + ";width:" + ((Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getWidth() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getSafeUri().asString() + "\") -" + (Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getLeft() + "px -" + (Contacts_ContactPopup_BinderImpl_GenBundle_default_StaticClientBundleGenerator.this.photo()).getTop() + "px  no-repeat")  + ";float:" + ("left")  + ";margin-right:") + (("4px")  + ";}.GL0P3EKCMJ{white-space:" + ("nowrap")  + ";}.GL0P3EKCJJ{font-style:" + ("italic")  + ";}"));
      }
      public java.lang.String email(){
        return "GL0P3EKCJJ";
      }
      public java.lang.String photo(){
        return "GL0P3EKCKJ";
      }
      public java.lang.String popup(){
        return "GL0P3EKCLJ";
      }
      public java.lang.String right(){
        return "GL0P3EKCMJ";
      }
    }
    ;
  }
  private static class styleInitializer {
    static {
      _instance0.styleInitializer();
    }
    static org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenCss_style get() {
      return style;
    }
  }
  public org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenCss_style style() {
    return styleInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String externalImage = GWT.getModuleBaseURL() + "51BDF5B93509AA1F1BC6DF3080013499.cache.jpg";
  private static com.google.gwt.resources.client.ImageResource photo;
  private static org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenCss_style style;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      photo(), 
      style(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("photo", photo());
        resourceMap.put("style", style());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'photo': return this.@org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenBundle::photo()();
      case 'style': return this.@org.restlet.example.book.restlet.ch09.client.Contacts_ContactPopup_BinderImpl_GenBundle::style()();
    }
    return null;
  }-*/;
}
