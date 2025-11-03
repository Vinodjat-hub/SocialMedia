package com.socialmedia.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.Dto.CommentRequest;
import com.socialmedia.Dto.CommentResponse;
import com.socialmedia.Dto.PostRequest;
import com.socialmedia.Dto.PostResponse;
import com.socialmedia.entity.Post;

public interface PostService {

	public PostResponse addPostWithImage(PostRequest postRequest, MultipartFile imageFile);

	public List<PostResponse> getPostOfUsers(String userId);

	public List<PostResponse> getAllPost();

	public List<PostResponse> getAllFolloingPost();

	public PostResponse getPostbyId(String postId);

	public String deletePost(String userId);

	public String likeUnlikePost(String postId);

	public CommentResponse commentOnPost(CommentRequest commentRequest);

//	public List<CommentResponse> comments(CommentRequest commentRequest);

	public String deleteComment(String commentId);

	public Post getPost(String postId);

	public List<CommentResponse> getCommentsByPostId(String postId);

	public String sharePost(String postId, String sharedToUserId);
}
