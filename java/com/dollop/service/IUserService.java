package com.dollop.service;

import java.util.List;
import java.util.Map;

import com.dollop.dto.ForgetPasswordDTO;
import com.dollop.dto.UserDTO;
import com.dollop.entity.Users;

public interface IUserService {

	public Map<String, String> login(String password, String email);

	public Map<String, Object> registerWithOtp(UserDTO userDto);

	public Users forgetPassword(ForgetPasswordDTO forgetPass);

	public Users getUserByEmail(String email);

	public Users getUser();

	public long getUserId();

	public List<Users> getAllUsers();

	public Users updateUser(Users user);
}
