package com.socialmedia.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socialmedia.num.AccountType;
import com.socialmedia.num.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class Users extends Audit {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Size(min = 3, max = 50)
	@Column(unique = true)
	@NotBlank(message = "UserName must not be blank")
	private String userName;
	@Size(min = 3, max = 50)
	private String name;
	@Column(unique = true)
	@Email
	private String email;
	private String password;
	private String profilePictureUrl;
	@Size(min = 10, max = 100)
	private String bio;
	private Role role;
	private AccountType type;

	private String status;
	private boolean isDeleted;

	// Followers
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_followers", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "follower_id"))
	@Builder.Default
	@JsonIgnore
	private Set<Users> followers = new HashSet<>();

	// Following
	@ManyToMany(mappedBy = "followers")
	@Builder.Default
	@JsonIgnore
	private Set<Users> following = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Builder.Default
	@JsonIgnore
	@JoinTable(name = "user_follow_request", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "requester_id"))
	private Set<Users> followRequest = new HashSet<>();

	// Posts by this user
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	private Set<Post> posts = new HashSet<>();

	// Messages sent
	@OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	private Set<Message> sentMessages = new HashSet<>();

	// Messages received
	@OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	private Set<Message> receivedMessages = new HashSet<>();

	// Refresh tokens for the user
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	@JsonIgnore
	private Set<RefreshToken> refreshTokens = new HashSet<>();

	// ✅ Safe custom toString() (no lazy fields)
	@Override
	public String toString() {
		return "Users{" + "id=" + id + ", username='" + userName + '\'' + ", email='" + email + '\'' + ", role=" + role
				+ '}';
	}
}
