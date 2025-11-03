package com.socialmedia.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.socialmedia.num.Token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {

	@Value("${jwt.access.secret}")
	private String accessSecretStr;

	@Value("${jwt.refresh.secret}")
	private String refreshSecretStr;

	@Value("${jwt.access.ttl}")
	private long accessTtlMs;

	@Value("${jwt.refresh.ttl}")
	private long refreshTtlMs;

	@Autowired
	private HttpServletRequest request;
	private SecretKey accessKey;
	private SecretKey refreshKey;

	@PostConstruct
	public void initKeys() {
		this.accessKey = parseKey(accessSecretStr);
		this.refreshKey = parseKey(refreshSecretStr);
	}

	private SecretKey parseKey(String secret) {
		try {
			return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		} catch (IllegalArgumentException e) {
			return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
	}

	// ================= SINGLE TOKEN GENERATOR =================
	public String generateToken(String email, Token tokenType) {
		SecretKey key;
		long ttl;

		switch (tokenType) {
		case ACCESS_TOKEN:
		case AUTH_TOKEN:
			key = accessKey;
			ttl = accessTtlMs;
			break;
		case REFRESH_TOKEN:
			key = refreshKey;
			ttl = refreshTtlMs;
			break;
		default:
			throw new IllegalArgumentException("Invalid token type");
		}

		Date now = new Date();
		Date exp = new Date(now.getTime() + ttl);

		Map<String, Object> claims = new HashMap<>();
		claims.put("token_type", tokenType.name());

		System.err.println("in utils the email ------------ >> " + email);
		// IMPORTANT: set email as the subject
		return Jwts.builder().setSubject(email) // <-- email will be in "sub"
				.claim("token_type", tokenType.name()) // <-- add custom claim
				.setIssuedAt(now).setExpiration(exp).signWith(key, SignatureAlgorithm.HS512).compact();

	}

	// ================= VALIDATION =================
	public boolean validateToken(String token, Token tokenType) {
		SecretKey key = (tokenType == Token.REFRESH_TOKEN) ? refreshKey : accessKey;
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	public String getSubjectFromToken(String token, Token tokenType) {
		System.err.println("token ===>> " + token);
		SecretKey key = (tokenType == Token.REFRESH_TOKEN) ? refreshKey : accessKey;
		Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		return claims.getSubject();
	}

	public String getHeader(String token, String key, Token tokenType) {
		SecretKey secretKey = (tokenType == Token.REFRESH_TOKEN) ? refreshKey : accessKey;
		Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
		return (String) claims.get(key);
	}

	public String getTokenFromHeader() {
		return request.getHeader("Authorization").substring(7);
	}

	private Claims extractAllClaims(String token) {
		SecretKey key = Keys.hmacShaKeyFor(refreshSecretStr.getBytes(StandardCharsets.UTF_8));

		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	// ✅ Extract expiration date
	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	// ✅ Check if token expired
	public boolean isTokenExpired(String token) {
		try {
			Date expiration = extractExpiration(token);
			return expiration.before(new Date());
		} catch (Exception e) {
			return true; // If invalid token, treat it as expired
		}
	}

}
