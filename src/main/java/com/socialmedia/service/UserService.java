package com.socialmedia.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.socialmedia.Dto.LoginPayload;
import com.socialmedia.Dto.OtpVerificationPayload;
import com.socialmedia.Dto.Payload;
import com.socialmedia.Dto.UpdateUser;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.Dto.UserSearchDto;
import com.socialmedia.Dto.UsersWithPost;
import com.socialmedia.entity.TempreroryUser;
import com.socialmedia.entity.Users;

public interface UserService {

	public LoginPayload loginUser(Payload payload);

	public Optional<Users> findLoggingUser();

	Map<String, String> registerUser(TempreroryUser tempreroryUser);

	public UserResponseDto verifyOtpAtRegister(OtpVerificationPayload payload);

	public List<Users> getAllUser(int page, int size);

	public UserResponseDto getUserByEmail(String email);

	public List<UserSearchDto> getUserByUserName(String userName);

	public Users findUserByUserName(String userName);

	public UserResponseDto updateUser(UpdateUser user, MultipartFile file);

	public UserResponseDto blockUser(String email);

	public String uploadImage(MultipartFile file, String userId);

	public Map<String, String> forgetPassword(String email);

	public String verifyOtpForForgetPassword(String otp);

	public String changePassword(String password);

	public Users findUserById(String userId);

	public UsersWithPost getMyProfile();

	public UsersWithPost getUsersProfile(String userName);

	public List<UserResponseDto> getSuggestions();

	public List<UserResponseDto> getUsersByUserName(String userName);

	public UserResponseDto getUser(String identifers);
}
