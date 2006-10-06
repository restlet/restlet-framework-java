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

package com.noelios.restlet.example.tutorial;

import org.restlet.component.Container;
import org.restlet.data.Protocol;

import com.noelios.restlet.DirectoryFinder;

/**
 * Serving static files.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial06 implements Constants
{
   public static void main(String[] args) throws Exception
   {
      // Create a new Restlet container and add a HTTP server connector to it
   	Container myContainer = new Container();
      myContainer.getServers().add(Protocol.HTTP, 8182);

      // Create a DirectoryFinder able to return a deep hierarchy of Web files
      // (HTML pages, CSS stylesheets or GIF images) from a local directory.
      DirectoryFinder directory = new DirectoryFinder(myContainer.getContext(), ROOT_URI, "index.html");
      myContainer.getLocalHost().attach("/", directory);

      // Now, let's start the container!
      myContainer.start();
   }

}
