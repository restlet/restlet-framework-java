package org.restlet.ext.guice.examples;

import com.google.inject.*;
import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.*;

@Retention(RUNTIME)
@Target( { FIELD, PARAMETER })
@BindingAnnotation
public @interface HelloWorld {
}
