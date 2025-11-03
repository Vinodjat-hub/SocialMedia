package com.socialmedia.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FollowUser {
	@NotBlank(message = "Please Enter Valid Id")
	private String followingUserId;
}
