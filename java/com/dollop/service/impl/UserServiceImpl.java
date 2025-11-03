package com.dollop.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dollop.dto.ForgetPasswordDTO;
import com.dollop.dto.UserDTO;
import com.dollop.enm.Role;
import com.dollop.enm.Status;
import com.dollop.entity.Order;
import com.dollop.entity.Product;
import com.dollop.entity.TemperoryUser;
import com.dollop.entity.Users;
import com.dollop.exception.DuplicateEntryExecption;
import com.dollop.exception.InvalidLoginException;
import com.dollop.exception.ResourceNotFoundException;
import com.dollop.exception.UserBlockedException;
import com.dollop.payload.ApiResponse;
import com.dollop.payload.OtpVerificationRequest;
import com.dollop.repository.OrderRepository;
import com.dollop.repository.ProductRepository;
import com.dollop.repository.TemperoryUseRepo;
import com.dollop.repository.UserRepository;
import com.dollop.service.IUserService;
import com.dollop.util.JwtUtils;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	private AuthenticationManager authenticationManager; // ✅ Use Spring's real AuthenticationManager

	@Autowired
	private JwtUtils jwtUtil;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private TemperoryUseRepo temperoryUseRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	OrderRepository orderRepository;

	@Override
	public Map<String, Object> registerWithOtp(UserDTO userDto) {
		if (userRepo.findByEmail(userDto.getEmail()).isPresent()) {
			throw new DuplicateEntryExecption("User already exists with this email. Please use another.");
		}

		String encodedPassword = passwordEncoder.encode(userDto.getPassword());
		userDto.setPassword(encodedPassword);
		userDto.setStatus("INACTIVE");

		TemperoryUser tempUser = new TemperoryUser();
		BeanUtils.copyProperties(userDto, tempUser);

		String otp = String.format("%06d", new Random().nextInt(999999));
		tempUser.setOtp(otp);
		tempUser.setOtpGeneratedAt(LocalDateTime.now());
		tempUser.setOtpExpiryAt(LocalDateTime.now().plusMinutes(10));
		tempUser.setRole(Role.CUSTOMER);
		tempUser.setMobileNumber(userDto.getMobile());
		tempUser.setCreated_at(LocalDateTime.now());
		String tempToken = UUID.randomUUID().toString();
		tempUser.setTempToken(tempToken);

		temperoryUseRepo.getUserByEmail(tempUser.getEmail()).ifPresent(u -> temperoryUseRepo.deleteById(u.getUserId()));

		temperoryUseRepo.save(tempUser);
		emailService.sendOtpEmail(tempUser.getEmail(), otp);
		Map<String, Object> response = new HashMap<>();
		response.put("tempToken", tempToken);
		response.put("otp", otp);
		return response;
	}

	@Override
	public Map<String, String> login(String password, String email) {

		Users user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if ("BLOCKED".equals(user.getStatus())) {
			throw new UserBlockedException("You are blocked and not authorized to login!!!");
		}
		try {
			@SuppressWarnings("unused")
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(email, password));

			String otp = String.format("%06d", new Random().nextInt(999999));
			LocalDateTime otpGeneratedAt = LocalDateTime.now();

			user.setOtp(otp);
			user.setOtpGeneratedAt(otpGeneratedAt);
			userRepo.save(user);

			String tokenType = "otpToken";
			String tempToken = jwtUtil.generateToken(email,tokenType);
			Map<String, String> response = new HashMap<>();
			response.put("status", "OTP_SENT");
			response.put("tempToken", tempToken);
			response.put("otp", otp);
			response.put("name", user.getName());
			response.put("role", user.getRole().name());
			response.put("userId", user.getUserId().toString());
			return response;

		} catch (Exception ex) {
			throw new InvalidLoginException("Invalid email or password");
		}
	}

	public ApiResponse verifyOtp(OtpVerificationRequest request) {
		String email = jwtUtil.getEmailFromTempToken(request.getTempToken());
		if (email == null) {
			throw new ResourceNotFoundException("Invalid or expired token");
		}
		String storedOtp = null;
		LocalDateTime otpGeneratedAt = null;
		Users user = new Users();
		if (request.getType().equals("login")) {
			user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
			storedOtp = user.getOtp();
			otpGeneratedAt = user.getOtpGeneratedAt();
		}
		if (request.getType().equals("register")) {
			user = temperoryUseRepo.findByEmail(email);
			if (user == null)
				throw new ResourceNotFoundException("User not found");
			storedOtp = user.getOtp();
			otpGeneratedAt = user.getOtpGeneratedAt();
			userRepo.save(user);
			temperoryUseRepo.deleteById(user.getUserId());
		}

		if (otpGeneratedAt.plusMinutes(5).isBefore(LocalDateTime.now())) {
			throw new RuntimeException("OTP expired");
		}

		if (!storedOtp.equals(request.getOtp())) {
			throw new ResourceNotFoundException("Invalid OTP");
		}

		String tokenType = "loginToken";
		String token = jwtUtil.generateToken(user.getEmail(),tokenType);
		return new ApiResponse("SUCCESS", "OTP verified", token, null);
	}

	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<Users> user = userRepo.findByEmail(email); // ✅ load by email
		if (user.isEmpty()) {
			throw new UsernameNotFoundException("User not found with email: " + email);
		}

		return new org.springframework.security.core.userdetails.User(user.get().getEmail(), user.get().getPassword(),
				new ArrayList<>()); // you can load roles here too
	}

	@Override
	public Users forgetPassword(ForgetPasswordDTO forgetPass) {
		Optional<Users> user = userRepo.findByEmail(forgetPass.getEmail());
		if (user.isEmpty()) {
			throw new ResourceNotFoundException("User not available on this email.pls try another email");
		}
		user.get().setPassword(passwordEncoder.encode(forgetPass.getPassword()));
		Users users = userRepo.save(user.get());
		return users;
	}

	@Override
	public Users getUserByEmail(String email) {
		Optional<Users> user = userRepo.findByEmail(email);
		if (user.isEmpty())
			throw new ResourceNotFoundException("user not available on this email.pls use another email!!");
		return user.get();
	}

	@Override
	public long getUserId() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		Users user = userRepo.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		return user.getUserId();
	}

	@Override
	public List<Users> getAllUsers() {
		List<Users> userList = userRepo.findAll().stream().filter(user -> !Role.ADMIN.equals(user.getRole())).collect(Collectors.toList());
		System.err.println(userList);
		if (userList.isEmpty())
			throw new ResourceNotFoundException("User's not availabel");
		return userList;
	}

	@Override
	public Users getUser() {
		Long id = getUserId();
		Optional<Users> user = userRepo.findById(id);
		if (user.isEmpty())
			throw new ResourceNotFoundException("User not available");
		return user.get();
	}

	public Page<Users> getUsers(int page, int size, String name, LocalDateTime date) {
	    Pageable pageable = PageRequest.of(page, size);

	    if (name != null && !name.isEmpty()) {
	        // Fetch only customers by name
	        return userRepo.findByNameContainingIgnoreCaseAndRole(name, Role.CUSTOMER, pageable);
	    }

	    if (date != null) {
	        // Fetch only customers by creation date
	        return userRepo.findByCreatedAtAndRole(date, Role.CUSTOMER, pageable);
	    }

	    // Fetch all customers
	    return userRepo.findByRole(Role.CUSTOMER, pageable);
	}


	@Override
	public Users updateUser(Users user) {
		Long id = getUserId();
		Optional<Users> users = userRepo.findById(id);
		if (users.isEmpty())
			throw new ResourceNotFoundException("User not available");
		Optional<Users> user1 = userRepo.findByEmail(user.getEmail());
		if (user1.isPresent() && !user1.get().getEmail().equals(users.get().getEmail())) {
			throw new DuplicateEntryExecption("Please enter another email.This email is already used!!");
		}
		users.get().setName(user.getName());
		users.get().setEmail(user.getEmail());
		users.get().setMobileNumber(user.getMobileNumber());
		users.get().setPassword(passwordEncoder.encode(user.getPassword()));
		user = userRepo.save(users.get());
		return user;
	}

	public Map<String, Long> getAllCounts(LocalDate startDate, LocalDate endDate) {

		long userCount = 0;
		long productCount = 0;
		long orderCount = 0;
		List<Users> userList = userRepo.findAll();
		List<Order> orderList = orderRepository.findAll();
		Map<Status, Long> statusCounts = null;

		Stream<Users> userStream = userRepo.findAll().stream()
	            .filter(u -> Role.CUSTOMER.equals(u.getRole()));

	    List<Order> orderStream = orderRepository.findAll();
		productCount = productRepository.findAll().stream().filter(p -> p.getProductId() != null).count();

		if (startDate != null && endDate == null || startDate == null && endDate != null) {
			if (startDate != null) {
				userCount = userStream.filter(u -> u.getCreatedAt().toLocalDate().equals(startDate)).count();
				orderCount = orderStream.stream().filter(o -> o.getOrderDate().toLocalDate().equals(startDate)).count();
				statusCounts = orderRepository.findAll().stream().filter(o -> o.getOrderDate().toLocalDate().equals(startDate)).collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
				
			}
			if (endDate != null) {
				userCount = userStream.filter(u -> u.getCreatedAt().toLocalDate().equals(endDate)).count();
				orderCount = orderStream.stream().filter(o -> o.getOrderDate().toLocalDate().equals(endDate)).count();
				statusCounts = orderRepository.findAll().stream().filter(o -> o.getOrderDate().toLocalDate().equals(endDate)).collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
			}
			
		}

		if (startDate != null && endDate != null) {
		    userCount = userStream
		            .filter(u -> {
		                LocalDate created = u.getCreatedAt().toLocalDate();
		                return (created.isEqual(startDate) || created.isAfter(startDate)) &&
		                       (created.isEqual(endDate) || created.isBefore(endDate));
		            })
		            .count();

		    List<Order> filteredOrders = orderRepository.findAll().stream()
		            .filter(o -> {
		                LocalDate orderDate = o.getOrderDate().toLocalDate();
		                return (orderDate.isEqual(startDate) || orderDate.isAfter(startDate)) &&
		                       (orderDate.isEqual(endDate) || orderDate.isBefore(endDate));
		            })
		            .toList();

		    orderCount = filteredOrders.size();

		    statusCounts = filteredOrders.stream()
		            .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
		}


		if (startDate == null && endDate == null) {
			userCount = userRepo.findAll().stream().filter(u -> Role.CUSTOMER.equals(u.getRole())).count();
			orderList = orderRepository.findAll();
			orderCount = orderList.stream().filter(o -> o.getOrderId() != null).count();

			statusCounts = orderList.stream().collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
		}

		long orderWithPlaced = statusCounts.getOrDefault(Status.PLACED, 0L);
		long orderWithPacked = statusCounts.getOrDefault(Status.PACKED, 0L);
		long orderWithShipped = statusCounts.getOrDefault(Status.SHIPPED, 0L);
		long orderWithDelivered = statusCounts.getOrDefault(Status.DELIVERED, 0L);
		long orderWithCancelled = statusCounts.getOrDefault(Status.CANCELLED, 0L);

		Map<String, Long> response = new HashMap<>();
		response.put("userCount", userCount);
		response.put("productCount", productCount);
		response.put("orderCount", orderCount);
		response.put("orderWithPlaced", orderWithPlaced);
		response.put("orderWithPacked", orderWithPacked);
		response.put("orderWithShipped", orderWithShipped);
		response.put("orderWithDelivered", orderWithDelivered);
		response.put("orderWithCancelled", orderWithCancelled);
		System.err.println(response);
		return response;
	}

	public Users updateUserStatus(Long userId, String status) {
		Optional<Users> user = userRepo.findById(userId);
		if (user.isEmpty())
			throw new ResourceNotFoundException("User not available");
		user.get().setStatus(status);
		Users users = userRepo.save(user.get());
		return users;
	}

	public List<Map<String, Object>> getLastSevenDaysUsers() {
		List<Object[]> rows = userRepo.getAllUsersOfLastSevenDays();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] row : rows) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", row[0]);
			map.put("username", row[1]);
			map.put("email", row[2]);
			map.put("createdAt", row[3]);
			result.add(map);
		}
		return result;
	}

}
