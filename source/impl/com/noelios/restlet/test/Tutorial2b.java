package com.noelios.restlet.test;

import java.io.IOException;

import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.data.Methods;
import org.restlet.data.Reference;
import org.restlet.data.Representation;

import com.noelios.restlet.connector.HttpClient;

public class Tutorial2b
{
   public static void main(String[] args)
   {
      // Registering the Restlet API implementation
      com.noelios.restlet.Engine.register();

      // Outputting the content of a Web page
      try
      {
         // Prepare the REST call
         UniformCall call = Manager.createCall();
         Reference uri = Manager.createReference("http://www.restlet.org");
         call.setResourceUri(uri);
         call.setMethod(Methods.GET);

         // Ask to the HTTP client connector to handle the call
         HttpClient client = new HttpClient("My Web client");
         client.handle(call);
         
         // Output the result representation on the JVM console
         Representation output = call.getOutput();
         output.write(System.out);
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

}
