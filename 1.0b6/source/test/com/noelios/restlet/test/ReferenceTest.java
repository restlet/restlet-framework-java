/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import junit.framework.TestCase;

import org.restlet.Manager;
import org.restlet.data.Reference;

/**
 * Unit tests for the Cookie related classes.
 */
public class ReferenceTest extends TestCase
{
   /**
    * Tests the cookies parsing.
    */
   public void testParsing() throws IOException
   {
      String uri01 = "http://www.domain.com/123/456/789";
      String uri02 = "http://www.domain.com/abc";
      String uri03 = "http://www.domain.com/abc/";
      String uri04 = "http://www.domain.com/abc/def";
      String uri05 = "http://www.domain.com/abc/def/123/456";
      String uri06 = "http://www.domain.com/abc/def/ghi/jkl";
      String uri07 = "http://www.domain.com/abc/def/ghi/jkl/";
      String uri08 = "http://www.domain.com/abc/def/ghi/jkl/mno/pqr";
      String uri09 = "http://www.domain.com/abc/defghi/jkl";
      String uri10 = "http://www.domain.com/abc/defghi/jkl/";
      String uri11 = "http://www.domaine.com/abc/def/ghi/jkl";
      
      testRef(uri06, uri05, "../../123/456");
      testRef(uri06, uri08, "mno/pqr");
      testRef(uri06, uri02, "../../..");
      testRef(uri06, uri06, ".");
      testRef(uri03, uri02, ".");
      testRef(uri02, uri03, ".");
      testRef(uri06, uri01, "../../../../123/456/789");
      testRef(uri06, uri11, uri11);
      testRef(uri06, uri09, "../../../defghi/jkl");
      testRef(uri07, uri10, "../../../defghi/jkl/");
      testRef(uri09, uri06, "../../def/ghi/jkl");
      testRef(uri10, uri07, "../../def/ghi/jkl/");
      testRef(uri09, uri04, "../../def");
   }
   
   private void testRef(String baseUri, String absoluteUri, String expectedRelativeUri)
   {
      Reference baseRef = Manager.createReference(baseUri);
      Reference absoluteRef = Manager.createReference(absoluteUri);
      Reference relativeRef = absoluteRef.getRelativeRef(baseRef);
      assertEquals(expectedRelativeUri, relativeRef.toString());
   }

}
