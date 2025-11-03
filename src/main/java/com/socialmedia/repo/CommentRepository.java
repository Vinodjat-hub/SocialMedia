package com.socialmedia.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialmedia.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, String> {

	@Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
	List<Comment> findAllCommentsByPostId(@Param("postId") String postId);
}
