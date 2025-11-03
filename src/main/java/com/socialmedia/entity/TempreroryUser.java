package com.socialmedia.entity;

import java.time.LocalDateTime;

import com.socialmedia.annotations.ValidName;
import com.socialmedia.annotations.ValidPassword;
import com.socialmedia.num.OtpType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TempreroryUser {

	@Size(min = 3, max = 50)
	@Column(unique = true)
	@NotBlank(message = "UserName must not be blank")
	private String userName;
	@ValidName
	private String name;
	@Id
	@Email
	private String email;
	@ValidPassword
	private String password;
	private String profilePictureUrl;
	private String bio;
	private String otp;
	private OtpType otptype;
	public LocalDateTime otpGeneratedAt;
}
