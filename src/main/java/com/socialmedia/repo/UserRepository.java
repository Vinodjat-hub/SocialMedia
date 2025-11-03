package com.socialmedia.repo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.Dto.UserSearchDto;
import com.socialmedia.entity.Users;
import com.socialmedia.num.Role;

public interface UserRepository extends JpaRepository<Users, String> {

	Users getUserByEmail(String email);

	Optional<Users> findByEmail(String email);

	List<Users> findByRoleAndIsDeletedOrderByEmail(Role admin, boolean b);

	@Query("SELECT u FROM Users u")
	List<Users> findAllWithFilter(Pageable pageable);

	@Query("SELECT u.followRequest FROM Users u WHERE u.id = :id")
	Set<Users> findFollowRequestsByUserId(@Param("id") String id);

	@Query("SELECT u FROM Users u JOIN u.refreshTokens r WHERE r.token = :token")
	Optional<Users> findUserByRefreshToken(@Param("token") String token);

	@Query("""
			    SELECT u, COUNT(mutual.id) as mutualCount
			    FROM Users u
			    JOIN u.followers mutual
			    WHERE mutual IN (
			        SELECT f FROM Users cu
			        JOIN cu.following f
			        WHERE cu.id = :userId
			    )
			    AND u.id <> :userId
			    AND u NOT IN :myFollowing
			    GROUP BY u
			    ORDER BY mutualCount DESC
			""")
	List<Object[]> findSuggestions(@Param("userId") String userId, @Param("myFollowing") Set<Users> myFollowing);

	@Query("SELECT u FROM Users u WHERE LOWER(u.userName) LIKE LOWER(CONCAT( :userName, '%'))")
	Optional<Users> findByUserName(String userName);

	@Query("""
			    SELECT new com.socialmedia.Dto.UserSearchDto(
			        u.id,
			        u.userName,
			        u.email,
			        u.profilePictureUrl,
			        u.bio,
			        u.type,
			        CASE WHEN :currentUser MEMBER OF u.followers THEN true ELSE false END,
			        CASE WHEN :currentUser MEMBER OF u.following THEN true ELSE false END
			    )
			    FROM Users u
			    WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :userName, '%'))
			      AND u.id <> :currentUserId
			""")
	List<UserSearchDto> searchUsersWithFollowStatus(@Param("userName") String userName,
			@Param("currentUser") Users currentUser, @Param("currentUserId") String currentUserId);

	@Query("SELECT u FROM Users u WHERE u.email = :email")
	UserResponseDto getByEmail(@Param("email") String email);

	@Query("SELECT u FROM Users u WHERE LOWER(u.userName) LIKE LOWER(CONCAT( :userName, '%'))")
	List<Users> findAllByUserName(@Param("userName") String userName);

	@Query("SELECT u FROM Users u WHERE LOWER(u.name) LIKE LOWER(CONCAT( :name, '%'))")
	List<Users> findByName(String name);

//	@Query("""
//			    SELECT new com.socialmedia.dto.UserResponse(u.id, u.name, u.userName, u.profilePictureUrl)
//			    FROM Users usr
//			    JOIN usr.followRequest u
//			    WHERE usr.id = :userId
//			""")
//	List<UserResponseDto> findFollowRequests(@Param("userId") String userId);

}
