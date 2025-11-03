package com.socialmedia.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.Dto.ApiResponse;
import com.socialmedia.Dto.UpdateUser;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.Dto.UserSearchDto;
import com.socialmedia.Dto.UsersWithPost;
import com.socialmedia.entity.Users;
import com.socialmedia.service.RefreshTokenService;
import com.socialmedia.service.UserService;
import com.socialmedia.util.JwtUtils;

import io.jsonwebtoken.io.IOException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private RefreshTokenService tokenService;
	@Autowired
	private JwtUtils jwtUtils;

	// ✅ Update User
	@PutMapping("/update")
	public ResponseEntity<ApiResponse<?>> updateUser(@Valid UpdateUser user) throws IOException {
		try {
			UserResponseDto updatedUser = userService.updateUser(user, user.getFile());
			return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to update user: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Block/Unblock User
	@PutMapping("/block/{email}")
	public ResponseEntity<ApiResponse<?>> blockUnblockUser(@PathVariable String email) {
		try {
			UserResponseDto blockedUser = userService.blockUser(email);
			return ResponseEntity
					.ok(ApiResponse.success(blockedUser, "User block/unblock status updated successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to block/unblock user: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Upload Profile Image
	@PostMapping("/upload/{userId}")
	public ResponseEntity<ApiResponse<?>> uploadImage(@RequestParam("file") MultipartFile file,
			@PathVariable String userId) throws IOException {
		try {
			String imageUrl = userService.uploadImage(file, userId);
			return ResponseEntity.ok(ApiResponse.success(imageUrl, "Profile image uploaded successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Image upload failed: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Get All Users (Paginated)
	@GetMapping
	public ResponseEntity<ApiResponse<?>> getAllUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		try {
			List<Users> users = userService.getAllUser(page, size);
			return ResponseEntity.ok(ApiResponse.success(users, "All users fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					ApiResponse.error("Failed to fetch users: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	// ✅ Get User by Email
	@GetMapping("/by-email")
	public ResponseEntity<ApiResponse<?>> getUserByEmail(@RequestParam String email) {
		try {
			UserResponseDto user = userService.getUserByEmail(email);
			return ResponseEntity.ok(ApiResponse.success(user, "User fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error("User not found: " + e.getMessage(), HttpStatus.NOT_FOUND));
		}
	}

	// ✅ Get Profile with Posts
	@GetMapping("/Myprofile")
	public ResponseEntity<ApiResponse<?>> getCurrentProfile() {
		try {
			UsersWithPost user = userService.getMyProfile();
			return ResponseEntity.ok(ApiResponse.success(user, "User profile fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to fetch profile: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<?>> getProfile(@RequestParam String userName) {
//		try {
		System.err.println("in getProfile********************");
		UsersWithPost user = userService.getUsersProfile(userName);
		System.err.println("userProfile ---------- >>> " + user);
		return ResponseEntity.ok(ApiResponse.success(user, "User profile fetched successfully!"));
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//					.body(ApiResponse.error("Failed to fetch profile: " + e.getMessage(), HttpStatus.BAD_REQUEST));
//		}
	}

	// ✅ Get Current Logged-in User
	@GetMapping("/currentUser")
	public ResponseEntity<ApiResponse<?>> getCurrentUser() {
		try {
			Optional<Users> userOpt = userService.findLoggingUser();
			if (userOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(ApiResponse.error("No logged-in user found.", HttpStatus.NOT_FOUND));
			}
			return ResponseEntity.ok(ApiResponse.success(userOpt.get(), "Current user fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse
					.error("Failed to fetch current user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	// ✅ Forgot Password - Send OTP
	@PostMapping("/forget")
	public ResponseEntity<ApiResponse<?>> forgetPassword(@RequestParam String email) {
		try {
			Map<String, String> response = userService.forgetPassword(email);
			return ResponseEntity.ok(ApiResponse.success(response, "OTP sent successfully to registered email!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to send OTP: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Verify OTP for Forgot Password
	@PostMapping("/verifyOtpForForgetPassword")
	public ResponseEntity<ApiResponse<?>> verifyOtpForForgetPassword(@RequestParam String otp) {
		try {
			String result = userService.verifyOtpForForgetPassword(otp);
			return ResponseEntity.ok(ApiResponse.success(result, "OTP verified successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					ApiResponse.error("Invalid OTP or verification failed: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	// ✅ Change Password after OTP verification
	@PostMapping("/chngePass")
	public ResponseEntity<ApiResponse<?>> changePassword(@RequestParam String password) {
		try {
			String result = userService.changePassword(password);
			return ResponseEntity.ok(ApiResponse.success(result, "Password changed successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to change password: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	@GetMapping("/suggestions")
	public ResponseEntity<ApiResponse<List<UserResponseDto>>> getSuggestions() {
		try {
			List<UserResponseDto> suggestions = userService.getSuggestions();
			return ResponseEntity.ok(ApiResponse.success(suggestions, "User suggestions fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to fetch suggestions: " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	@GetMapping("/getUsers")
	public ResponseEntity<ApiResponse<List<UserSearchDto>>> getUsersByUserName(@RequestParam String userName) {
		try {
			System.err.println("userName ---- >> " + userName);
			List<UserSearchDto> searchedUsers = userService.getUserByUserName(userName);
			System.err.println("searchedUsers --------->>>>>>>>>>> " + searchedUsers);
			return ResponseEntity.ok(ApiResponse.success(searchedUsers, "User fetched successfully!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Failed to fetch users : " + e.getMessage(), HttpStatus.BAD_REQUEST));
		}
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken() {
		try {
			String refreshToken = jwtUtils.getTokenFromHeader();

			System.err.println("Refresh Token ----------- >> " + refreshToken);
			Map<String, String> response = tokenService.generateAccessToken(refreshToken);

			System.err.println("respo ---->>>>>>> " + response);
			return ResponseEntity.ok(ApiResponse.success(response, "Access Token Generated Successfully!!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("Refresh Token is experied !!", HttpStatus.BAD_REQUEST));
		}
	}

	@GetMapping("/byUserName")
	public ResponseEntity<ApiResponse<List<UserResponseDto>>> getUserByUsersName(@RequestParam String userName) {
		try {
			List<UserResponseDto> users = userService.getUsersByUserName(userName);
			return ResponseEntity.ok(ApiResponse.success(users, "User is available!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("User not fetched!!", HttpStatus.BAD_REQUEST));
		}
	}

	@GetMapping("/getUser")
	public ResponseEntity<ApiResponse<UserResponseDto>> getUsers(@RequestParam String identifiers) {
		try {
			UserResponseDto user = userService.getUser(identifiers);
			System.err.println("fetched user in getUser api --------->>>>>>>>>> " + user);
			return ResponseEntity.ok(ApiResponse.success(user, "User Fetched!!"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error("User not fetched!!", HttpStatus.BAD_REQUEST));
		}
	}
}
