package com.dollop.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dollop.entity.Notification;
import com.dollop.repository.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository repo;

	public Notification saveNotification(Long userId, String message, String type) {
		Notification notif = new Notification();
		notif.setUserId(userId);
		notif.setMessage(message);
		notif.setType(type);
		return repo.save(notif);
	}

	public List<Notification> getUserNotifications(Long userId) {
		return repo.findByUserIdOrderByCreatedAtDesc(userId);
	}

	public void markAsSeen(Long id) {
		Notification notif = repo.findById(id).orElseThrow();
		notif.setSeen(true);
		repo.save(notif);
	}
	
	public void markAsRead( Long id) {
		repo.findById(id).ifPresent(n -> n.setSeen(true));
	}

}
