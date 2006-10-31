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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite of unit tests for the Restlet RI.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class RestletTestSuite extends TestSuite
{
   /** Constructor. */
   public RestletTestSuite()
   {
      addTestSuite(CallTestCase.class);
      addTestSuite(DirectoryTestCase.class);
      addTestSuite(FileReferenceTestCase.class);
      addTestSuite(FilterTestCase.class);
      addTestSuite(FreeMarkerTestCase.class);
      addTestSuite(MediaTypeTestCase.class);
      addTestSuite(RedirectTestCase.class);
      addTestSuite(ReferenceTestCase.class);
      addTestSuite(RestartTestCase.class);
      addTestSuite(StringTemplateTestCase.class);
   }

   /**
    * JUnit constructor.
    * @return The unit test.
    */
   public static Test suite()
   {
      return new RestletTestSuite();
   }

}
