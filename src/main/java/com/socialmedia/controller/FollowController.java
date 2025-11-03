package com.socialmedia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialmedia.Dto.ApiResponse;
import com.socialmedia.Dto.FollowUser;
import com.socialmedia.Dto.FollowingWithPostsResponse;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.num.AccountType;
import com.socialmedia.num.RequestAcceptReject;
import com.socialmedia.service.FollowService;

import jakarta.validation.Valid;

@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class FollowController {

	@Autowired
	private FollowService followService;

	// ✅ Follow user
	@PostMapping("/follow")
	public ResponseEntity<ApiResponse<?>> followUser(@Valid @RequestBody FollowUser followUser) {
		try {
			String result = followService.followUser(followUser);
			return ResponseEntity.ok(ApiResponse.success(result, "User followed successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to follow user: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Unfollow user
	@PostMapping("/unfollow")
	public ResponseEntity<ApiResponse<?>> unFollowUser(@Valid @RequestBody FollowUser followUser) {
		try {
			String result = followService.unFollowUser(followUser);
			return ResponseEntity.ok(ApiResponse.success(result, "User unfollowed successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to unfollow user: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Change account type
	@PatchMapping("/changeStatus")
	public ResponseEntity<ApiResponse<?>> changeUserAccountType(@RequestParam AccountType type) {
		try {
			UserResponseDto responseDto = followService.changeUserStatus(type);
			return ResponseEntity.ok(ApiResponse.success(responseDto, "Account type changed successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					ApiResponse.error("Failed to change account type: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Get follow requests
	@GetMapping("/followRequest")
	public ResponseEntity<ApiResponse<?>> followRequest() {
		try {
			List<UserResponseDto> users = followService.followRequest();
			return ResponseEntity.ok(ApiResponse.success(users, "Follow requests fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					ApiResponse.error("Failed to fetch follow requests: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Get all followings with their posts
	@GetMapping("/followingsWithPost")
	public ResponseEntity<ApiResponse<?>> getAllFollowingWithPost() {
		try {
			List<FollowingWithPostsResponse> users = followService.getAllFollowingWithPost();
			return ResponseEntity.ok(ApiResponse.success(users, "Followings with posts fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
					.error("Failed to fetch followings with posts: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Get all followers with their posts
	@GetMapping("/followersWithPost")
	public ResponseEntity<ApiResponse<?>> getAllFollowersWithPost() {
		try {
			List<FollowingWithPostsResponse> users = followService.getAllFollowersWithPost();
			return ResponseEntity.ok(ApiResponse.success(users, "Followers with posts fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
					.error("Failed to fetch followers with posts: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Accept / Reject follow request
	@PutMapping("/requestAcceptReject")
	public ResponseEntity<ApiResponse<?>> acceptRejectFollowRequest(@RequestParam String requestUserId,
			@RequestParam String status) {
		try {
			String result = followService.acceptRejectFollowRequest(requestUserId, RequestAcceptReject.valueOf(status));
			return ResponseEntity.ok(ApiResponse.success(result, "Follow request updated successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					ApiResponse.error("Failed to update follow request: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}
}
