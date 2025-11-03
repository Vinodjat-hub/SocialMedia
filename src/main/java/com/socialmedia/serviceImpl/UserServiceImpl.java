package com.socialmedia.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.socialmedia.Dto.LoginPayload;
import com.socialmedia.Dto.OtpVerificationPayload;
import com.socialmedia.Dto.Payload;
import com.socialmedia.Dto.PostResponse;
import com.socialmedia.Dto.UpdateUser;
import com.socialmedia.Dto.UserResponseDto;
import com.socialmedia.Dto.UserSearchDto;
import com.socialmedia.Dto.UsersWithPost;
import com.socialmedia.entity.RefreshToken;
import com.socialmedia.entity.TempreroryUser;
import com.socialmedia.entity.Users;
import com.socialmedia.exception.DuplicateEntryExecption;
import com.socialmedia.exception.InvalidLoginException;
import com.socialmedia.exception.ResourceNotFoundException;
import com.socialmedia.num.AccountType;
import com.socialmedia.num.OtpType;
import com.socialmedia.num.Role;
import com.socialmedia.num.Token;
import com.socialmedia.repo.PostRepository;
import com.socialmedia.repo.RefreshTokenRepository;
import com.socialmedia.repo.TempreroryUserRepo;
import com.socialmedia.repo.UserRepository;
import com.socialmedia.service.UserService;
import com.socialmedia.util.JwtUtils;
import com.socialmedia.util.constants.ErrorConstants;

