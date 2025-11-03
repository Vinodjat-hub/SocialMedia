package com.dollop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dollop.entity.Notification;
import com.dollop.repository.NotificationRepository;
import com.dollop.service.impl.NotificationService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private NotificationService notificationService ;

	@GetMapping("/{userId}")
	public List<Notification> getUserNotifications(@PathVariable Long userId) {
		return notificationService.getUserNotifications(userId);
	}

	@DeleteMapping("/{userId}")
	@Transactional
	public ResponseEntity<Void> clearNotifications(@PathVariable Long userId) {
		notificationRepository.deleteByUserId(userId);
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/{id}/read")
	@Transactional
	public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
		notificationService.markAsRead(id);
	    return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/seen")
	@Transactional
	public ResponseEntity<Void> markAsSeen(@PathVariable Long id) {
	    notificationRepository.findById(id).ifPresent(n -> n.setSeen(true));
	    return ResponseEntity.noContent().build();
	}

	@PutMapping("/user/{userId}/seen")
	@Transactional
	public ResponseEntity<Void> markAllAsSeen(@PathVariable Long userId) {
	    notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
	        .forEach(n -> n.setSeen(true));
	    return ResponseEntity.noContent().build();
	}

	@GetMapping("/{userId}/unseen-count")
	public Long getUnseenCount(@PathVariable Long userId) {
	    return notificationRepository.countByUserIdAndSeenFalse(userId);
	}

}
