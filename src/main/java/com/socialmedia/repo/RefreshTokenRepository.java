package com.socialmedia.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.entity.RefreshToken;
import com.socialmedia.entity.Users;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
	Optional<RefreshToken> findByToken(String token);

	void deleteByUser(Users user);

	RefreshToken findByUserEmail(String email);

	List<RefreshToken> findByUserId(String userId);

}