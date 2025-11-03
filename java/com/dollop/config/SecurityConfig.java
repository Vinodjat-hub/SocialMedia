package com.dollop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dollop.config.filter.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtAuthFilter) {
		this.jwtFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		System.err.println("SecurityConfig SecurityFilterChain");
		http.csrf(csrf -> csrf.disable()).cors(cors -> {
		}).authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers("/api/auth/**", "/api/auth/register", "/api/auth/verify-login-otp",
						"/api/auth/verify-registration-otp", "/api/auth/forgetPass", "/api/users/get/**",
						"/api/products/update", "/api/products/get/**", "/api/order/update/**", "/ws/**",
						"/api/notifications/**", "/uploads/**")
				.permitAll()

				.requestMatchers("/api/users/getAllCount", "/api/users/getAll", "/api/users/getAllUsers", "/api/users/last7days",
						"/api/users/updateUsers/**", "/api/products/active/**", "/api/products/add",
						"/api/products/updateImg/**", "/api/products/delete/**", "/api/order/AllOrder",
						"/api/order/getByStatus/**", "/api/order/monthlyRevenue", "/api/order/totalRevenue")
				.hasAnyRole("ADMIN")

				.requestMatchers(HttpMethod.PUT, "/api/users/updateUser").hasAnyRole("CUSTOMER", "ADMIN")
				.requestMatchers("/api/users/profile", "/api/products/getAll", "/api/order/get/**","/api/products/getAllActiveProducts")
				.hasAnyRole("CUSTOMER", "ADMIN")

				.requestMatchers("/api/products/getAllProducts").hasAnyRole("ADMIN", "CUSTOMER")
				.requestMatchers("/api/order/placeOrder", "/api/order/myOrder", "/api/order/invoice/**")
				.hasAnyRole("CUSTOMER")

				.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v2/api-docs/**",
						"/swagger-resources/**", "/webjars/**", "/actuator/**")
				.permitAll()

				.anyRequest().authenticated())
				.sessionManagement(
						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		System.err.println("SecurityConfig AuthenticationManager");
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}