package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class Tree_Resources_default_StaticClientBundleGenerator implements com.google.gwt.user.client.ui.Tree.Resources {
  private static Tree_Resources_default_StaticClientBundleGenerator _instance0 = new Tree_Resources_default_StaticClientBundleGenerator();
  private void treeClosedInitializer() {
    treeClosed = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeClosed",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      32, 0, 16, 16, false, false
    );
  }
  private static class treeClosedInitializer {
    static {
      _instance0.treeClosedInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return treeClosed;
    }
  }
  public com.google.gwt.resources.client.ImageResource treeClosed() {
    return treeClosedInitializer.get();
  }
  private void treeLeafInitializer() {
    treeLeaf = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeLeaf",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      16, 0, 16, 16, false, false
    );
  }
  private static class treeLeafInitializer {
    static {
      _instance0.treeLeafInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return treeLeaf;
    }
  }
  public com.google.gwt.resources.client.ImageResource treeLeaf() {
    return treeLeafInitializer.get();
  }
  private void treeOpenInitializer() {
    treeOpen = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeOpen",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      0, 0, 16, 16, false, false
    );
  }
  private static class treeOpenInitializer {
    static {
      _instance0.treeOpenInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return treeOpen;
    }
  }
  public com.google.gwt.resources.client.ImageResource treeOpen() {
    return treeOpenInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String bundledImage_None = GWT.getModuleBaseURL() + "EDC7827FEEA59EE44AD790B1C6430C45.cache.png";
  private static com.google.gwt.resources.client.ImageResource treeClosed;
  private static com.google.gwt.resources.client.ImageResource treeLeaf;
  private static com.google.gwt.resources.client.ImageResource treeOpen;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      treeClosed(), 
      treeLeaf(), 
      treeOpen(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("treeClosed", treeClosed());
        resourceMap.put("treeLeaf", treeLeaf());
        resourceMap.put("treeOpen", treeOpen());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'treeClosed': return this.@com.google.gwt.user.client.ui.Tree.Resources::treeClosed()();
      case 'treeLeaf': return this.@com.google.gwt.user.client.ui.Tree.Resources::treeLeaf()();
      case 'treeOpen': return this.@com.google.gwt.user.client.ui.Tree.Resources::treeOpen()();
    }
    return null;
  }-*/;
}
