package com.dollop.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dollop.entity.Order;
import com.dollop.entity.Product;
import com.dollop.entity.Users;
import com.dollop.service.impl.UserServiceImpl;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class UserController {

	@Autowired
	private UserServiceImpl userServ;

	@GetMapping("/get/{email}")
	public ResponseEntity<Users> getUserByEmail(@PathVariable String email) {
		return new ResponseEntity<Users>(userServ.getUserByEmail(email), HttpStatus.OK);
	}

	@GetMapping("/getAll")
	public ResponseEntity<List<Users>> getAllUsers() {
		return new ResponseEntity<List<Users>>(userServ.getAllUsers(), HttpStatus.OK);
	}
	
	@GetMapping("/getAllUsers")
	public ResponseEntity<Page<Users>> getAllUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size, @RequestParam(required = false) String name,
			@RequestParam(required = false) String sort, @RequestParam(required = false) LocalDateTime date) {

		Page<Users> users = userServ.getUsers(page, size, name, date);
		return ResponseEntity.ok(users);
	}

	@GetMapping("/profile")
	public ResponseEntity<Users> getUser() {
		return new ResponseEntity<Users>(userServ.getUser(), HttpStatus.OK);
	}

	@PutMapping("/updateUser")
	public ResponseEntity<Users> updateUser(@RequestBody Users user) {
		return new ResponseEntity<Users>(userServ.updateUser(user), HttpStatus.OK);
	}

	@PutMapping("/updateUsers/{userId}/{status}")
	public ResponseEntity<Users> updateUserStatus(@PathVariable Long userId, @PathVariable String status) {
		return new ResponseEntity<Users>(userServ.updateUserStatus(userId, status), HttpStatus.OK);
	}

	@GetMapping("/getAllCount")
	public ResponseEntity<Map<String, Long>> getAllCount(
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
	    
	    return new ResponseEntity<>(userServ.getAllCounts(startDate, endDate), HttpStatus.OK);
	}


	@GetMapping("/last7days")
	public List<Map<String, Object>> getUsersFromLast7Days() {
		return userServ.getLastSevenDaysUsers();
	}

}
