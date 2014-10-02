package org.restlet.ext.apispark.internal.model.swagger_2_0.parameters;

import org.restlet.ext.apispark.internal.model.swagger_2_0.properties.*;

public interface SerializableParameter extends Parameter {
  String getType();
  void setType(String type);

  void setItems(Property items);
  Property getItems();

  String getFormat();
  void setFormat(String format);

  String getCollectionFormat();
  void setCollectionFormat(String collectionFormat);
}