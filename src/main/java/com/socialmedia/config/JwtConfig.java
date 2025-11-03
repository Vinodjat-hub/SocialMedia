package com.socialmedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

	private String accessSecret;
	private String refreshSecret;
	private String tempSecret;
	private long accessTtl;
	private long refreshTtl;

}
