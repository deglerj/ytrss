package org.ytrss.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ChannelURLValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ChannelURL {

	Class<?>[] groups() default {};

	String message() default "Not a valid YouTube channel URL or connection is down";

	Class<? extends Payload>[] payload() default {};

}
