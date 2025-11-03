package com.dollop.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dollop.dto.ForgetPasswordDTO;
import com.dollop.dto.UserDTO;
import com.dollop.entity.TemperoryUser;
import com.dollop.entity.Users;
import com.dollop.exception.DuplicateEntryExecption;
import com.dollop.payload.ApiResponse;
import com.dollop.payload.AuthRequest;
import com.dollop.payload.OtpVerificationRequest;
import com.dollop.repository.TemperoryUseRepo;
import com.dollop.repository.UserRepository;
import com.dollop.service.impl.UserServiceImpl;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private UserServiceImpl userServ;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TemperoryUseRepo temperoryUseRepo;

	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserDTO userDto) {
		Map<String, Object> response = new HashMap<>();
		try {
			Map<String, Object> tempToken = userServ.registerWithOtp(userDto);
			tempToken.put("status", "OTP_SENT");
			tempToken.put("message", "OTP sent to your email");
//            response.put("tempToken", tempToken);
			return ResponseEntity.ok(tempToken);
		} catch (DuplicateEntryExecption e) {
			response.put("status", "ERROR");
			response.put("message", e.getMessage());
			response.put("errorCode", "DUPLICATE_EMAIL");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request) {
		Map<String, String> token = userServ.login(request.getPassword().trim(), request.getEmail().trim());
		System.err.println(token);
		return ResponseEntity.ok(token);
	}

	@PostMapping("/verify-login-otp")
	public ResponseEntity<ApiResponse> verifyLoginOtp(@RequestBody OtpVerificationRequest request) {
		request.setType("login");
		return ResponseEntity.ok(userServ.verifyOtp(request));
	}

	@PostMapping("/verify-registration-otp")
	public ResponseEntity<Map<String, Object>> verifyRegistrationOtp(@RequestBody OtpVerificationRequest request) {
		Optional<TemperoryUser> optionalTempUser = temperoryUseRepo.getUserByTempToken(request.getTempToken());

		if (optionalTempUser.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid temp token"));
		}

		TemperoryUser tempUser = optionalTempUser.get();

		if (!tempUser.getOtp().equals(request.getOtp())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid OTP"));
		}

		if (tempUser.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "OTP expired"));
		}

		Users user = new Users();
		BeanUtils.copyProperties(tempUser, user);
		user.setStatus("ACTIVE");
		user.setUserId(null);
		user = userRepository.save(user);
		System.err.println("user saved : " + user);
		temperoryUseRepo.delete(tempUser);

		return ResponseEntity.ok(Map.of("message", "OTP Verified successfully!", "registerWithOtp", true, "userId",
				user.getUserId(), "email", user.getEmail(), "status", "SUCCESS"));
	}

	@PutMapping("/forgetPass")
	public ResponseEntity<Users> forgetPassword(@RequestBody ForgetPasswordDTO forgetPasswordDTO) {
		return new ResponseEntity<Users>(userServ.forgetPassword(forgetPasswordDTO), HttpStatus.OK);
	}

}
