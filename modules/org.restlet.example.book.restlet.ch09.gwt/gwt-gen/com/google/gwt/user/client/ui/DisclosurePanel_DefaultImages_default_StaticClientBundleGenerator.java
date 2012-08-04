package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class DisclosurePanel_DefaultImages_default_StaticClientBundleGenerator implements com.google.gwt.user.client.ui.DisclosurePanel.DefaultImages {
  private static DisclosurePanel_DefaultImages_default_StaticClientBundleGenerator _instance0 = new DisclosurePanel_DefaultImages_default_StaticClientBundleGenerator();
  private void disclosurePanelClosedInitializer() {
    disclosurePanelClosed = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "disclosurePanelClosed",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ?bundledImage_None_rtl : bundledImage_None),
      16, 0, 16, 16, false, false
    );
  }
  private static class disclosurePanelClosedInitializer {
    static {
      _instance0.disclosurePanelClosedInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return disclosurePanelClosed;
    }
  }
  public com.google.gwt.resources.client.ImageResource disclosurePanelClosed() {
    return disclosurePanelClosedInitializer.get();
  }
  private void disclosurePanelOpenInitializer() {
    disclosurePanelOpen = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "disclosurePanelOpen",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ?bundledImage_None_rtl : bundledImage_None),
      0, 0, 16, 16, false, false
    );
  }
  private static class disclosurePanelOpenInitializer {
    static {
      _instance0.disclosurePanelOpenInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return disclosurePanelOpen;
    }
  }
  public com.google.gwt.resources.client.ImageResource disclosurePanelOpen() {
    return disclosurePanelOpenInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String bundledImage_None = GWT.getModuleBaseURL() + "E44767377485D18D6B6864F65BA8EF73.cache.png";
  private static final java.lang.String bundledImage_None_rtl = GWT.getModuleBaseURL() + "0A9476898799A150D840F0B1C3672921.cache.png";
  private static com.google.gwt.resources.client.ImageResource disclosurePanelClosed;
  private static com.google.gwt.resources.client.ImageResource disclosurePanelOpen;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      disclosurePanelClosed(), 
      disclosurePanelOpen(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("disclosurePanelClosed", disclosurePanelClosed());
        resourceMap.put("disclosurePanelOpen", disclosurePanelOpen());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'disclosurePanelClosed': return this.@com.google.gwt.user.client.ui.DisclosurePanel.DefaultImages::disclosurePanelClosed()();
      case 'disclosurePanelOpen': return this.@com.google.gwt.user.client.ui.DisclosurePanel.DefaultImages::disclosurePanelOpen()();
    }
    return null;
  }-*/;
}
