package com.socialmedia.serviceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.socialmedia.Dto.CommentRequest;
import com.socialmedia.Dto.CommentResponse;
import com.socialmedia.Dto.PostRequest;
import com.socialmedia.Dto.PostResponse;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.entity.Comment;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.PostShare;
import com.socialmedia.entity.Users;
import com.socialmedia.exception.ResourceNotFoundException;
import com.socialmedia.repo.CommentRepository;
import com.socialmedia.repo.PostRepository;
import com.socialmedia.repo.PostShareRepository;
import com.socialmedia.repo.UserRepository;
import com.socialmedia.service.PostService;
import com.socialmedia.service.UserService;
import com.socialmedia.util.constants.ErrorConstants;

@Service
public class PostServiceImpl implements PostService {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private Cloudinary cloudinary;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostShareRepository postShareRepository;

	@Override
	public PostResponse addPostWithImage(PostRequest postRequest, MultipartFile imageFile) {
		// Fetch the actual persistent user entity
		Optional<Users> userOpt = userService.findLoggingUser();
		Users user = userOpt.get();
		String imageUrl = null;
		if (imageFile != null && !imageFile.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
				imageUrl = uploadResult.get("secure_url").toString();
			} catch (Exception e) {
				throw new RuntimeException("Cloudinary upload failed", e);
			}
		}

		Post post = Post.builder().content(postRequest.getContent()).imageUrl(imageUrl).createdAt(LocalDateTime.now())
				.user(user) // ✅ managed entity
				.likes(new HashSet<>()).build();

		Post savedPost = postRepository.save(post);

