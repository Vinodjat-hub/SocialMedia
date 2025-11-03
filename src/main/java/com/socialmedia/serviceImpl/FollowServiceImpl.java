package com.socialmedia.serviceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialmedia.Dto.FollowUser;
import com.socialmedia.Dto.FollowingWithPostsResponse;
import com.socialmedia.Dto.PostResponse;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.Users;
import com.socialmedia.exception.ResourceNotFoundException;
import com.socialmedia.num.AccountType;
import com.socialmedia.num.RequestAcceptReject;
import com.socialmedia.repo.UserRepository;
import com.socialmedia.service.FollowService;
import com.socialmedia.util.constants.ErrorConstants;

@Service
public class FollowServiceImpl implements FollowService {

	@Autowired
	public UserRepository userRepository;
	@Autowired
	public UserServiceImpl impl;

	@Transactional
	@Override
	public String followUser(FollowUser followUser) {
		Optional<Users> currentUserOpt = impl.findLoggingUser();
		Users currentUser = currentUserOpt.get();
		Users userToFollow = userRepository.findById(followUser.getFollowingUserId())
				.orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND));

		if (userToFollow.getType() == AccountType.PRIVATE) {
			// Add currentUser to the follow request of userToFollow
			userToFollow.getFollowRequest().add(currentUser);
			return "Follow Request Sent Successfully!!";
		} else {
			// Directly follow
			userToFollow.getFollowers().add(currentUser);
			currentUser.getFollowing().add(userToFollow);
			return "Followed Successfully!!";
		}
	}

	@Transactional
	@Override
	public String unFollowUser(FollowUser followUser) {

		Optional<Users> currentUserOpt = impl.findLoggingUser();
		Users currentUser = currentUserOpt.get();

		Users userToUnfollow = userRepository.findById(followUser.getFollowingUserId())
				.orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND));

		if (userToUnfollow.getType().equals(AccountType.PUBLIC)) {
			userToUnfollow.getFollowers().remove(currentUser);
			currentUser.getFollowing().remove(userToUnfollow);
			return "Unfollowed Successfully!!";
		}
		return "Cannot unfollow private account without request!!";
	}

	@Override
	public UserResponseDto changeUserStatus(AccountType accountType) {
		Optional<Users> userOpt = impl.findLoggingUser();
		Users user = userOpt.get();
		user.setType(accountType);
		user = userRepository.save(user);
		UserResponseDto response = UserResponseDto.builder().username(user.getName()).email(user.getEmail())
				.bio(user.getBio()).accountType(user.getType()).build();
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponseDto> followRequest() {
		Optional<Users> loggedInUserOpt = impl.findLoggingUser();
		Users loggedInUser = loggedInUserOpt.get();
		// ✅ Ensure proper type
		Set<Users> users = userRepository.findFollowRequestsByUserId(loggedInUser.getId());

		// ✅ Stream mapping with explicit generic types
		List<UserResponseDto> responseList = users.stream()
				.map((Users u) -> UserResponseDto.builder().id(u.getId()).username(u.getName()).email(u.getEmail())
						.profilePictureUrl(u.getProfilePictureUrl()).accountType(u.getType()).role(u.getRole())
						.bio(u.getBio()).build())
				.collect(Collectors.toList()); // ✅ correctly closed

		return responseList;
	}

	@Override
	public String acceptRejectFollowRequest(String requestUserId, RequestAcceptReject status) {
		Users user = impl.findUserById(requestUserId);
		Optional<Users> currentUserOpt = impl.findLoggingUser();
		Users currentUser = currentUserOpt.get();
		boolean isInFollowRequests = currentUser.getFollowRequest().stream()
				.anyMatch(u -> u.getId().equals(user.getId()));

		if (!isInFollowRequests) {
			return "No follow request from this user found!";
		}
		if (RequestAcceptReject.ACCEPT.equals(status)) {
			currentUser.getFollowers().add(user);

			currentUser.getFollowRequest().removeIf(u -> u.getId().equals(user.getId()));

			userRepository.save(currentUser);

			return "Follow Request Accepted Successfully!!";
		}

		return "Follow Request Not Accepted!!";
	}

	@Override
	@Transactional(readOnly = true)
	public List<FollowingWithPostsResponse> getAllFollowingWithPost() {
		Optional<Users> currentUserOpt = impl.findLoggingUser();
		Users currentUser = currentUserOpt.get();
		Set<Users> following = currentUser.getFollowing();

		return following.stream().map((Users user) -> {
			List<PostResponse> posts = user.getPosts().stream()
					.sorted(Comparator.comparing(Post::getCreatedAt).reversed())
					.map((Post post) -> PostResponse.builder().id(post.getId()).content(post.getContent())
							.imageUrl(post.getImageUrl()).createdAt(post.getCreatedAt())
							.likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
							.userResponseDto(UserResponseDto.fromEntity(post.getUser())).build())
					.collect(Collectors.toList());

			return FollowingWithPostsResponse.builder().userId(user.getId()).username(user.getName())
					.profilePic(user.getProfilePictureUrl()).posts(posts).build();
		}).collect(Collectors.toList());

	}

	@Override
	@Transactional
	public List<FollowingWithPostsResponse> getAllFollowersWithPost() {
		Optional<Users> currentUserOpt = impl.findLoggingUser();
		Users currentUser = currentUserOpt.get();
		Set<Users> followers = currentUser.getFollowers();

		return followers.stream().map((Users user) -> {
			List<PostResponse> posts = user.getPosts().stream()
					.sorted(Comparator.comparing(Post::getCreatedAt).reversed())
					.map((Post post) -> PostResponse.builder().id(post.getId()).content(post.getContent())
							.imageUrl(post.getImageUrl()).createdAt(post.getCreatedAt())
							.likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
							.userResponseDto(UserResponseDto.fromEntity(post.getUser())).build())
					.collect(Collectors.toList());

			return FollowingWithPostsResponse.builder().userId(user.getId()).username(user.getName())
					.profilePic(user.getProfilePictureUrl()).posts(posts).build();
		}).collect(Collectors.toList());
	}

}
