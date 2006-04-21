/*
 * Copyright 2005-2006 Jerome LOUVEL
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Parameter;

import junit.framework.TestCase;

import com.noelios.restlet.util.FormUtils;

/**
 * Unit tests for the Cookie related classes.
 */
public class FormTest extends TestCase
{
   /**
    * Tests the cookies parsing.
    */
   public void testParsing() throws IOException
   {
      List<Parameter> params = new ArrayList<Parameter>();
      params.add(new Parameter("name", "John D. Mitchell"));
      params.add(new Parameter("email", "john@bob.net"));
      params.add(new Parameter("email2", "joe@bob.net"));
      
      String query = FormUtils.format(params);
      List<Parameter> newParams = FormUtils.getParameters(query);
      String newQuery = FormUtils.format(newParams);
      assertEquals(query, newQuery);
   }

}
