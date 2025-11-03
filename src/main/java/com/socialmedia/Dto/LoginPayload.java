package com.socialmedia.Dto;

import com.socialmedia.annotations.Trimmed;
import com.socialmedia.annotations.ValidEmail;
import com.socialmedia.num.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Trimmed
public class LoginPayload {
	@ValidEmail
	private String email;
	private Role role;
	private String accessToken;
	private String refreshToken;
}