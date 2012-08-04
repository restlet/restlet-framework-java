package org.restlet.example.book.restlet.ch09.common;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

@SuppressWarnings("deprecation")
public class ContactsRepresentation_FieldSerializer implements com.google.gwt.user.client.rpc.impl.TypeHandler {
  private static native java.util.ArrayList getContacts(org.restlet.example.book.restlet.ch09.common.ContactsRepresentation instance) /*-{
    return instance.@org.restlet.example.book.restlet.ch09.common.ContactsRepresentation::contacts;
  }-*/;
  
  private static native void setContacts(org.restlet.example.book.restlet.ch09.common.ContactsRepresentation instance, java.util.ArrayList value) 
  /*-{
    instance.@org.restlet.example.book.restlet.ch09.common.ContactsRepresentation::contacts = value;
  }-*/;
  
  public static void deserialize(SerializationStreamReader streamReader, org.restlet.example.book.restlet.ch09.common.ContactsRepresentation instance) throws SerializationException {
    setContacts(instance, (java.util.ArrayList) streamReader.readObject());
    
  }
  
  public static org.restlet.example.book.restlet.ch09.common.ContactsRepresentation instantiate(SerializationStreamReader streamReader) throws SerializationException {
    return new org.restlet.example.book.restlet.ch09.common.ContactsRepresentation();
  }
  
  public static void serialize(SerializationStreamWriter streamWriter, org.restlet.example.book.restlet.ch09.common.ContactsRepresentation instance) throws SerializationException {
    streamWriter.writeObject(getContacts(instance));
    
  }
  
  public Object create(SerializationStreamReader reader) throws SerializationException {
    return org.restlet.example.book.restlet.ch09.common.ContactsRepresentation_FieldSerializer.instantiate(reader);
  }
  
  public void deserial(SerializationStreamReader reader, Object object) throws SerializationException {
    org.restlet.example.book.restlet.ch09.common.ContactsRepresentation_FieldSerializer.deserialize(reader, (org.restlet.example.book.restlet.ch09.common.ContactsRepresentation)object);
  }
  
  public void serial(SerializationStreamWriter writer, Object object) throws SerializationException {
    org.restlet.example.book.restlet.ch09.common.ContactsRepresentation_FieldSerializer.serialize(writer, (org.restlet.example.book.restlet.ch09.common.ContactsRepresentation)object);
  }
  
}
