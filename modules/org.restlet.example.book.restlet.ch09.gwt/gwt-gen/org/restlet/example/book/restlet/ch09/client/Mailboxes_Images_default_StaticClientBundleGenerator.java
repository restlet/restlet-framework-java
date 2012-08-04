package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class Mailboxes_Images_default_StaticClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.Mailboxes.Images {
  private static Mailboxes_Images_default_StaticClientBundleGenerator _instance0 = new Mailboxes_Images_default_StaticClientBundleGenerator();
  private void draftsInitializer() {
    drafts = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "drafts",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      80, 0, 16, 16, false, false
    );
  }
  private static class draftsInitializer {
    static {
      _instance0.draftsInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return drafts;
    }
  }
  public com.google.gwt.resources.client.ImageResource drafts() {
    return draftsInitializer.get();
  }
  private void homeInitializer() {
    home = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "home",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      64, 0, 16, 16, false, false
    );
  }
  private static class homeInitializer {
    static {
      _instance0.homeInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return home;
    }
  }
  public com.google.gwt.resources.client.ImageResource home() {
    return homeInitializer.get();
  }
  private void inboxInitializer() {
    inbox = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "inbox",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      48, 0, 16, 16, false, false
    );
  }
  private static class inboxInitializer {
    static {
      _instance0.inboxInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return inbox;
    }
  }
  public com.google.gwt.resources.client.ImageResource inbox() {
    return inboxInitializer.get();
  }
  private void sentInitializer() {
    sent = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "sent",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      32, 0, 16, 16, false, false
    );
  }
  private static class sentInitializer {
    static {
      _instance0.sentInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return sent;
    }
  }
  public com.google.gwt.resources.client.ImageResource sent() {
    return sentInitializer.get();
  }
  private void templatesInitializer() {
    templates = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "templates",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      16, 0, 16, 16, false, false
    );
  }
  private static class templatesInitializer {
    static {
      _instance0.templatesInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return templates;
    }
  }
  public com.google.gwt.resources.client.ImageResource templates() {
    return templatesInitializer.get();
  }
  private void trashInitializer() {
    trash = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "trash",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      0, 0, 16, 16, false, false
    );
  }
  private static class trashInitializer {
    static {
      _instance0.trashInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return trash;
    }
  }
  public com.google.gwt.resources.client.ImageResource trash() {
    return trashInitializer.get();
  }
  private void treeClosedInitializer() {
    treeClosed = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeClosed",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      112, 0, 16, 16, false, false
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
      128, 0, 1, 1, false, false
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
      96, 0, 16, 16, false, false
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
  private static final java.lang.String bundledImage_None = GWT.getModuleBaseURL() + "7EF5C36A05DC63199EDC5348859622DB.cache.png";
  private static com.google.gwt.resources.client.ImageResource drafts;
  private static com.google.gwt.resources.client.ImageResource home;
  private static com.google.gwt.resources.client.ImageResource inbox;
  private static com.google.gwt.resources.client.ImageResource sent;
  private static com.google.gwt.resources.client.ImageResource templates;
  private static com.google.gwt.resources.client.ImageResource trash;
  private static com.google.gwt.resources.client.ImageResource treeClosed;
  private static com.google.gwt.resources.client.ImageResource treeLeaf;
  private static com.google.gwt.resources.client.ImageResource treeOpen;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      drafts(), 
      home(), 
      inbox(), 
      sent(), 
      templates(), 
      trash(), 
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
        resourceMap.put("drafts", drafts());
        resourceMap.put("home", home());
        resourceMap.put("inbox", inbox());
        resourceMap.put("sent", sent());
        resourceMap.put("templates", templates());
        resourceMap.put("trash", trash());
        resourceMap.put("treeClosed", treeClosed());
        resourceMap.put("treeLeaf", treeLeaf());
        resourceMap.put("treeOpen", treeOpen());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'drafts': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::drafts()();
      case 'home': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::home()();
      case 'inbox': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::inbox()();
      case 'sent': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::sent()();
      case 'templates': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::templates()();
      case 'trash': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::trash()();
      case 'treeClosed': return this.@com.google.gwt.user.client.ui.Tree.Resources::treeClosed()();
      case 'treeLeaf': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::treeLeaf()();
      case 'treeOpen': return this.@com.google.gwt.user.client.ui.Tree.Resources::treeOpen()();
    }
    return null;
  }-*/;
}
