package com.google.gwt.user.client.ui;

public class Composite_HTMLTemplatesImpl implements com.google.gwt.user.client.ui.Composite.HTMLTemplates {
  
  public com.google.gwt.safehtml.shared.SafeHtml renderWithId(java.lang.String arg0) {
    StringBuilder sb = new java.lang.StringBuilder();
    sb.append("<span id=\"");
    sb.append(com.google.gwt.safehtml.shared.SafeHtmlUtils.htmlEscape(arg0));
    sb.append("\"></span>");
return new com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(sb.toString());
}
}
