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
