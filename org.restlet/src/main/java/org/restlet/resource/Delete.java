/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
 * Annotation for methods that remove representations. Its semantics are equivalent to an HTTP
 * DELETE method.<br>
 * <br>
 * Example:
 *
 * <pre>
 * &#064;Delete()
 * public void removeAll();
 *
 * &#064;Delete(&quot;xml|json&quot;)
 * public Representation removeAll();
 *
 * &#064;Delete(&quot;json?param=val&quot;)
 * public Representation removeAllWithParam();
 *
 * &#064;Delete(&quot;json?param&quot;)
 * public Representation removeAllWithParam();
 *
 * &#064;Delete(&quot;?param&quot;)
 * public Representation removeAllWithParam();
 * </pre>
 *
 * @author Jerome Louvel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Method("DELETE")
public @interface Delete {

    /**
     * Specifies the media type extension of the response entity. If several media types are
     * supported, their extension can be specified separated by "|" characters. Note that this isn't
     * the full MIME type value, just the extension name declared in {@link MetadataService}. For a
     * list of all predefined extensions, please check {@link
     * MetadataService#addCommonExtensions()}. New extension can be registered using {@link
     * MetadataService#addExtension(String, org.restlet.data.Metadata)} method.
     *
     * @return The result media types.
     */
    String value() default "";
}
