//package com.socialmedia.repo;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.socialmedia.Dto.FollowUser;
//import com.socialmedia.entity.Users;
//
//public interface FollowUserRepository extends JpaRepository<FollowUser, Long> {
//	@Query("SELECT f.follower FROM FollowUser f WHERE f.following.follower_id = :follower_id AND f.status = 'PENDING'")
//	List<Users> findFollowRequests(@Param("username") String followerId);
//}
