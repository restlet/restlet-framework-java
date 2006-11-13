/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.CallModel;
import org.restlet.util.MapModel;
import org.restlet.util.Model;
import org.restlet.util.StringTemplate;

/**
 * Unit tests for the StringTemplate class.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class StringTemplateTestCase extends TestCase
{
   /** Tests the interpolation feature. */
   public void testInterpolation()
   {
   	Model dataModel = new MapModel();
      dataModel.put("number", Integer.toString(12345));
      dataModel.put("string", "abcdef");

      StringTemplate st = new StringTemplate("The number is ${number} and the string is ${string}");
      assertEquals("The number is 12345 and the string is abcdef", st.format(dataModel));

      st = new StringTemplate("The number is ${foo?exists} and the string is ${string?exists}");
      assertEquals("The number is  and the string is abcdef", st.format(dataModel));

      st = new StringTemplate("The number is $$$ {{{${number}${number} and the string is ${string}$${string}$i{ng}");
      assertEquals("The number is $$$ {{{1234512345 and the string is abcdef$abcdef$i{ng}", st.format(dataModel));
   }
   
   /** Tests the conditions feature. */
   public void testConditions()
   {
   	Model dataModel = new MapModel();
      dataModel.put("number", Integer.toString(12345));
      dataModel.put("string", "abcdef");

      StringTemplate st = new StringTemplate("#[if number]Number exists: ${number}#[else]Number doesn't exist#[end]");
      assertEquals("Number exists: 12345", st.format(dataModel));
      
      dataModel.remove("number");
      assertEquals("Number doesn't exist", st.format(dataModel));
   }

   /** Test URI patterns based on the CallModel and StringTemplate. */
   public void testUriPattern()
   {
   	// Create a test call
      Request request = new Request();
      Response response = new Response(request);
      request.setResourceRef("http://www.domain.com:8080/path1/path2/path3?param1&param2=123&query=abc");

      // Create the template engine
   	String pattern = "http://www.target.org";
   	pattern += "${path}#[if query('query')]?${query('query')}#[end]";
      StringTemplate te = new StringTemplate(pattern);

      // Create the template data model
      String targetUri = te.format(new CallModel(request, response, null));
      assertEquals("http://www.target.org/path1/path2/path3?abc", targetUri);

      // Remove the query parameter
      request.setResourceRef("http://www.domain.com:8080/path1/path2/path3?param1&param2=123");
      targetUri = te.format(new CallModel(request, response, null));
      assertEquals("http://www.target.org/path1/path2/path3", targetUri);
      
      // Extract the last segment
      te = new StringTemplate("${segment(last)}");
      targetUri = te.format(new CallModel(request, response, null));
      assertEquals("path3", targetUri);
   }
   
}
