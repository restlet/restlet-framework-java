/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import org.restlet.engine.connector.Method;
import org.restlet.service.MetadataService;

import java.lang.annotation.*;

/**
 * Annotation for methods that retrieve a resource representation. Its semantics
 * is equivalent to an HTTP GET method.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * &#064;Get
 * public MyBean represent();
 * 
 * &#064;Get(&quot;json&quot;)
 * public String toJson();
 * 
 * &#064;Get(&quot;xml|html&quot;)
 * public Representation represent();
 * 
 * &#064;Get(&quot;json?param=val&quot;)
 * public Representation representWithParam();
 * 
 * &#064;Get(&quot;json?param&quot;)
 * public Representation representWithParam();
 * 
 * &#064;Get(&quot;?param&quot;)
 * public Representation representWithParam();
 * </pre>
 * 
 * @author Jerome Louvel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Method("GET")
public @interface Get {

	/**
	 * Specifies the media type extension of the response entity. If several media
	 * types are supported, their extension can be specified separated by "|"
	 * characters. Note that this isn't the full MIME type value, just the extension
	 * name declared in {@link MetadataService}. For a list of all predefined
	 * extensions, please check {@link MetadataService#addCommonExtensions()}. New
	 * extension can be registered using
	 * {@link MetadataService#addExtension(String, org.restlet.data.Metadata)}
	 * method.
	 * 
	 * @return The result media types.
	 */
	String value() default "";

}
