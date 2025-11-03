package com.socialmedia.Dto;

import com.socialmedia.num.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchDto {
	private String id;
	private String username;
	private String email;
	private String profilePictureUrl;
	private String bio;
	private AccountType accountType;
	private boolean isFollowing; // current user is following this user
	private boolean isFollowedBy; // this user is following current user
}
