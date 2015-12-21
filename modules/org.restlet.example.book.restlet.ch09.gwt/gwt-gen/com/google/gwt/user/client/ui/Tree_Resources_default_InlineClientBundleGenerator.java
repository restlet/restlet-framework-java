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

package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class Tree_Resources_default_InlineClientBundleGenerator implements com.google.gwt.user.client.ui.Tree.Resources {
  private static Tree_Resources_default_InlineClientBundleGenerator _instance0 = new Tree_Resources_default_InlineClientBundleGenerator();
  private void treeClosedInitializer() {
    treeClosed = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeClosed",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage),
      0, 0, 16, 16, false, false
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
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage0),
      0, 0, 16, 16, false, false
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
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage1),
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
  private static final java.lang.String externalImage = "data:image/gif;base64,R0lGODlhEAAQAIQaAFhorldnrquz1mFxsvz9/vr6/M3Q2ZGbw5mixvb3+Gp5t2Nys77F4GRzs9ze4mt6uGV1s8/R2VZnrl5usFdortPV2/P09+3u8eXm6lZnrf///wAAzP///////////////yH5BAEAAB8ALAAAAAAQABAAAAVE4CeOZGmeaKquo5K974MuTKHdhDCcgOVvvoTkRLkYN8bL0ETBbJ5PTIaIqW6q0lPAYcVOTRNEpEI2HCYoCOzVYLnf7hAAOw==";
  private static final java.lang.String externalImage0 = "data:image/gif;base64,R0lGODlhEAAQAJEAAP///wAAAP///wAAACH5BAEAAAIALAAAAAAQABAAAAIOlI+py+0Po5y02ouzPgUAOw==";
  private static final java.lang.String externalImage1 = "data:image/gif;base64,R0lGODlhEAAQAIQaAFhorldnrquz1mFxsvz9/vr6/M3Q2ZGbw5mixvb3+Gp5t2Nys77F4GRzs9ze4mt6uGV1s8/R2VZnrl5usFdortPV2/P09+3u8eXm6lZnrf///wAAzP///////////////yH5BAEAAB8ALAAAAAAQABAAAAVD4CeOZGmeaKquo5K974MuTKHdhDCcgOVfvoTkRLkYj5ehiYLZOJ2YDBFDvVCjp4CjepWaJohIZWw4TFAQ2KvBarvbIQA7";
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
