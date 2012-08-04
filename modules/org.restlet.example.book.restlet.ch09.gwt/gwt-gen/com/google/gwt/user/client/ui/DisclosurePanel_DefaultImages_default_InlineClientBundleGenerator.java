package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class DisclosurePanel_DefaultImages_default_InlineClientBundleGenerator implements com.google.gwt.user.client.ui.DisclosurePanel.DefaultImages {
  private static DisclosurePanel_DefaultImages_default_InlineClientBundleGenerator _instance0 = new DisclosurePanel_DefaultImages_default_InlineClientBundleGenerator();
  private void disclosurePanelClosedInitializer() {
    disclosurePanelClosed = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "disclosurePanelClosed",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ?externalImage_rtl : externalImage),
      0, 0, 16, 16, false, false
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
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage0),
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
  private static final java.lang.String externalImage = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAfklEQVR42mNgoDZITk4WosiAtLS0M6mpqb1Amp9cAy4B8X8gfpWenp5MiQEwfB6IbSgxAIaXArEcJQaA8Ddg+NQVFhZykmsADG8MDQ1lJseA5wQDFocBP0FRm5WVxUNOGGwEJi4VcmLhKtC5HuSkg8NA5+bjDCRCAG8UDUoAAIw8kVdwMG+3AAAAAElFTkSuQmCC";
  private static final java.lang.String externalImage_rtl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAkElEQVR42mNgoCdITk4WIktjWloaf2pqai+QPkOy5vT09GSgxldA/B+IL5Fiqw0Qn4dq/E+0AUBFckC8FE0jYQMKCws5gf6sAyr6hkMzbgNCQ0OZgZIb8WgkzgvQAHtOtgEgkJWVxQONsp9kGYCUaFSweOsSOenAA6jxKtkGwAIZ6K18oAGHKcoLoKhmGHQAAOyGkVflBH+sAAAAAElFTkSuQmCC";
  private static final java.lang.String externalImage0 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAjUlEQVR42mNgGD6gsLCQMy0t7TAQXyICn0lOThbCMCQ1NTUfKPmfEAaq68XqitDQUGaggqsEDHgFxPw4vZKenu6BzwCgfDLB8AAq3IjDgPNEBSgwgFSAin9iMcCG6FgBBRSa5qUkRWtWVhYPUNNzqOZvQCxHctoABRg02urITmCgAAUlMrINAKWNwZ2HAAhGkVd3k7/tAAAAAElFTkSuQmCC";
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
