package com.socialmedia.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialmedia.entity.Post;
import com.socialmedia.entity.PostShare;
import com.socialmedia.entity.Users;

public interface PostShareRepository extends JpaRepository<PostShare, String> {

	// Check if this recipient already received the post before
	Optional<PostShare> findByPostAndSharedTo(Post post, Users sharedTo);

	@Query("SELECT COUNT(DISTINCT ps.sharedTo.id) FROM PostShare ps WHERE ps.post.id = :postId")
	long countUniqueSharesByPostId(@Param("postId") String postId);

}
