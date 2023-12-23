package org.ielts.playground.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In addition to the {@link org.ielts.playground.security.filter.PrivateResourceFilter}, this annotation
 * is to restrict which group of clients is allowed to access a specific private resource.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireClient {
    /**
     * Name of the permitted clients.
     */
    String[] name() default {};
}
