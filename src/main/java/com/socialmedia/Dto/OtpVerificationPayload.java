package com.socialmedia.Dto;

import com.socialmedia.annotations.Trimmed;
import com.socialmedia.num.Token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Trimmed
public class OtpVerificationPayload {

	private String token;
	private Token tokenType;
	private String otp;

}
