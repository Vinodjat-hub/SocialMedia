package com.socialmedia.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialmedia.entity.Post;

public interface PostRepository extends JpaRepository<Post, String> {

	List<Post> findAllByUserId(String userId);

	Post findByUserId(String userId);

	@Query("SELECT p FROM Post p " + "LEFT JOIN FETCH p.user " + "LEFT JOIN FETCH p.likes "
			+ "WHERE (:userId IS NULL OR p.user.id = :userId) " + "ORDER BY p.createdAt DESC")
	List<Post> findAllPostsWithUserAndLikes(@Param("userId") String userId);

	// Case 1: following not empty
	@Query("""
			    SELECT p
			    FROM Post p
			    JOIN FETCH p.user
			    WHERE p.user.id IN :followingIds OR p.user.id = :currentUserId
			    ORDER BY p.createdAt DESC
			""")
	List<Post> findAllPostsByFollowingAndSelf(@Param("followingIds") Set<String> followingIds,
			@Param("currentUserId") String currentUserId);

	// Case 2: following empty
	@Query("""
			    SELECT p
			    FROM Post p
			    JOIN FETCH p.user
			    WHERE p.user.id = :currentUserId
			    ORDER BY p.createdAt DESC
			""")
	List<Post> findAllPostsByCurrentUser(@Param("currentUserId") String currentUserId);

}
