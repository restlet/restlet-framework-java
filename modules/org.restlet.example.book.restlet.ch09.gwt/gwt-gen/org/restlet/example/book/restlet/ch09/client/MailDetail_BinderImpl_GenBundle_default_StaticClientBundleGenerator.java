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

public class MailDetail_BinderImpl_GenBundle_default_StaticClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenBundle {
  private static MailDetail_BinderImpl_GenBundle_default_StaticClientBundleGenerator _instance0 = new MailDetail_BinderImpl_GenBundle_default_StaticClientBundleGenerator();
  private void styleInitializer() {
    style = new org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenCss_style() {
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
        return com.google.gwt.i18n.client.LocaleInfo.getCurrentLocale().isRTL() ? ((".GL0P3EKCHI{border:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";background-color:" + ("white")  + ";}.GL0P3EKCII{background:" + ("#eee")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";padding:" + ("0.5em")  + ";}.GL0P3EKCJI{margin-bottom:" + ("0.5em")  + ";}.GL0P3EKCGI{line-height:" + ("150%")  + ";padding:" + ("20px"+ " " +"10px"+ " " +"20px"+ " " +"40px")  + ";font-family:" + ("\"Times New Roman\""+ ","+ " " +"Times"+ ","+ " " +"serif")  + ";}")) : ((".GL0P3EKCHI{border:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";background-color:" + ("white")  + ";}.GL0P3EKCII{background:" + ("#eee")  + ";border-bottom:" + ("1px"+ " " +"solid"+ " " +"#666")  + ";padding:" + ("0.5em")  + ";}.GL0P3EKCJI{margin-bottom:" + ("0.5em")  + ";}.GL0P3EKCGI{line-height:" + ("150%")  + ";padding:" + ("20px"+ " " +"40px"+ " " +"20px"+ " " +"10px")  + ";font-family:" + ("\"Times New Roman\""+ ","+ " " +"Times"+ ","+ " " +"serif")  + ";}"));
      }
      public java.lang.String body(){
        return "GL0P3EKCGI";
      }
      public java.lang.String detail(){
        return "GL0P3EKCHI";
      }
      public java.lang.String header(){
        return "GL0P3EKCII";
      }
      public java.lang.String headerItem(){
        return "GL0P3EKCJI";
      }
    }
    ;
  }
  private static class styleInitializer {
    static {
      _instance0.styleInitializer();
    }
    static org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenCss_style get() {
      return style;
    }
  }
  public org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenCss_style style() {
    return styleInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenCss_style style;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      style(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("style", style());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'style': return this.@org.restlet.example.book.restlet.ch09.client.MailDetail_BinderImpl_GenBundle::style()();
    }
    return null;
  }-*/;
}
