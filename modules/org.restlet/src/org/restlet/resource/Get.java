/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.restlet.engine.connector.Method;
import org.restlet.service.MetadataService;

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
     * Specifies the media type extension of the response entity. If several
     * media types are supported, their extension can be specified separated by
     * "|" characters. Note that this isn't the full MIME type value, just the
     * extension name declared in {@link MetadataService}. For a list of all
     * predefined extensions, please check
     * {@link MetadataService#addCommonExtensions()}. New extension can be
     * registered using
     * {@link MetadataService#addExtension(String, org.restlet.data.Metadata)}
     * method.
     * 
     * @return The result media types.
     */
    String value() default "";

}
