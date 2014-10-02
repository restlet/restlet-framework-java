package org.restlet.ext.apispark.internal.model.swagger_2_0;

import org.restlet.ext.apispark.internal.model.swagger_2_0.properties.Property;

import java.util.*;

public interface Model {
  String getDescription();
  void setDescription(String description);

  Map<String, Property> getProperties();
  void setProperties(Map<String, Property> properties);

  String getExample();
  void setExample(String example);
}
