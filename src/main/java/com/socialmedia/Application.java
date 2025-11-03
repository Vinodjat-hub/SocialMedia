package com.socialmedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.socialmedia.config.JwtConfig;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.socialmedia.repo")
@EntityScan(basePackages = "com.socialmedia.entity")
@EnableConfigurationProperties(JwtConfig.class)
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.err.println("🚀 Application started successfully!");
	}
}
