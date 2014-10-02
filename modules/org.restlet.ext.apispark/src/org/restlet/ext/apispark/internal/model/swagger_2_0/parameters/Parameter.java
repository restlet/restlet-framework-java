package org.restlet.ext.apispark.internal.model.swagger_2_0.parameters;

import org.restlet.ext.apispark.internal.model.swagger_2_0.properties.Property;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
  property = "in")
@JsonSubTypes({  
  @Type(value = BodyParameter.class, name = "body"),
  @Type(value = HeaderParameter.class, name = "header"),
  @Type(value = PathParameter.class, name = "path"),
  @Type(value = QueryParameter.class, name = "query"),
  @Type(value = CookieParameter.class, name = "cookie")})
public interface Parameter {
  @JsonIgnore
  String getIn();
  @JsonIgnore
  void setIn(String in);

  String getName();
  void setName(String name);

  String getDescription();
  void setDescription(String description);

  boolean getRequired();
}