import io.jsonwebtoken.io.IOException;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	public UserRepository userRepository;

	@Autowired
	public TempreroryUserRepo tempreroryUserRepo;

	@Autowired
	public PostRepository postRepository;

	@Autowired
	public PasswordEncoder passwordEncoder;

	@Autowired
	public JwtUtils jwtUtils;

	@Autowired
	public EmailService emailService;

	@Autowired
	public RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private Cloudinary cloudinary;

	@Override
	public Map<String, String> registerUser(TempreroryUser tempreroryUser) {
		TempreroryUser tempUser = tempreroryUserRepo.findByEmail(tempreroryUser.getEmail());
		if (tempUser != null) {
			tempreroryUserRepo.delete(tempUser);
		}
		UserResponseDto user = userRepository.getByEmail(tempreroryUser.getEmail());
		if (user != null)
			throw new DuplicateEntryExecption(ErrorConstants.EMAIL_ALREADY_EXIST);

		String otp = String.format("%06d", new Random().nextInt(999999));
		tempreroryUser.setOtp(otp);
		tempreroryUser.setPassword(passwordEncoder.encode(tempreroryUser.getPassword()));
		tempreroryUser.setOtpGeneratedAt(LocalDateTime.now());
		tempreroryUser.setOtptype(OtpType.REGISTRATION);
		tempreroryUser = tempreroryUserRepo.save(tempreroryUser);

		emailService.sendOtpEmail(tempreroryUser.getEmail(), otp, OtpType.REGISTRATION);

		String auth_token = jwtUtils.generateToken(tempreroryUser.getEmail(), Token.AUTH_TOKEN);

		Map<String, String> request = new HashMap<>();
		request.put("auth_token", auth_token);
		request.put("otp", otp);
		request.put("otp_type", "REGISTER_OTP");
		return request;
	}

	@Override
	public LoginPayload loginUser(Payload payload) {
		String identifier = payload.getIdentifier();
		Optional<Users> user;

		// Detect whether identifier is an email or username
		if (identifier.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
			// ✅ Login via email
			user = userRepository.findByEmail(identifier);
		} else {
			// ✅ Login via username
			user = userRepository.findByUserName(identifier);
		}

		if (user.isEmpty()) {
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);
		}

		if (!passwordEncoder.matches(payload.getPassword(), user.get().getPassword())) {
			throw new InvalidLoginException(ErrorConstants.PASSWORD_IS_INCORRECT);
		}

		// ✅ Generate tokens using email or username (choose one for payload)
		String subject = user.get().getEmail(); // or user.get().getUserName()
		String access_Token = jwtUtils.generateToken(subject, Token.ACCESS_TOKEN);
		String refresh_Token = jwtUtils.generateToken(subject, Token.REFRESH_TOKEN);

		LoginPayload load = LoginPayload.builder().email(user.get().getEmail()).role(user.get().getRole())
				.accessToken(access_Token).refreshToken(refresh_Token).build();

		// ✅ Save refresh token
		RefreshToken refreshToken = RefreshToken.builder().token(refresh_Token).user(user.get())
				.expiryDate(LocalDateTime.now().plusDays(7)).build();

		refreshTokenRepository.save(refreshToken);

		return load;
	}

	@Override
	public Optional<Users> findLoggingUser() {
		String email = jwtUtils.getSubjectFromToken(jwtUtils.getTokenFromHeader(), Token.REFRESH_TOKEN);
		Optional<Users> user = userRepository.findByEmail(email);
		if (user.isEmpty()) {
			throw new ResourceNotFoundException(ErrorConstants.LOGIN_FIRST);
		}
		System.err.println("in findLoggingUser user - >> " + user.get());
		return user;
	}

	@Override
	public UserResponseDto verifyOtpAtRegister(OtpVerificationPayload payload) {
		String token = payload.getToken(); // incoming token from request
		// Extract email from token
		String email = jwtUtils.getSubjectFromToken(token, Token.AUTH_TOKEN);

		if (email == null) {
			throw new ResourceNotFoundException(ErrorConstants.JWT_EMAIL_NOT_FOUND);
		}
		TempreroryUser tempreroryUser = tempreroryUserRepo.findByEmail(email);

		if (!payload.getOtp().equals(tempreroryUser.getOtp())
				&& !OtpType.REGISTRATION.equals(tempreroryUser.getOtptype())) {
			throw new InvalidLoginException(ErrorConstants.INVALID_OTP);
		}

		if (tempreroryUser.getOtpGeneratedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
			throw new InvalidLoginException(ErrorConstants.OTP_EXPIRED);
		}

		Users user = Users.builder().name(tempreroryUser.getName()).email(tempreroryUser.getEmail())
				.password(tempreroryUser.getPassword()).profilePictureUrl(tempreroryUser.getProfilePictureUrl())
				.bio(tempreroryUser.getBio()).type(AccountType.PUBLIC).isDeleted(false).status("ACTIVE")
				.userName(tempreroryUser.getUserName()).role(Role.USER).build();

		Users savedUser = userRepository.save(user);
		tempreroryUserRepo.delete(tempreroryUser);
		if (savedUser == null)
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);
		UserResponseDto responseDto = UserResponseDto.builder().id(savedUser.getId()).username(savedUser.getName())
				.email(savedUser.getEmail()).bio(savedUser.getBio()).role(savedUser.getRole())
				.profilePictureUrl(savedUser.getProfilePictureUrl()).accountType(savedUser.getType()).followersCount(0L)
				.followingCount(0L).build();
		return responseDto;
	}

	@Override
	public List<Users> getAllUser(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		List<Users> userList = userRepository.findAllWithFilter(pageable);
		if (userList.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);
		return userList;
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDto getUserByEmail(String email) {
		Optional<Users> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);
		Users user = userOpt.get();
		long followersCount = user.getFollowers() != null ? user.getFollowers().size() : 0L;
		long followingCount = user.getFollowing() != null ? user.getFollowing().size() : 0L;

		UserResponseDto response = UserResponseDto.builder().id(user.getId()).username(user.getUserName())
				.email(user.getEmail()).name(user.getName()).role(user.getRole())
				.profilePictureUrl(user.getProfilePictureUrl()).bio(user.getBio()).accountType(user.getType())
				.followersCount(followersCount).followingCount(followingCount).build();

		return response;
	}

	@Override
	@Transactional
	public UserResponseDto updateUser(UpdateUser user, MultipartFile file) throws IOException {
		Optional<Users> userLoggedIn = findLoggingUser();

		Users existingUser = userLoggedIn.get();

		// 2️⃣ Copy other fields from DTO
		BeanUtils.copyProperties(user, existingUser, "profilePictureUrl");

		// 3️⃣ Handle profile picture update
		if (file != null && !file.isEmpty()) {
			// 🔹 Delete old image from Cloudinary if exists
			if (existingUser.getProfilePictureUrl() != null && !existingUser.getProfilePictureUrl().isEmpty()) {
				// Extract publicId from URL
				String[] parts = existingUser.getProfilePictureUrl().split("/");
				String publicIdWithExtension = parts[parts.length - 1];
				String publicId = publicIdWithExtension.split("\\.")[0]; // remove extension
				try {
					cloudinary.uploader().destroy(publicId, new HashMap<>());
				} catch (java.io.IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// 🔹 Upload new image using existing uploadImage method
			String newImageUrl = uploadImage(file, existingUser.getId());
			existingUser.setProfilePictureUrl(newImageUrl);
		}

		// 4️⃣ Save updated user
		Users user1 = userRepository.save(existingUser);

		long followersCount = user1.getFollowers() != null ? user1.getFollowers().size() : 0L;
		long followingCount = user1.getFollowing() != null ? user1.getFollowing().size() : 0L;

		UserResponseDto response = UserResponseDto.builder().username(user1.getName()).email(user1.getEmail())
				.role(user1.getRole()).profilePictureUrl(user1.getProfilePictureUrl()).bio(user1.getBio())
				.followersCount(followersCount).followingCount(followingCount).accountType(user1.getType()).build();

		return response;
	}

	@Override
	public UserResponseDto blockUser(String email) {
		Optional<Users> user = userRepository.findByEmail(email);
		if (user.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);
		if (user.get().getStatus().equals("UNBLOCKED")) {
			user.get().setStatus("BLOCKED");
		} else {
			user.get().setStatus("UNBLOCKED");
		}
		Users udtedUser = userRepository.save(user.get());

		long followersCount = udtedUser.getFollowers() != null ? udtedUser.getFollowers().size() : 0L;
		long followingCount = udtedUser.getFollowing() != null ? udtedUser.getFollowing().size() : 0L;

		UserResponseDto response = UserResponseDto.builder().username(udtedUser.getName()).email(udtedUser.getEmail())
				.role(udtedUser.getRole()).profilePictureUrl(udtedUser.getProfilePictureUrl()).bio(udtedUser.getBio())
				.accountType(udtedUser.getType()).followersCount(followersCount).followingCount(followingCount).build();

		return response;
	}

	@Override
	public String uploadImage(MultipartFile file, String userId) throws IOException {
		Optional<Users> user = userRepository.findById(userId);
		if (user.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);

		Map<String, Object> options = new HashMap<>();

		// 🔹 Progressive + Auto-format + Auto-quality
		options.put("transformation", new Transformation().quality("auto").fetchFormat("auto").flags("progressive"));

		Map uploadResult = null;
		try {
			uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
		} catch (java.io.IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String upload = (String) uploadResult.get("secure_url");
		if (upload != null) {
			user.get().setProfilePictureUrl(upload);
			userRepository.save(user.get());
		}
		// return secure URL of optimized, progressive image
		return (String) uploadResult.get("secure_url");
	}

	@Override
	public Map<String, String> forgetPassword(String email) {
		UserResponseDto user = getUserByEmail(email);
		String otp = String.format("%06d", new Random().nextInt(999999));
		emailService.sendOtpEmail(email, otp, OtpType.FORGET_PASSWORD);

		TempreroryUser tempUser = new TempreroryUser();
		BeanUtils.copyProperties(user, tempUser);
		tempUser.setOtp(otp);
		tempUser.setOtpGeneratedAt(LocalDateTime.now());
		tempUser.setOtptype(OtpType.FORGET_PASSWORD);

		String auth_Token = jwtUtils.generateToken(email, Token.AUTH_TOKEN);
		Map<String, String> request = new HashMap<>();
		request.put("otpType", "FORGET_PASSWORD");
		request.put("otp", otp);
		request.put("auth_Token", auth_Token);

		TempreroryUser tempreroryUser = tempreroryUserRepo.findByEmail(email);

		if (tempreroryUser != null)
			tempreroryUserRepo.delete(tempreroryUser);
		tempreroryUserRepo.save(tempUser);

		return request;
	}

	@Override
	public String verifyOtpForForgetPassword(String otp) {
		Optional<Users> user = findLoggingUser();
		TempreroryUser tempUser = tempreroryUserRepo.findByEmail(user.get().getEmail());
		if (!otp.equals(tempUser.getOtp()) && !OtpType.FORGET_PASSWORD.equals(tempUser.getOtptype()))
			throw new InvalidLoginException(ErrorConstants.INVALID_OTP);
		if (tempUser.getOtpGeneratedAt().plusMinutes(5).isBefore(LocalDateTime.now()))
			throw new InvalidLoginException(ErrorConstants.OTP_EXPIRED);

		return "OTP Verifed Successfully !!";
	}

	@Override
	public String changePassword(String password) {
		Optional<Users> user = findLoggingUser();
		user.get().setPassword(passwordEncoder.encode(password));
		Users users = userRepository.save(user.get());
		if (users == null)
			throw new ResourceNotFoundException(ErrorConstants.PASSWORD_NOT_CHANGED);
		return "Password Changed Successfully!!";
	}

	@Override
	public Users findUserById(String userId) {
		Optional<Users> user = userRepository.findById(userId);
		if (user.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);
		return user.get();
	}

	@Override
	public Users findUserByUserName(String userName) {
		Optional<Users> userOpt = userRepository.findByUserName(userName);
		if (userOpt.isEmpty())
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);
		Users user = userOpt.get();
		return user;
	}

	@Override
	@Transactional(readOnly = true)
	public UsersWithPost getMyProfile() {
		// Fetch logged-in user
		Users user = findLoggingUser().orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND));

		// Map posts
		List<PostResponse> postResponses = Collections.emptyList();
		if (user.getPosts() != null) {
			postResponses = user.getPosts().stream()
					.map(post -> PostResponse.builder().id(post.getId()).content(post.getContent())
							.imageUrl(post.getImageUrl()).createdAt(post.getCreatedAt())
							.likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
							.commentsCount(post.getComments() != null ? post.getComments().size() : 0)
							.shareCount(post.getShares() != null ? post.getShares().size() : 0)
							// likedByMe is always true for own posts
							.likedByMe(true).userResponseDto(UserResponseDto.fromEntity(post.getUser())).build())
					.collect(Collectors.toList());
		}

		long followersCount = user.getFollowers() != null ? user.getFollowers().size() : 0L;
		long followingCount = user.getFollowing() != null ? user.getFollowing().size() : 0L;
		long postCount = user.getPosts() != null ? user.getPosts().size() : 0;

		return UsersWithPost.builder().id(user.getId()).username(user.getUserName()).name(user.getName())
				.email(user.getEmail()).profilePictureUrl(user.getProfilePictureUrl()).bio(user.getBio())
				.accountType(user.getType()).followers(followersCount).following(followingCount).isMe(true) // self
				.postCount(postCount) // profile
				.isfollowing(false) // irrelevant for self
				.hasSentFollowingrequest(false) // irrelevant for self
				.posts(postResponses).build();
	}

	@Override
	@Transactional(readOnly = true)
	public UsersWithPost getUsersProfile(String userName) {
		// Fetch target user
		Users user = findUserByUserName(userName);
		if (user == null)
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND);

		// Fetch logged-in user
		Users loggedInUser = findLoggingUser()
				.orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

		boolean isMe = user.getId().equals(loggedInUser.getId());

		// Already following
		boolean isFollowing = loggedInUser.getFollowing() != null
				&& loggedInUser.getFollowing().stream().anyMatch(f -> f.getId().equals(user.getId()));

		// Logged-in user sent a follow request to target (pending)
		boolean hasSentFollowingRequest = user.getFollowRequest() != null
				&& user.getFollowRequest().stream().anyMatch(f -> f.getId().equals(loggedInUser.getId()));

		// Logged-in user received a follow request from target
		boolean isRequestReceiver = loggedInUser.getFollowRequest() != null
				&& loggedInUser.getFollowRequest().stream().anyMatch(f -> f.getId().equals(user.getId()));

		// Can view posts?
		boolean canViewPosts = "PUBLIC".equalsIgnoreCase(user.getType().name()) || isMe || isFollowing;

		long postCount = user.getPosts() != null ? user.getPosts().size() : 0;

		// Map posts
		List<PostResponse> postResponses = Collections.emptyList();
		if (canViewPosts && user.getPosts() != null) {
			postResponses = user.getPosts().stream()
					.map(post -> PostResponse.builder().id(post.getId()).content(post.getContent())
							.imageUrl(post.getImageUrl()).createdAt(post.getCreatedAt())
							.likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
							.commentsCount(post.getComments() != null ? post.getComments().size() : 0)
							.shareCount(post.getShares() != null ? post.getShares().size() : 0)
							.likedByMe(post.getLikes() != null
									&& post.getLikes().stream().anyMatch(l -> l.getId().equals(loggedInUser.getId())))
							.userResponseDto(UserResponseDto.fromEntity(post.getUser())).build())
					.collect(Collectors.toList());
		}

		long followersCount = user.getFollowers() != null ? user.getFollowers().size() : 0L;
		long followingCount = user.getFollowing() != null ? user.getFollowing().size() : 0L;

		return UsersWithPost.builder().id(user.getId()).username(user.getUserName()).name(user.getName())
				.email(user.getEmail()).profilePictureUrl(user.getProfilePictureUrl()).bio(user.getBio())
				.accountType(user.getType()).followers(followersCount).following(followingCount).isMe(isMe)
				.isfollowing(isFollowing).hasSentFollowingrequest(hasSentFollowingRequest)
				.isRequestReceiver(isRequestReceiver).posts(postResponses).postCount(postCount).build();
	}

	public Optional<Users> findUserByRefreshToken(String refreshToken) {
		Optional<Users> user = userRepository.findUserByRefreshToken(refreshToken);
		return user;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponseDto> getSuggestions() {
		Users currentUser = findLoggingUser().orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Set<Users> myFollowing = currentUser.getFollowing();

		if (myFollowing.isEmpty()) {
			return new ArrayList<>();
		}

		List<Object[]> results = userRepository.findSuggestions(currentUser.getId(), myFollowing);

		return results.stream().map((Function<Object[], UserResponseDto>) obj -> {
			Users suggestedUser = (Users) obj[0];
			Long mutualCount = (Long) obj[1];

			return UserResponseDto.builder().id(suggestedUser.getId()).username(suggestedUser.getUserName())
					.email(suggestedUser.getEmail()).profilePictureUrl(suggestedUser.getProfilePictureUrl())
					.mutualCount(mutualCount).build();
		}).collect(Collectors.toList());
	}

	@Override
	public List<UserSearchDto> getUserByUserName(String userName) {
		Optional<Users> currentUserOpt = findLoggingUser(); // your method to fetch current logged-in user
		Users currentUser = currentUserOpt.get();
		return userRepository.searchUsersWithFollowStatus(userName, currentUser, currentUser.getId());
	}

	@Override
	public List<UserResponseDto> getUsersByUserName(String userName) {
		List<Users> userList = userRepository.findAllByUserName(userName);
		if (userList.isEmpty())
			return Collections.emptyList();

		// ✅ Convert entity list to DTO list
		return userList.stream()
				.map((Users user) -> UserResponseDto.builder().id(user.getId()).username(user.getUserName())
						.email(user.getEmail()).profilePictureUrl(user.getProfilePictureUrl()).bio(user.getBio())
						.build())
				.collect(Collectors.toList());
	}

	@Override
	public UserResponseDto getUser(String identifiers) {
		Optional<Users> user = Optional.empty();
		List<Users> users = new ArrayList<>();
		System.err.println("heyyyyyyyyy inside the getUser method----------");
		// ✅ Detect input type
		if (identifiers.matches("^[a-z0-9+_.-]+@(.+)$")) {
			System.err.println("step 1");
			user = userRepository.findByEmail(identifiers);
		} else if (identifiers.contains("_") || Character.isUpperCase(identifiers.charAt(0))) {
			System.err.println("step 2");
			user = userRepository.findByUserName(identifiers);
			System.err.println(user);
		} else {
			System.err.println("step 3");
			users = userRepository.findByName(identifiers);
		}

		Users u = null;

		if (user.isPresent()) {
			u = user.get();
		} else if (!users.isEmpty()) {
			u = users.get(0); // or handle list if multiple users found
		}

		if (u == null) {
			throw new ResourceNotFoundException(ErrorConstants.USER_NOT_FOUND + identifiers);
		}

		// ✅ Use Lombok builder
		return UserResponseDto.builder().id(u.getId()).username(u.getUserName()).name(u.getName()).email(u.getEmail())
				.profilePictureUrl(u.getProfilePictureUrl()).bio(u.getBio()).followersCount(null).followingCount(null)
				.role(u.getRole()).mutualCount(0L) // or calculate if you have// logic
				.accountType(u.getType()).build();
	}

}
