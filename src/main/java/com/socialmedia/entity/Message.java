package com.socialmedia.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "messages")
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private String content;
	private LocalDateTime timestamp;

	@ManyToOne
	@JoinColumn(name = "sender_id")
	private Users sender;

	@ManyToOne
	@JoinColumn(name = "receiver_id")
	private Users receiver;
}
