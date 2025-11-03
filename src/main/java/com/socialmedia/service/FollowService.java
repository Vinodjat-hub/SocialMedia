package com.socialmedia.service;

import java.util.List;

import com.socialmedia.Dto.FollowUser;
import com.socialmedia.Dto.FollowingWithPostsResponse;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.num.AccountType;
import com.socialmedia.num.RequestAcceptReject;

public interface FollowService {

	public String followUser(FollowUser user);

	public String unFollowUser(FollowUser user);

	public List<UserResponseDto> followRequest();

	public String acceptRejectFollowRequest(String requestUserId, RequestAcceptReject status);

	public UserResponseDto changeUserStatus(AccountType accountType);

	public List<FollowingWithPostsResponse> getAllFollowingWithPost();

	public List<FollowingWithPostsResponse> getAllFollowersWithPost();

}
