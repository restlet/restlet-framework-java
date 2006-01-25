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

package com.noelios.restlet.ext.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC result wrapper. Used by the JDBC client connector as an output of JDBC calls.
 */
public class JdbcResult
{
   /** The JDBC statement. */
   private Statement statement;

   /**
    * Constructor.
    * @param statement The JDBC statement.
    */
   public JdbcResult(Statement statement)
   {
      this.statement = statement;
   }

   /**
    * Release the statement connection. To call when result navigation is done.
    * @throws SQLException 
    */
   public void release() throws SQLException 
   {
      statement.getConnection().close();
   }

   /**
    * Returns the result set.
    * @return The result set.
    * @throws SQLException 
    */
   public ResultSet getResultSet() throws SQLException 
   {
      return statement.getResultSet();
   }

   /**
    * Returns the generated keys.
    * @return The generated keys.
    * @throws SQLException 
    */
   public ResultSet getGeneratedKeys() throws SQLException
   {
      return statement.getGeneratedKeys();
   }

   /**
    * Returns the update count.
    * @return The update count.
    * @throws SQLException 
    */
   public int getUpdateCount() throws SQLException
   {
      return statement.getUpdateCount();
   }

}
