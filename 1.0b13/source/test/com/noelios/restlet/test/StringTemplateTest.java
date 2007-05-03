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

package com.noelios.restlet.test;

import junit.framework.TestCase;

import com.noelios.restlet.util.MapModel;
import com.noelios.restlet.util.StringTemplate;

/**
 * Unit tests for the SemaTemplate class.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class StringTemplateTest extends TestCase
{
   /** Tests the interpolation feature. */
   public void testInterpolation()
   {
   	MapModel dataModel = new MapModel();
      dataModel.put("number", Integer.toString(12345));
      dataModel.put("string", "abcdef");

      StringTemplate st = new StringTemplate("The number is ${number} and the string is ${string}");
      assertEquals(st.process(dataModel), "The number is 12345 and the string is abcdef");

      st = new StringTemplate("The number is $$$ {{{${number}${number} and the string is ${string}$${string}$i{ng}");
      assertEquals(st.process(dataModel), "The number is $$$ {{{1234512345 and the string is abcdef$abcdef$i{ng}");
   }
   
   /** Tests the conditions feature. */
   public void testConditions()
   {
   	MapModel dataModel = new MapModel();
      dataModel.put("number", Integer.toString(12345));
      dataModel.put("string", "abcdef");

      StringTemplate st = new StringTemplate("#[if number]Number exists: ${number}#[else]Number doesn't exist#[end]");
      assertEquals(st.process(dataModel), "Number exists: 12345");
      
      dataModel.remove("number");
      assertEquals(st.process(dataModel), "Number doesn't exist");
   }

}
