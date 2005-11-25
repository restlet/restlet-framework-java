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

package org.restlet;

/**
 * Mapper of calls to other restlets. Automatic delegation is provided for attached restlets.
 */
public interface Maplet extends Restlet
{
   /**
    * Attaches a restlet instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param restlet The restlet to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Restlet restlet);

   /**
    * Attaches a restlet class. A new instance will be created for each call.
    * @param pathPattern The path pattern used to map calls.
    * @param restletClass The restlet class to attach (must have a constructor taking a RestletContainer
    * parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends Restlet> restletClass);

   /**
    * Detaches a restlet instance.
    * @param restlet The restlet to detach.
    */
   public void detach(Restlet restlet);

   /**
    * Detaches a restlet class.
    * @param restletClass The restlet class to detach.
    */
   public void detach(Class<? extends Restlet> restletClass);

   /**
    * Delegates a call to attached restlets.
    * @param call The call to delegate.
    * @throws RestletException
    */
   public void delegate(RestletCall call) throws RestletException;

}
