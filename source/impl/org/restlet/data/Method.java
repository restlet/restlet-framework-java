package org.restlet.data;

/**
 * Method to execute when handling a uniform call.
 * @see org.restlet.data.MethodEnum
 */
public interface Method extends ControlData
{
   /**
    * Returns the technical name of the method.
    * @return The technical name of the method.
    */
   public String getName();

   /**
    * Returns the URI of the specification describing the method.
    * @return The URI of the specification describing the method.
    */
   public String getUri();

   /**
    * Indicates if the method is equal to a given one.
    * @param method  The method to compare to.
    * @return        True if the method is equal to a given one.
    */
   public boolean equals(Method method);

}