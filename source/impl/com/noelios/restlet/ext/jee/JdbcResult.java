/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.ext.jee;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.restlet.RestletException;

/**
 * JDBC result wrapper.
 * Used by the JDBC client connector as an output of JDBC calls.
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
    * Release the statement connection.
    * To call when result navigation is done.
    */
   public void release() throws RestletException
   {
      try
      {
         statement.getConnection().close();
      }
      catch (SQLException se)
      {
         throw new RestletException("Couldn't release the database connection.", se);
      }
   }

   /**
    * Returns the result set.
    * @return The result set.
    */
   public ResultSet getResultSet() throws RestletException
   {
      try
      {
         return statement.getResultSet();
      }
      catch (SQLException se)
      {
         throw new RestletException("Couldn't get the result of the database request.", se);
      }
   }

   /**
    * Returns the generated keys.
    * @return The generated keys.
    */
   public ResultSet getGeneratedKeys() throws RestletException
   {
      try
      {
         return statement.getGeneratedKeys();
      }
      catch (SQLException se)
      {
         throw new RestletException("Couldn't get the generated keys for the database request.", se);
      }
   }

   /**
    * Returns the update count.
    * @return The update count.
    */
   public int getUpdateCount() throws RestletException
   {
      try
      {
         return statement.getUpdateCount();
      }
      catch (SQLException se)
      {
         throw new RestletException("Couldn't get the update count for the database request.", se);
      }
   }

}




