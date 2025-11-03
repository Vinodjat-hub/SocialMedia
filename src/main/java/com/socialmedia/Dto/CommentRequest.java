package com.socialmedia.Dto;

import com.socialmedia.annotations.Trimmed;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Trimmed
public class CommentRequest {

	@NotBlank(message = "Please Enter Valid Comment")
	private String content;
	@NotBlank
	private String postId;
}
