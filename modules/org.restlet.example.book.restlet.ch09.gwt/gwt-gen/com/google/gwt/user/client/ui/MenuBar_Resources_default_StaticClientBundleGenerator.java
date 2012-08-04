package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class MenuBar_Resources_default_StaticClientBundleGenerator implements com.google.gwt.user.client.ui.MenuBar.Resources {
  private static MenuBar_Resources_default_StaticClientBundleGenerator _instance0 = new MenuBar_Resources_default_StaticClientBundleGenerator();
  private void menuBarSubMenuIconInitializer() {
    menuBarSubMenuIcon = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "menuBarSubMenuIcon",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ?bundledImage_None_rtl : bundledImage_None),
      0, 0, 5, 9, false, false
    );
  }
  private static class menuBarSubMenuIconInitializer {
    static {
      _instance0.menuBarSubMenuIconInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return menuBarSubMenuIcon;
    }
  }
  public com.google.gwt.resources.client.ImageResource menuBarSubMenuIcon() {
    return menuBarSubMenuIconInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String bundledImage_None = GWT.getModuleBaseURL() + "DF7764EEC1903CD03C9545B354D8D8E4.cache.png";
  private static final java.lang.String bundledImage_None_rtl = GWT.getModuleBaseURL() + "396F806CD63ABD414BFBB9D57429F05B.cache.png";
  private static com.google.gwt.resources.client.ImageResource menuBarSubMenuIcon;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      menuBarSubMenuIcon(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("menuBarSubMenuIcon", menuBarSubMenuIcon());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'menuBarSubMenuIcon': return this.@com.google.gwt.user.client.ui.MenuBar.Resources::menuBarSubMenuIcon()();
    }
    return null;
  }-*/;
}
