package com.socialmedia.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NameValidator implements ConstraintValidator<com.socialmedia.annotations.ValidName, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.trim().isEmpty())
			return false;
		return value.matches("^[A-Za-z\\s]+$");
	}
}