		return PostResponse.builder().id(savedPost.getId()).content(savedPost.getContent())
				.imageUrl(savedPost.getImageUrl()).createdAt(savedPost.getCreatedAt())
				.userResponseDto(UserResponseDto.fromEntity(post.getUser())).likeCount(savedPost.getLikes().size())
				.commentsCount(0).build();
	}

	@Override
	public List<PostResponse> getPostOfUsers(String userId) {
		List<Post> posts = postRepository.findAllPostsWithUserAndLikes(userId);
		if (posts.isEmpty()) {
			throw new ResourceNotFoundException(ErrorConstants.POSTS_NOT_FOUND);
		}

		return posts.stream().sorted(Comparator.comparing(Post::getCreatedAt).reversed())
				.map(post -> PostResponse.builder().id(post.getId()).content(post.getContent())
						.imageUrl(post.getImageUrl()).createdAt(post.getCreatedAt())
						.userResponseDto(UserResponseDto.fromEntity(post.getUser()))
						.likeCount(post.getLikes() != null ? post.getLikes().size() : 0).build())
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<PostResponse> getAllPost() {
		List<Post> posts = postRepository.findAll();

		if (posts.isEmpty()) {
			throw new ResourceNotFoundException(ErrorConstants.POSTS_NOT_FOUND);
		}

		return posts.stream().sorted(Comparator.comparing(Post::getCreatedAt).reversed())
				.map(post -> PostResponse.builder().id(post.getId()).content(post.getContent())
						.imageUrl(post.getImageUrl()).createdAt(post.getCreatedAt())
						.userResponseDto(UserResponseDto.fromEntity(post.getUser()))
						.likeCount(post.getLikes() != null ? post.getLikes().size() : 0).build())
				.collect(Collectors.toList());
	}

	@Override
	public String deletePost(String postId) {
		Optional<Post> post = postRepository.findById(postId);
		if (post.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.POSTS_NOT_FOUND);
		postRepository.delete(post.get());
		return "Post Deleted Successfully!!";
	}

	@Override
	@Transactional(readOnly = true)
	public List<PostResponse> getAllFolloingPost() {
		Optional<Users> currentUserOpt = userService.findLoggingUser();
		if (currentUserOpt.isEmpty())
			return Collections.emptyList();

		Users currentUser = currentUserOpt.get();

		// Get IDs of users the current user is following
		Set<String> followingIds = currentUser.getFollowing().stream().map(Users::getId).collect(Collectors.toSet());

		List<Post> posts;

		if (followingIds.isEmpty()) {
			// No following → only current user's posts
			posts = postRepository.findAllPostsByCurrentUser(currentUser.getId());
		} else {
			// Fetch posts from following + current user
			posts = postRepository.findAllPostsByFollowingAndSelf(followingIds, currentUser.getId());
		}

		if (posts.isEmpty())
			return Collections.emptyList();

		// Map posts to PostResponse including unique share count and likedByMe
		return posts.stream().map(post -> {
			long shareCount = postShareRepository.countUniqueSharesByPostId(post.getId());
			boolean likedByMe = post.getLikes() != null
					&& post.getLikes().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));

			return PostResponse.builder().id(post.getId()).content(post.getContent()).imageUrl(post.getImageUrl())
					.createdAt(post.getCreatedAt()).userResponseDto(UserResponseDto.fromEntity(post.getUser()))
					.likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
					.commentsCount(post.getComments() != null ? post.getComments().size() : 0).shareCount(shareCount)
					.likedByMe(likedByMe) // <-- added here
					.build();
		}).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public String likeUnlikePost(String postId) {
		Optional<Users> userOpt = userService.findLoggingUser();
		Users user = userOpt.get();
		Optional<Post> postOpt = postRepository.findById(postId);
		if (postOpt.isEmpty()) {
			throw new ResourceNotFoundException(ErrorConstants.POSTS_NOT_FOUND);
		}
		Post post = postOpt.get();
		boolean likedByCurrentUser = post.getLikes().stream()
				.anyMatch(like -> like.getUserName().equals(user.getUserName()));
		if (!likedByCurrentUser) {
			post.getLikes().add(user);
			postRepository.save(post);
			return "Post Liked Successfully!!";
		} else {
			post.getLikes().remove(user);
			postRepository.save(post);
			return "Post Unliked Successfully!!";
		}
	}

	@Override
	public CommentResponse commentOnPost(CommentRequest commentRequest) {
		Optional<Users> userOpt = userService.findLoggingUser();
		Users user = userOpt.get();
		Optional<Post> postOpt = postRepository.findById(commentRequest.getPostId());
		if (postOpt.isEmpty()) {
			throw new ResourceNotFoundException(ErrorConstants.POSTS_NOT_FOUND);
		}
		Post post = postOpt.get();
		System.err.println(user);
		Comment comments = Comment.builder().createdAt(LocalDateTime.now()).content(commentRequest.getContent())
				.user(user).post(post).build();
		Comment comment = commentRepository.save(comments);
		CommentResponse commentResponse = CommentResponse.builder().commentId(comment.getId())
				.profilePictureUrl(user.getProfilePictureUrl()).postId(comment.getPost().getId())
				.userName(user.getUserName()).content(comment.getContent()).build();

		return commentResponse;

	}

	@Override
	public String deleteComment(String commentId) {
		Optional<Comment> commentOpt = commentRepository.findById(commentId);
		if (commentOpt.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.COMMENT_NOT_FOUND);
		commentRepository.deleteById(commentId);
		return "Comment Deleted Successfully!!";
	}

	@Override
	public Post getPost(String postId) {
		Optional<Post> postOpt = postRepository.findById(postId);
		if (postOpt.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.POSTS_NOT_FOUND);
		return postOpt.get();
	}

	@Override
	@Transactional(readOnly = true)
	public List<CommentResponse> getCommentsByPostId(String postId) {
		Post post = getPost(postId);
		List<Comment> comments = commentRepository.findAllCommentsByPostId(postId);

		return comments.stream()
				.map(c -> CommentResponse.builder().commentId(c.getId()).content(c.getContent())
						.postId(c.getPost() != null ? c.getPost().getId() : null)
						.userName(c.getUser() != null ? c.getUser().getUserName() : "username")
						.profilePictureUrl(c.getUser().getProfilePictureUrl()).build())
				.collect(Collectors.toList());
	}

	@Override
	public String sharePost(String postId, String sharedToUserId) {
		Optional<Users> currentUserOpt = userService.findLoggingUser(); // however you get current user
		Users currentUser = currentUserOpt.get();
		Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

		Users sharedTo = userRepository.findById(sharedToUserId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// ✅ Check if this recipient already got the post before
		boolean alreadyReceived = postShareRepository.findByPostAndSharedTo(post, sharedTo).isPresent();
		if (alreadyReceived) {
			return "This user has already received the post before — not counted again.";
		}

		// ✅ Save new share record
		PostShare share = PostShare.builder().post(post).sharedBy(currentUser).sharedTo(sharedTo)
				.sharedAt(LocalDateTime.now()).build();

		postShareRepository.save(share);

		return "Post shared successfully!";
	}

	@Override
	@Transactional(readOnly = true)
	public PostResponse getPostbyId(String postId) {
		Optional<Post> postOpt = postRepository.findById(postId);

		if (postOpt.isEmpty()) {
			throw new ResourceNotFoundException("Post not found with ID: " + postId);
		}

		Post post = postOpt.get();

		// Count unique shares
		long shareCount = postShareRepository.countUniqueSharesByPostId(post.getId());

		PostResponse response = PostResponse.builder().id(post.getId()).content(post.getContent())
				.imageUrl(post.getImageUrl()).createdAt(post.getCreatedAt())
				.userResponseDto(UserResponseDto.fromEntity(post.getUser())) // include user info
				.likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
				.commentsCount(post.getComments() != null ? post.getComments().size() : 0).shareCount(shareCount)
				.build();

		return response;
	}

}
