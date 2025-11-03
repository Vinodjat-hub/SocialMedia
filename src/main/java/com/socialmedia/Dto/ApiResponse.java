package com.socialmedia.Dto;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API Response Wrapper for sending consistent responses across
 * controllers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private T data; // Actual response data
	private boolean response; // true = success, false = failure
	private HttpStatus httpStatus; // HTTP status (e.g., OK, BAD_REQUEST)
	private String message; // Success or error message
	private LocalDateTime timestamp; // Time of response

	// Static factory methods for convenience
	public static <T> ApiResponse<T> success(T data, String message) {
		return ApiResponse.<T>builder().data(data).response(true).httpStatus(HttpStatus.OK).message(message)
				.timestamp(LocalDateTime.now()).build();
	}

	public static <T> ApiResponse<T> error(String message, HttpStatus status) {
		return ApiResponse.<T>builder().data(null).response(false).httpStatus(status).message(message)
				.timestamp(LocalDateTime.now()).build();
	}
}
