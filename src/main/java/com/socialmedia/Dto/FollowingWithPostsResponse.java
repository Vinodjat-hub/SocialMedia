package com.socialmedia.Dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowingWithPostsResponse {
	private String userId;
	private String username;
	private String profilePic;
	private List<PostResponse> posts;
}
