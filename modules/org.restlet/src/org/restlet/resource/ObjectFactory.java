package org.restlet.resource;


/**
 * Factory for instantiating objects.
 * 
 * @author Raniz
 *
 */
public interface ObjectFactory
{

	/**
	 * Gets an instance of a class.
	 * 
	 * @param targetClass
     *            The class that should be instantiated.
	 * 
	 * @return An instance of targetClass
	 */
	<T> T getInstance(Class<T> targetClass) throws Exception;

}
