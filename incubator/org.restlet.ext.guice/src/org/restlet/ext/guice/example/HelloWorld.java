package org.restlet.ext.guice.example;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;


/**
 * Qualifier for demonstrating use of FinderFactory with qualifiers.
 */
@Retention(RUNTIME)
@Target( { FIELD, PARAMETER })
@Qualifier
public @interface HelloWorld {
}
