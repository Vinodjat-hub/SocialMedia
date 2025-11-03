package com.socialmedia.Dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
	private String id;
	private String content;
	private String imageUrl;
	private LocalDateTime createdAt;
	private int likeCount;
	private int commentsCount;
	private long shareCount;
	private boolean likedByMe;

	private UserResponseDto userResponseDto;
}
