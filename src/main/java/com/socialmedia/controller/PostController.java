package com.socialmedia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.Dto.ApiResponse;
import com.socialmedia.Dto.CommentRequest;
import com.socialmedia.Dto.CommentResponse;
import com.socialmedia.Dto.PostRequest;
import com.socialmedia.Dto.PostResponse;
import com.socialmedia.service.PostService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/posts")
public class PostController {

	@Autowired
	private PostService postService;

	// ✅ Create post (with image optional)
	@PostMapping(value = "/add", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiResponse<?>> createPost(@Valid @RequestParam("content") String content,
			@RequestPart(value = "image", required = false) MultipartFile imageFile) {
		try {
			PostRequest postRequest = PostRequest.builder().content(content).build();
			PostResponse response = postService.addPostWithImage(postRequest, imageFile);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(response, "Post created successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to create post: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Get all posts
	@GetMapping("/all")
	public ResponseEntity<ApiResponse<?>> getAllPosts() {
		try {
			List<PostResponse> posts = postService.getAllPost();
			return ResponseEntity.ok(ApiResponse.success(posts, "All posts fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					ApiResponse.error("Failed to fetch posts: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	@GetMapping("getPost/{postId}")
	public ResponseEntity<PostResponse> getPostById(@PathVariable String postId) {
		PostResponse postResponse = postService.getPostbyId(postId);
		return ResponseEntity.ok(postResponse);
	}

	// ✅ Get user posts by userId
	@GetMapping("/user/{userId}")
	public ResponseEntity<ApiResponse<?>> getUserPosts(@PathVariable String userId) {
		try {
			List<PostResponse> posts = postService.getPostOfUsers(userId);
			return ResponseEntity.ok(ApiResponse.success(posts, "User posts fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to fetch user posts: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Delete post
	@DeleteMapping("/delete/{postId}")
	public ResponseEntity<ApiResponse<?>> deletePost(@PathVariable String postId) {
		try {
			String message = postService.deletePost(postId);
			return ResponseEntity.ok(ApiResponse.success(message, "Post deleted successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to delete post: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Get all posts from followings
	@GetMapping("/getFollowingPosts")
	public ResponseEntity<ApiResponse<?>> getAllFollowingsPost() {
		try {
			List<PostResponse> posts = postService.getAllFolloingPost();
			System.err.println("posts ------------------------- >>>>>>>>>>> " + posts);
			return ResponseEntity.ok(ApiResponse.success(posts, "Following's posts fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse
					.error("Failed to fetch followings' posts: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	// ✅ Like or Unlike post
	@PostMapping("/likeUnlikePost")
	public ResponseEntity<ApiResponse<?>> likeUnlikePost(@RequestParam String postId) {
		try {
			String result = postService.likeUnlikePost(postId);
			return ResponseEntity.ok(ApiResponse.success(result, "Post like/unlike status updated successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to like/unlike post: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Comment on post
	@PostMapping("/comment")
	public ResponseEntity<ApiResponse<?>> commentOnPost(@Valid @RequestBody CommentRequest commentRequest) {
		try {
			CommentResponse response = postService.commentOnPost(commentRequest);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(response, "Comment added successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to add comment: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Delete comment
	@DeleteMapping("/deleteComment")
	public ResponseEntity<ApiResponse<?>> deleteComment(@RequestParam String commentId) {
		try {
			String message = postService.deleteComment(commentId);
			return ResponseEntity.ok(ApiResponse.success(message, "Comment deleted successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to delete comment: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Get comments by postId
	@GetMapping("/getComments")
	public ResponseEntity<ApiResponse<?>> getComments(@RequestParam String postId) {
		try {
			List<CommentResponse> comments = postService.getCommentsByPostId(postId);
			return ResponseEntity.ok(ApiResponse.success(comments, "Comments fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to fetch comments: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	@PostMapping("/share")
	public ResponseEntity<ApiResponse<?>> sharePost(@RequestParam String postId, @RequestParam String sharedToUserId) {
		try {
			String message = postService.sharePost(postId, sharedToUserId);
			return ResponseEntity.ok(ApiResponse.success(message, "Post shared successfully"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Post share failed: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

}
