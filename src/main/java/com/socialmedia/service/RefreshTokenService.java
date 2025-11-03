package com.socialmedia.service;

import java.util.Map;
import java.util.Optional;

import com.socialmedia.entity.RefreshToken;
import com.socialmedia.entity.Users;

public interface RefreshTokenService {

	void deleteByUser(String token);

	Optional<Users> findByToken(String requestToken);

	RefreshToken verifyExpiration(Users orElseThrow);

	Map<String, String> generateAccessToken(String refreshToken);

}
