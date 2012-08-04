package org.restlet.example.book.restlet.ch09.common;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

@SuppressWarnings("deprecation")
public class ContactRepresentation_FieldSerializer implements com.google.gwt.user.client.rpc.impl.TypeHandler {
  private static native java.lang.String getEmail(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) /*-{
    return instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::email;
  }-*/;
  
  private static native void setEmail(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance, java.lang.String value) 
  /*-{
    instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::email = value;
  }-*/;
  
  private static native java.lang.String getFirstName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) /*-{
    return instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::firstName;
  }-*/;
  
  private static native void setFirstName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance, java.lang.String value) 
  /*-{
    instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::firstName = value;
  }-*/;
  
  private static native java.lang.String getLastName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) /*-{
    return instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::lastName;
  }-*/;
  
  private static native void setLastName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance, java.lang.String value) 
  /*-{
    instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::lastName = value;
  }-*/;
  
  private static native java.lang.String getLogin(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) /*-{
    return instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::login;
  }-*/;
  
  private static native void setLogin(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance, java.lang.String value) 
  /*-{
    instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::login = value;
  }-*/;
  
  private static native java.lang.String getNickName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) /*-{
    return instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::nickName;
  }-*/;
  
  private static native void setNickName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance, java.lang.String value) 
  /*-{
    instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::nickName = value;
  }-*/;
  
  private static native java.lang.String getSenderName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) /*-{
    return instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::senderName;
  }-*/;
  
  private static native void setSenderName(org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance, java.lang.String value) 
  /*-{
    instance.@org.restlet.example.book.restlet.ch09.common.ContactRepresentation::senderName = value;
  }-*/;
  
  public static void deserialize(SerializationStreamReader streamReader, org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) throws SerializationException {
    setEmail(instance, streamReader.readString());
    setFirstName(instance, streamReader.readString());
    setLastName(instance, streamReader.readString());
    setLogin(instance, streamReader.readString());
    setNickName(instance, streamReader.readString());
    setSenderName(instance, streamReader.readString());
    
  }
  
  public static org.restlet.example.book.restlet.ch09.common.ContactRepresentation instantiate(SerializationStreamReader streamReader) throws SerializationException {
    return new org.restlet.example.book.restlet.ch09.common.ContactRepresentation();
  }
  
  public static void serialize(SerializationStreamWriter streamWriter, org.restlet.example.book.restlet.ch09.common.ContactRepresentation instance) throws SerializationException {
    streamWriter.writeString(getEmail(instance));
    streamWriter.writeString(getFirstName(instance));
    streamWriter.writeString(getLastName(instance));
    streamWriter.writeString(getLogin(instance));
    streamWriter.writeString(getNickName(instance));
    streamWriter.writeString(getSenderName(instance));
    
  }
  
  public Object create(SerializationStreamReader reader) throws SerializationException {
    return org.restlet.example.book.restlet.ch09.common.ContactRepresentation_FieldSerializer.instantiate(reader);
  }
  
  public void deserial(SerializationStreamReader reader, Object object) throws SerializationException {
    org.restlet.example.book.restlet.ch09.common.ContactRepresentation_FieldSerializer.deserialize(reader, (org.restlet.example.book.restlet.ch09.common.ContactRepresentation)object);
  }
  
  public void serial(SerializationStreamWriter writer, Object object) throws SerializationException {
    org.restlet.example.book.restlet.ch09.common.ContactRepresentation_FieldSerializer.serialize(writer, (org.restlet.example.book.restlet.ch09.common.ContactRepresentation)object);
  }
  
}
