package com.socialmedia.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.socialmedia.Dto.ChatMessage;

@Controller
public class ChatController {

//	private final SimpMessagingTemplate messagingTemplate;
//
//	public ChatController(SimpMessagingTemplate messagingTemplate) {
//		this.messagingTemplate = messagingTemplate;
//	}
//
//	// When a client sends a message to /app/chat.send
//	@MessageMapping("/chat.send")
//	public void send(ChatMessage message) {
//		message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
//
//		// Send to specific user topic
//		messagingTemplate.convertAndSend("/topic/" + message.getReceiver(), message);
//	}
//
//	// Optional: broadcast welcome messages
//	@MessageMapping("/chat.broadcast")
//	@SendTo("/topic/public")
//	public ChatMessage broadcast(ChatMessage message) {
//		message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
//		return message;
//	}
}
