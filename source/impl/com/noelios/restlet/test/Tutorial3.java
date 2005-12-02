package com.noelios.restlet.test;

import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.data.MediaTypes;

import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.ext.jetty.JettyServer;

public class Tutorial3
{
   public static void main(String[] args)
   {
      // Registering the Restlet API implementation
      com.noelios.restlet.Engine.register();

      // Creating a minimal handler returning "Hello World"
      UniformInterface handler = new UniformInterface()
      {
         public void handle(UniformCall call)
         {
            call.setOutput(new StringRepresentation("Hello World!", MediaTypes.TEXT_PLAIN));
         }
      };

      // Create the HTTP server and listen on port 8182
      JettyServer server = new JettyServer("My Web server", 8182, handler);
      server.start();
   }

}
