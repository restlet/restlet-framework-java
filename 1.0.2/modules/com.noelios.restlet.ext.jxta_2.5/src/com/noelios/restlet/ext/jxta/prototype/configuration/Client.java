package com.noelios.restlet.ext.jxta.prototype.configuration;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@BindingAnnotation
public @interface Client {
}
