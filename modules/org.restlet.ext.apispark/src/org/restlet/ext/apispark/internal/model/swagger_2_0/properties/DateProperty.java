package org.restlet.ext.apispark.internal.model.swagger_2_0.properties;

public class DateProperty extends AbstractProperty implements Property {
  public DateProperty() {
    super.type = "string";
    super.format = "date";
  }

  public DateProperty example(String example) {
    this.setExample(example);
    return this;
  }

  public static boolean isType(String type, String format) {
    if("string".equals(type) && "date".equals(format))
      return true;
    else return false;
  }
}