package com.socialmedia.Dto;

import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.annotations.Trimmed;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Trimmed
public class UpdateUser {

	@NotBlank(message = "Plaese enter valid user name")
	private String username;
	@NotBlank(message = "Plaese enter valid name")
	private String name;
	private String profilePictureUrl;
	@NotBlank(message = "Plaese enter valid bio")
	private String bio;
	MultipartFile file;
}
