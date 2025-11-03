package com.socialmedia.validators;

import java.util.regex.Pattern;

import com.socialmedia.annotations.ValidPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

	private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		if (password == null || password.trim().isEmpty()) {
			return false; // Null or empty password is invalid
		}
		return Pattern.matches(PASSWORD_PATTERN, password);
	}
}
