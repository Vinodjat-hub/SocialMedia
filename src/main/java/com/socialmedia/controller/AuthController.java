package com.socialmedia.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.Dto.ApiResponse;
import com.socialmedia.Dto.LoginPayload;
import com.socialmedia.Dto.OtpVerificationPayload;
import com.socialmedia.Dto.Payload;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.entity.TempreroryUser;
import com.socialmedia.service.RefreshTokenService;
import com.socialmedia.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class AuthController {

//	private final JwtUtils jwtUtils;
//	private final PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;
	@Autowired
	private RefreshTokenService tokenService;

//	public AuthController(JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
//		System.err.println("into register");
//		this.jwtUtils = jwtUtils;
//		this.passwordEncoder = passwordEncoder;
//	}

//	@PostMapping("/register")
//	public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody TempreroryUser user) {
//		return new ResponseEntity<Map<String, String>>(userService.registerUser(user), HttpStatus.CREATED);
//	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody TempreroryUser user) {
//		try {
		System.err.println("user for register ========= >>..." + user);
		Map<String, String> user1 = userService.registerUser(user);
		return ResponseEntity.ok(ApiResponse.success(user1, "OTP Send To Your Email"));
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//					.body(ApiResponse.error("Not Registered !!", HttpStatus.BAD_REQUEST));
//		}
	}

//	@PostMapping("/verifyOtpAtRegister")
//	public ResponseEntity<UserResponseDto> verifyOtpAtRegister(@Valid @RequestBody OtpVerificationPayload payload) {
//		return new ResponseEntity<UserResponseDto>(userService.verifyOtpAtRegister(payload), HttpStatus.CREATED);
//	}

	@PostMapping("/verifyOtpAtRegister")
	public ResponseEntity<ApiResponse<?>> verifyOtpAtRegister(@Valid @RequestBody OtpVerificationPayload payload) {
		try {
			UserResponseDto responseDto = userService.verifyOtpAtRegister(payload);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(responseDto, "User Registered Successfully"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Not Registered !!", HttpStatus.BAD_REQUEST));
		}
	}

//	@PostMapping("/login")
//	public ResponseEntity<LoginPayload> login(@Valid @RequestBody Payload payload) {
//		return new ResponseEntity<LoginPayload>(userService.loginUser(payload), HttpStatus.OK);
//	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody Payload payload) {
		try {
			System.err.println(payload);
			LoginPayload loginResponse = userService.loginUser(payload);
			return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login Successfully!!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Bad credentials!!", HttpStatus.BAD_REQUEST));
		}
	}

//	@PostMapping("/refresh")
//	public ResponseEntity<?> refreshToken(@Valid @RequestParam String refreshToken) {
//		return new ResponseEntity<>(tokenService.generateAccessToken(refreshToken), HttpStatus.OK);
//	}

//	@PostMapping("/logout")
//	public ResponseEntity<?> logout(@Valid @RequestBody Map<String, String> request) {
//		Optional<Users> users = userService.findLoggingUser();
//		tokenService.deleteByUser(users.get());
//
//		return ResponseEntity.ok("Logged out successfully");
//	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<?>> logout(@Valid @RequestParam String refreshToken) {
		try {
			tokenService.deleteByUser(refreshToken);
			return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null, "Logout Successfully!!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Logout Failed!!", HttpStatus.BAD_REQUEST));
		}

	}

//	@PostMapping("/upload-profile/{userId}")
//	public ResponseEntity<String> uploadProfile(@Valid @PathVariable String userId,
//			@Valid @RequestParam("file") MultipartFile file) {
//		return new ResponseEntity<String>(userService.uploadImage(file, userId), HttpStatus.CREATED);
//	}

	@PostMapping("/upload-profile/{userId}")
	public ResponseEntity<ApiResponse<?>> uploadProfile(@Valid @PathVariable String userId,
			@Valid @RequestParam("file") MultipartFile file) {
		try {
			String str = userService.uploadImage(file, userId);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(str, "Profile Updated Successfully!!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Profile Not Uploaded!!", HttpStatus.BAD_REQUEST));
		}
	}

}
