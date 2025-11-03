package com.socialmedia.Dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socialmedia.num.AccountType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsersWithPost {

	private String id;
	private String username;
	private String name;
	private String email;
	private String profilePictureUrl;
	private String bio;
	private Long followers;
	private Long following;
	private Long postCount;
	private boolean isMe;
	@JsonProperty("isFollowing")
	private boolean isfollowing;
	@JsonProperty("hasSentFollowRequest")
	private boolean hasSentFollowingrequest;
	private boolean isRequestReceiver;
	private AccountType accountType;
	private List<PostResponse> posts;
}
