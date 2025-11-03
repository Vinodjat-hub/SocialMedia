package com.dollop.config.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dollop.entity.CustomUserDetails;
import com.dollop.entity.Users;
import com.dollop.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtil;
	private final UserDetailsService userDetailsService;

	private static final List<String> EXCLUDED_PATHS = List.of("/api/users/login", "/api/users/verify-login-otp",
			"/api/users/register", "/api/users/verify-registration-otp");

	public JwtFilter(JwtUtils jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		System.err.println("Request Path: " + path);

		// ✅ Skip JWT validation for open endpoints
		if (path.matches("/api/auth/login") || path.matches("/api/auth/register")
				|| path.matches("/api/auth/verify-login-otp") || path.matches("/api/auth/verify-registration-otp")
				|| path.matches("/api/auth/forgetPass") || path.startsWith("/api/users/get/")) {
			System.err.println("Skipping JWT filter for: " + path);
			filterChain.doFilter(request, response);
			return;
		}
		String authHeader = request.getHeader("Authorization");
		String token = null;
		String username = null;

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
			username = jwtUtil.getEmailFromTempToken(token); // <-- make sure method exists
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			
			if (userDetails instanceof CustomUserDetails customUser) {
			    if ("BLOCKED".equalsIgnoreCase(customUser.getStatus())) {
			        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			        return; // stop filter chain → frontend logs out
			    }
			}

	            System.err.println("in filters username <=============> "+username);

			if (jwtUtil.validateAccessToken(token)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
        System.err.println("before dofilter <=============> ");

		filterChain.doFilter(request, response);
	}
}