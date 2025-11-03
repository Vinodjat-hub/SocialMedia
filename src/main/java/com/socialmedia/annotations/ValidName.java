package com.socialmedia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = com.socialmedia.validators.NameValidator.class)
public @interface ValidName {
	String message() default "Invalid name — only letters and spaces allowed";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
