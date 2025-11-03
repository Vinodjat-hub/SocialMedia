package com.dollop.dto;

import java.time.LocalDateTime;

import com.dollop.enm.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

	@NotBlank(message = "Please Provide Valid Name")
	private String name;
	@NotBlank(message = "Please Provide Valid Email")
	@Email
	@Column(unique = true, nullable = false)
	private String email;
	@NotBlank(message = "Please Provide Valid Password")
	private String password;
	@NotBlank(message = "Please Provide Valid Mobile")
	@Pattern(regexp = "[6789][0-9]{9}",message = "Please Provide Valid Mobile")
	private String mobile;
	@NotBlank(message = "Please Provide Valid Role")
	private Role role;
	private String status;
	private LocalDateTime OtpExpiryAt;
}
