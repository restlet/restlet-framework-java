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

package com.noelios.restlet.util;

import java.io.IOException;

/**
 * Log file handler that uses the {@link LogFormatter} by default. Also useful in
 * configuration files to differentiate from the java.util.logging.FileHandler.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class LogFileHandler extends java.util.logging.FileHandler
{
   /**
    * Constructor.
    * @throws IOException
    * @throws SecurityException
    */
   public LogFileHandler() throws IOException, SecurityException
   {
      super();
      init();
   }

   /**
    * Constructor.
    * @param pattern The name of the output file.
    * @throws IOException
    * @throws SecurityException
    */
   public LogFileHandler(String pattern) throws IOException, SecurityException
   {
      super(pattern);
      init();
   }

   /**
    * Constructor.
    * @param pattern The name of the output file.
    * @param append Specifies append mode.
    * @throws IOException
    * @throws SecurityException
    */
   public LogFileHandler(String pattern, boolean append) throws IOException, SecurityException
   {
      super(pattern, append);
      init();
   }

   /**
    * Constructor.
    * @param pattern The name of the output file.
    * @param limit The maximum number of bytes to write to any one file.
    * @param count The number of files to use.
    * @throws IOException
    * @throws SecurityException
    */
   public LogFileHandler(String pattern, int limit, int count) throws IOException, SecurityException
   {
      super(pattern, limit, count);
      init();
   }

   /**
    * Constructor.
    * @param pattern The name of the output file.
    * @param limit The maximum number of bytes to write to any one file.
    * @param count The number of files to use.
    * @param append Specifies append mode.
    * @throws IOException
    * @throws SecurityException
    */
   public LogFileHandler(String pattern, int limit, int count, boolean append) throws IOException,
         SecurityException
   {
      super(pattern, limit, count, append);
      init();
   }

   /**
    * Initialization code common to all constructors.
    */
   protected void init()
   {
      setFormatter(new LogFormatter());
   }

}
