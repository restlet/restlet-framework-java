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
 * Annotation for methods that store submitted representations. Its semantics is
 * equivalent to an HTTP PUT method. Note that your method must have one input
 * parameter if you want it to be selected for requests containing an
 * entity.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * &#064;Put
 * public MyOutputBean store(MyInputBean input);
 * 
 * &#064;Put(&quot;json&quot;)
 * public String storeJson(String value);
 * 
 * &#064;Put(&quot;json|xml:xml|json&quot;)
 * public Representation store(Representation value);
 * 
 * &#064;Put(&quot;json?param=val&quot;)
 * public Representation storeWithParam(String value);
 * 
 * &#064;Put(&quot;json?param&quot;)
 * public Representation storeWithParam(String value);
 * 
 * &#064;Put(&quot;?param&quot;)
 * public Representation storeWithParam(String value);
 * </pre>
 * 
 * @author Jerome Louvel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Method("PUT")
public @interface Put {

	/**
	 * Specifies the media type of the request and response entities as extensions.
	 * If only one extension is provided, the extension applies to both request and
	 * response entities. If two extensions are provided, separated by a colon, then
	 * the first one is for the request entity and the second one for the response
	 * entity.<br>
	 * <br>
	 * If several media types are supported, their extension can be specified
	 * separated by "|" characters. Note that this isn't the full MIME type value,
	 * just the extension name declared in {@link MetadataService}. For a list of
	 * all predefined extensions, please check
	 * {@link MetadataService#addCommonExtensions()}. New extension can be
	 * registered using
	 * {@link MetadataService#addExtension(String, org.restlet.data.Metadata)}
	 * method.
	 * 
	 * @return The media types of request and/or response entities.
	 */
	String value() default "";

}
