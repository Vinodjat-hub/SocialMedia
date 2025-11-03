package com.socialmedia.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.socialmedia.entity.Users;
import com.socialmedia.repo.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Users user = userRepository.getUserByEmail(email);
		System.err.println("user =========== "+user);
		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
				List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
	}

}
