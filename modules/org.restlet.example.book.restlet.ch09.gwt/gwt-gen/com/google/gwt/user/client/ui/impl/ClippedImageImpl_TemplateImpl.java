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

package com.google.gwt.user.client.ui.impl;

public class ClippedImageImpl_TemplateImpl implements com.google.gwt.user.client.ui.impl.ClippedImageImpl.Template {
  
  public com.google.gwt.safehtml.shared.SafeHtml image(com.google.gwt.safehtml.shared.SafeUri arg0,com.google.gwt.safecss.shared.SafeStyles arg1) {
    StringBuilder sb = new java.lang.StringBuilder();
    sb.append("<img onload='this.__gwtLastUnhandledEvent=\"load\";' src='");
    sb.append(com.google.gwt.safehtml.shared.SafeHtmlUtils.htmlEscape(arg0.asString()));
    sb.append("' style='");
    sb.append(com.google.gwt.safehtml.shared.SafeHtmlUtils.htmlEscape(arg1.asString()));
    sb.append("' border='0'>");
return new com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(sb.toString());
}
}
