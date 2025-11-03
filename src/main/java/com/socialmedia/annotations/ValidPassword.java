package com.socialmedia.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.socialmedia.validators.ValidPasswordValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ValidPasswordValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
	String message() default "Password must be at least 8 characters long, "
			+ "contain uppercase, lowercase, number, and special character";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
