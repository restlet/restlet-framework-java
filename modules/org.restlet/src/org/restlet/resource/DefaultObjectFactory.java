package org.restlet.resource;


/**
 * Default object factory implementation, uses reflection to instantiate new
 * objects.
 * 
 * @author Raniz
 *
 */
public class DefaultObjectFactory
	implements ObjectFactory
{
	/** Singleton instance for easy access and memory saving purposes */
	public static DefaultObjectFactory INSTANCE = new DefaultObjectFactory();

	@Override
	public <T>T getInstance(Class<T> targetClass) throws Exception
	{
		return targetClass.newInstance();
	}

}
