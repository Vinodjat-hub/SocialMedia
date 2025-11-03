package com.socialmedia.validators;

import java.util.regex.Pattern;

import com.socialmedia.annotations.ValidEmail;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

	private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		if (email == null || email.trim().isEmpty()) {
			return false; // Empty or null emails are invalid
		}
		return Pattern.matches(EMAIL_REGEX, email);
	}
}
