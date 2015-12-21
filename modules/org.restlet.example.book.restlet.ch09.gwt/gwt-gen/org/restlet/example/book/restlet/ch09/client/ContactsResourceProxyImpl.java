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

package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import java.io.IOException;
import org.restlet.client.Client;
import org.restlet.client.data.Protocol;
import org.restlet.client.data.Preference;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.Method;
import org.restlet.client.representation.Representation;
import org.restlet.client.representation.ObjectRepresentation;
import org.restlet.client.resource.ResourceException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.restlet.client.resource.Result;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import org.restlet.client.data.MediaType;

public class ContactsResourceProxyImpl extends org.restlet.client.engine.resource.GwtClientProxy implements org.restlet.example.book.restlet.ch09.client.ContactsResourceProxy {
  
  private static final String SERIALIZATION_POLICY ="null";
  private static final org.restlet.example.book.restlet.ch09.client.ContactsResourceProxy_TypeSerializer SERIALIZER = new org.restlet.example.book.restlet.ch09.client.ContactsResourceProxy_TypeSerializer();
  
  public ContactsResourceProxyImpl() {
    super(GWT.getModuleBaseURL(),
      SERIALIZATION_POLICY, 
      SERIALIZER);
  }
  
  public void add(org.restlet.example.book.restlet.ch09.common.ContactRepresentation param1, final org.restlet.client.resource.Result<java.lang.Void> callback){
    Representation requestEntity = new ObjectRepresentation<org.restlet.example.book.restlet.ch09.common.ContactRepresentation>((SerializationStreamFactory) ContactsResourceProxyImpl.this, param1);
    getClientResource().getRequest().setEntity(requestEntity);
    getClientResource().getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT_GWT));
    getClientResource().setMethod(Method.POST);
    getClientResource().setOnResponse(new Uniform() {
      public void handle(Request request, Response response) {
        if (getClientResource().getStatus().isError()) {
          callback.onFailure(new ResourceException(getClientResource().getStatus()));
        } else {
          callback.onSuccess(null);
        }
      }
    });
    getClientResource().handle();
  }
  
  public void retrieve(final org.restlet.client.resource.Result<org.restlet.example.book.restlet.ch09.common.ContactsRepresentation> callback){
    getClientResource().getRequest().setEntity(null);
    getClientResource().getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT_GWT));
    getClientResource().setMethod(Method.GET);
    getClientResource().setOnResponse(new Uniform() {
      public void handle(Request request, Response response) {
        if (getClientResource().getStatus().isError()) {
          callback.onFailure(new ResourceException(getClientResource().getStatus()));
        } else {
          org.restlet.example.book.restlet.ch09.common.ContactsRepresentation result = null;
          boolean serializationError = false;
          
          try {
            if(response.isEntityAvailable()){
              if (MediaType.APPLICATION_JAVA_OBJECT_GWT.equals(response.getEntity().getMediaType())) {
                result = new ObjectRepresentation<org.restlet.example.book.restlet.ch09.common.ContactsRepresentation>(
                  response.getEntity().getText(),
                  (SerializationStreamFactory) ContactsResourceProxyImpl.this, false)
                  .getObject();
              } else {
                throw new IOException("Can't parse the enclosed entity because of its media type. Expected <" + MediaType.APPLICATION_JAVA_OBJECT_GWT + "> but was <" + response.getEntity().getMediaType() + ">. Make sure you have added the org.restlet.client.ext.gwt.jar file to your server.");
              }
            }
          } catch (Throwable e) {
            // Serialization error, considered as a communication error.
            serializationError = true;
            callback.onFailure(new ResourceException(e));
          }
          
          if (!serializationError) {
            callback.onSuccess(result);
          }
        }
      }
    });
    getClientResource().handle();
  }
  
}
