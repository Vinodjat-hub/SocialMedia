package com.socialmedia.Dto;

import com.socialmedia.annotations.Trimmed;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Trimmed
//Payload.java
public class Payload {

	@NotBlank(message = "Identifier (username or email) is required")
	private String identifier;

	@NotBlank(message = "Password is required")
	private String password;

	// Getters and setters
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
