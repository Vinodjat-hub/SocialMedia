package com.socialmedia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
		System.err.println("SecurityConfig loaded ✅");
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		System.err.println("Configuring SecurityFilterChain...");
		http.csrf(csrf -> csrf.disable()).cors(cors -> {
		}).authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
						"/swagger-resources/**", "/webjars/**", "/v2/api-docs/**", "/actuator/**",
						"/api/users/byUserName", "/ws/**", "/topic/**")
				.permitAll()

				.requestMatchers("/api/users/update", "/api/users/by-email", "/api/users", "/api/chngePass",
						"/api/verifyOtpForForgetPassword", "/api/forget", "/api/users/requestAcceptReject",
						"/api/currentUser", "/api/posts/likeUnlikePost", "/api/users/refresh", "/api/users/Myprofile",
						"/api/users/profile", "/api/users/getUsers", "/api/users/getUser")
				.hasAnyRole("ADMIN", "USER")
				.requestMatchers("/api/posts/add", "/api/posts/all", "/api/posts/user/**", "/api/posts/delete/**",
						"/api/users/unfollow", "/api/users/follow", "/api/users/changeStatus",
						"/api/users/followRequest", "/api/posts/getFollowingPosts", "/api/users/followersWithPost",
						"/api/users/followingsWithPost", "/api/posts/comment", "/api/posts/deleteComment",
						"/api/posts/getComments", "/api/posts/share", "/api/users/suggestions", "/api/posts/getPost/**")
				.hasRole("USER").anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		System.err.println("SecurityFilterChain configured ✅");
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
