package com.socialmedia.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {

	private String commentId;
	private String userName;
	private String profilePictureUrl;
	private String content;
	private String postId;
}
