package org.restlet.data;

/**
 * Status to return after handling a uniform call.
 */
public interface Status extends ControlData
{
   /**
    * Returns the HTTP code.
    * @return The HTTP code.
    */
   public int getHttpCode();

   /**
    * Returns the URI of the specification describing the status.
    * @return The URI of the specification describing the status.
    */
   public String getUri();
   
   /**
    * Indicates if the status is equal to a given one.
    * @param status  The status to compare to.
    * @return        True if the status is equal to a given one.
    */
   public boolean equals(Status status);
   
}