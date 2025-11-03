package com.socialmedia.serviceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socialmedia.entity.RefreshToken;
import com.socialmedia.entity.Users;
import com.socialmedia.exception.ResourceNotFoundException;
import com.socialmedia.num.Token;
import com.socialmedia.repo.RefreshTokenRepository;
import com.socialmedia.service.RefreshTokenService;
import com.socialmedia.util.JwtUtils;
import com.socialmedia.util.constants.ErrorConstants;

@Service
public class RefreshTokenImpl implements RefreshTokenService {

	@Autowired
	private JwtUtils jwtUtils;
	@Autowired
	private UserServiceImpl serviceImpl;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Override
	public void deleteByUser(String token) {
		Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
		if (refreshToken.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.TOKEN_NOT_FOUND);
		refreshTokenRepository.delete(refreshToken.get());
	}

	@Override
	public Optional<Users> findByToken(String requestToken) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public RefreshToken verifyExpiration(Users orElseThrow) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> generateAccessToken(String refreshToken) {
		System.err.println("----------------------------------------->>>>>>>>>>>>>>>>>>>>");
		Boolean valid = jwtUtils.validateToken(refreshToken, Token.REFRESH_TOKEN);
		System.err.println("valid ++++++++++ " + valid);
//		Optional<Users> user = serviceImpl.findLoggingUser();
		Optional<Users> user = serviceImpl.findUserByRefreshToken(refreshToken);

		System.err.println(user.get());
		System.err.println("++++++++++++++++++++");
		List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.get().getId());

		System.err.println("tokenss ---------->>>>> " + tokens);
//		if (!tokens.isEmpty()) {
//			tokens.stream().filter(token -> jwtUtils.isTokenExpired(token.getToken())) // check expired tokens
//					.forEach(refreshTokenRepository::delete); // delete each expired one
//		}

		String access_token = null;
		System.err.println("valid ->>>>>> " + valid);
		if (valid) {
			access_token = jwtUtils.generateToken(user.get().getEmail(), Token.ACCESS_TOKEN);
			System.err.println("access token ----- >>>>>>>>>> " + access_token);

		} else {
			throw new ResourceNotFoundException(ErrorConstants.TOKEN_EXPIRED);
		}
		return Map.of("accessToken", access_token);
	}
}
