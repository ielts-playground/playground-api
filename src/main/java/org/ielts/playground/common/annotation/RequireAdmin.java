package org.ielts.playground.common.annotation;

import org.ielts.playground.model.entity.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a handler accepts only requests from the Admins.
 *
 * @see Role#getAuthority()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireAdmin {
}
