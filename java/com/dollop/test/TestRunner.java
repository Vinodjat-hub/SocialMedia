package com.dollop.test;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.dollop.enm.Role;
import com.dollop.entity.Users;
import com.dollop.repository.UserRepository;

@Component
public class TestRunner implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		List<Users> users = userRepository.findByRoleAndIsDeletedOrderByEmail(Role.ADMIN, false);

		if (users == null || users.isEmpty()) {
			Users user1 = Users.builder().email("vinodjat8818@gmail.com").isDeleted(false)
					.password(passwordEncoder.encode("vinod@123")).name("Vinod Jat").mobileNumber("8818847240").status("ACTIVE").role(Role.ADMIN)
					.build();

			userRepository.save(user1);
		}
	}

}