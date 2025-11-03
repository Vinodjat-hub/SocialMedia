package com.socialmedia.Dto;

import com.socialmedia.annotations.Trimmed;
import com.socialmedia.annotations.ValidEmail;
import com.socialmedia.annotations.ValidName;
import com.socialmedia.annotations.ValidPassword;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Trimmed
public class UserRequestDto {

	@Size(min = 3, max = 50)
	@Column(unique = true)
	@NotBlank(message = "UserName must not be blank")
	private String userName;
	@ValidName
	@Size(min = 3, max = 50)
	private String name;
	@NotBlank(message = "Please Enter Valid Email")
	@ValidEmail
	private String email;
	@NotBlank(message = "Please Enter Valid Password")
	@ValidPassword
	private String password;
	private String profilePictureUrl;
	private String bio;
	private String role;

}
