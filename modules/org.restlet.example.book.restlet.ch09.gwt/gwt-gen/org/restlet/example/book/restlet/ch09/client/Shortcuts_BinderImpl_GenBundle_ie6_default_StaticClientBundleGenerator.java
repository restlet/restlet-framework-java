package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle {
  private static Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator _instance0 = new Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator();
  private void contactsgroupInitializer() {
    contactsgroup = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "contactsgroup",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      62, 0, 31, 22, false, false
    );
  }
  private static class contactsgroupInitializer {
    static {
      _instance0.contactsgroupInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return contactsgroup;
    }
  }
  public com.google.gwt.resources.client.ImageResource contactsgroup() {
    return contactsgroupInitializer.get();
  }
  private void gradientInitializer() {
    gradient = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "gradient",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage),
      0, 0, 1, 64, false, true
    );
  }
  private static class gradientInitializer {
    static {
      _instance0.gradientInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return gradient;
    }
  }
  public com.google.gwt.resources.client.ImageResource gradient() {
    return gradientInitializer.get();
  }
  private void mailboxesgroupInitializer() {
    mailboxesgroup = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "mailboxesgroup",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      31, 0, 31, 22, false, false
    );
  }
  private static class mailboxesgroupInitializer {
    static {
      _instance0.mailboxesgroupInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return mailboxesgroup;
    }
  }
  public com.google.gwt.resources.client.ImageResource mailboxesgroup() {
    return mailboxesgroupInitializer.get();
  }
  private void tasksgroupInitializer() {
    tasksgroup = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "tasksgroup",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(bundledImage_None),
      0, 0, 31, 22, false, false
    );
  }
  private static class tasksgroupInitializer {
    static {
      _instance0.tasksgroupInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return tasksgroup;
    }
  }
  public com.google.gwt.resources.client.ImageResource tasksgroup() {
    return tasksgroupInitializer.get();
  }
  private void contactsgroupIe6DataInitializer() {
    contactsgroupIe6Data = // file:/C:/Projets/framework/java-2.1/modules/org.restlet.example.book.restlet.ch09.gwt/src/org/restlet/example/book/restlet/ch09/client/contactsgroup_ie6.gif
    new com.google.gwt.resources.client.impl.DataResourcePrototype(
      "contactsgroupIe6Data",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(GWT.getModuleBaseURL() + "61B4B0B4F491ADB29DB45F9620850594.cache.gif")
    );
  }
  private static class contactsgroupIe6DataInitializer {
    static {
      _instance0.contactsgroupIe6DataInitializer();
    }
    static com.google.gwt.resources.client.DataResource get() {
      return contactsgroupIe6Data;
    }
  }
  public com.google.gwt.resources.client.DataResource contactsgroupIe6Data() {
    return contactsgroupIe6DataInitializer.get();
  }
  private void mailboxesgroupIe6DataInitializer() {
    mailboxesgroupIe6Data = // file:/C:/Projets/framework/java-2.1/modules/org.restlet.example.book.restlet.ch09.gwt/src/org/restlet/example/book/restlet/ch09/client/mailboxesgroup_ie6.gif
    new com.google.gwt.resources.client.impl.DataResourcePrototype(
      "mailboxesgroupIe6Data",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(GWT.getModuleBaseURL() + "91D5CD9C9F27CB285666947488F07704.cache.gif")
    );
  }
  private static class mailboxesgroupIe6DataInitializer {
    static {
      _instance0.mailboxesgroupIe6DataInitializer();
    }
    static com.google.gwt.resources.client.DataResource get() {
      return mailboxesgroupIe6Data;
    }
  }
  public com.google.gwt.resources.client.DataResource mailboxesgroupIe6Data() {
    return mailboxesgroupIe6DataInitializer.get();
  }
  private void tasksgroupIe6DataInitializer() {
    tasksgroupIe6Data = // file:/C:/Projets/framework/java-2.1/modules/org.restlet.example.book.restlet.ch09.gwt/src/org/restlet/example/book/restlet/ch09/client/tasksgroup_ie6.gif
    new com.google.gwt.resources.client.impl.DataResourcePrototype(
      "tasksgroupIe6Data",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(GWT.getModuleBaseURL() + "B6B6AE38F859FEC58CE8973F5C4D740D.cache.gif")
    );
  }
  private static class tasksgroupIe6DataInitializer {
    static {
      _instance0.tasksgroupIe6DataInitializer();
    }
    static com.google.gwt.resources.client.DataResource get() {
      return tasksgroupIe6Data;
    }
  }
  public com.google.gwt.resources.client.DataResource tasksgroupIe6Data() {
    return tasksgroupIe6DataInitializer.get();
  }
  private void styleInitializer() {
    style = new org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenCss_style() {
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
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? ((".GL0P3EKCAJ{border-right:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-left:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";}.GL0P3EKCBJ{height:" + ((Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getHeight() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getSafeUri().asString() + "\") -" + (Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getLeft() + "px -" + (Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getTop() + "px  repeat-x")  + ";background-color:" + ("#b4b6bc")  + ";cursor:" + ("pointer")  + ";text-shadow:" + ("rgba(255,255,255,1)"+ " " +"0"+ " " +"1px"+ " " +"1px")  + ";font-size:" + ("1.2em")  + ";font-weight:") + (("bold")  + ";color:" + ("#000")  + ";padding:" + ("0.7em"+ " " +"0.6em"+ " " +"0"+ " " +"0.5em")  + ";border-top:" + ("1px"+ " " +"solid"+ " " +"#888")  + ";}.GL0P3EKCPI{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.mailboxesgroupIe6Data())).getUrl() + "')")  + ";width:" + ("31px")  + ";height:" + ("22px")  + ";float:" + ("right")  + ";}.GL0P3EKCCJ{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.tasksgroupIe6Data())).getUrl() + "')")  + ";width:" + ("31px")  + ";height:" + ("22px") ) + (";float:" + ("right")  + ";}.GL0P3EKCOI{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.contactsgroupIe6Data())).getUrl() + "')")  + ";width:" + ("31px")  + ";height:" + ("22px")  + ";float:" + ("right")  + ";}")) : ((".GL0P3EKCAJ{border-left:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-right:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#999")  + ";}.GL0P3EKCBJ{height:" + ((Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getHeight() + "px")  + ";overflow:" + ("hidden")  + ";background:" + ("url(\"" + (Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getSafeUri().asString() + "\") -" + (Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getLeft() + "px -" + (Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.gradient()).getTop() + "px  repeat-x")  + ";background-color:" + ("#b4b6bc")  + ";cursor:" + ("pointer")  + ";text-shadow:" + ("rgba(255,255,255,1)"+ " " +"0"+ " " +"1px"+ " " +"1px")  + ";font-size:" + ("1.2em")  + ";font-weight:") + (("bold")  + ";color:" + ("#000")  + ";padding:" + ("0.7em"+ " " +"0.5em"+ " " +"0"+ " " +"0.6em")  + ";border-top:" + ("1px"+ " " +"solid"+ " " +"#888")  + ";}.GL0P3EKCPI{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.mailboxesgroupIe6Data())).getUrl() + "')")  + ";width:" + ("31px")  + ";height:" + ("22px")  + ";float:" + ("left")  + ";}.GL0P3EKCCJ{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.tasksgroupIe6Data())).getUrl() + "')")  + ";width:" + ("31px")  + ";height:" + ("22px") ) + (";float:" + ("left")  + ";}.GL0P3EKCOI{background-image:" + ("url('" + ((com.google.gwt.resources.client.DataResource)(Shortcuts_BinderImpl_GenBundle_ie6_default_StaticClientBundleGenerator.this.contactsgroupIe6Data())).getUrl() + "')")  + ";width:" + ("31px")  + ";height:" + ("22px")  + ";float:" + ("left")  + ";}"));
      }
      public java.lang.String contactsIcon(){
        return "GL0P3EKCOI";
      }
      public java.lang.String mailboxesIcon(){
        return "GL0P3EKCPI";
      }
      public java.lang.String shortcuts(){
        return "GL0P3EKCAJ";
      }
      public java.lang.String stackHeader(){
        return "GL0P3EKCBJ";
      }
      public java.lang.String tasksIcon(){
        return "GL0P3EKCCJ";
      }
    }
    ;
  }
  private static class styleInitializer {
    static {
      _instance0.styleInitializer();
    }
    static org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenCss_style get() {
      return style;
    }
  }
  public org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenCss_style style() {
    return styleInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String bundledImage_None = GWT.getModuleBaseURL() + "20BE821ADF70427E40CF4EB11BAEF83E.cache.png";
  private static final java.lang.String externalImage = GWT.getModuleBaseURL() + "DE97258B391723C7A5CE876B33D33A0D.cache.png";
  private static com.google.gwt.resources.client.ImageResource contactsgroup;
  private static com.google.gwt.resources.client.ImageResource gradient;
  private static com.google.gwt.resources.client.ImageResource mailboxesgroup;
  private static com.google.gwt.resources.client.ImageResource tasksgroup;
  private static com.google.gwt.resources.client.DataResource contactsgroupIe6Data;
  private static com.google.gwt.resources.client.DataResource mailboxesgroupIe6Data;
  private static com.google.gwt.resources.client.DataResource tasksgroupIe6Data;
  private static org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenCss_style style;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      contactsgroup(), 
      gradient(), 
      mailboxesgroup(), 
      tasksgroup(), 
      contactsgroupIe6Data(), 
      mailboxesgroupIe6Data(), 
      tasksgroupIe6Data(), 
      style(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("contactsgroup", contactsgroup());
        resourceMap.put("gradient", gradient());
        resourceMap.put("mailboxesgroup", mailboxesgroup());
        resourceMap.put("tasksgroup", tasksgroup());
        resourceMap.put("contactsgroupIe6Data", contactsgroupIe6Data());
        resourceMap.put("mailboxesgroupIe6Data", mailboxesgroupIe6Data());
        resourceMap.put("tasksgroupIe6Data", tasksgroupIe6Data());
        resourceMap.put("style", style());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'contactsgroup': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::contactsgroup()();
      case 'gradient': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::gradient()();
      case 'mailboxesgroup': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::mailboxesgroup()();
      case 'tasksgroup': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::tasksgroup()();
      case 'contactsgroupIe6Data': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::contactsgroupIe6Data()();
      case 'mailboxesgroupIe6Data': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::mailboxesgroupIe6Data()();
      case 'tasksgroupIe6Data': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::tasksgroupIe6Data()();
      case 'style': return this.@org.restlet.example.book.restlet.ch09.client.Shortcuts_BinderImpl_GenBundle::style()();
    }
    return null;
  }-*/;
}
