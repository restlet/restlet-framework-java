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
