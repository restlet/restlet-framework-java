package com.noelios.restlet.tutorial;

import java.io.IOException;

import com.noelios.restlet.connector.HttpClient;

public class Tutorial2a
{
   public static void main(String[] args)
   {
      // Registering the Restlet API implementation
      com.noelios.restlet.Engine.register();

      // Outputting the content of a Web page
      try
      {
         HttpClient client = new HttpClient("My Web client");
         client.doGet("http://www.restlet.org").write(System.out);
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

}
