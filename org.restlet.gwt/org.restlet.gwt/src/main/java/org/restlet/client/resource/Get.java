/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.restlet.client.engine.connector.Method;
import org.restlet.client.service.MetadataService;

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
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Method("GET")
public @interface Get {

    /**
     * Specifies the media type extension of the response entity. If several
     * media types are supported, their extension can be specified separated by
     * "|" characters. Note that this isn't the full MIME type value, just the
     * extension name declared in {@link MetadataService}. For a list of all
     * predefined extensions, please check
     * {@link MetadataService#addCommonExtensions()}. New extension can be
     * registered using
     * {@link MetadataService#addExtension(String, org.restlet.client.data.Metadata)}
     * method.
     * 
     * @return The result media types.
     */
    String value() default "";

}
