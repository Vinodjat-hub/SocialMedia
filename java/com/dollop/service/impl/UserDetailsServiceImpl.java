package com.dollop.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dollop.entity.CustomUserDetails;
import com.dollop.entity.Users;
import com.dollop.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	    Users user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

	    return new CustomUserDetails(user); // must wrap Users entity
	}


}
