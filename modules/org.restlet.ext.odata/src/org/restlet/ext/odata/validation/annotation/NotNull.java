
package org.restlet.ext.odata.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated property can be null.
 *
 * @author Shantanu
 */

//Indicates that annotations with this type are to be retained by the VM so they can be read reflectively at run-time
@Retention(RetentionPolicy.RUNTIME)
//Indicates that this annotation type can be used to annotate only field 
@Target(ElementType.FIELD)
public @interface NotNull {
	
}
