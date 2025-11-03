package com.socialmedia.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
@ToString(exclude = { "likes", "comments", "shares" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@EqualsAndHashCode.Include
	private String id;

	private String content;
	private String imageUrl;
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	@Builder.Default
	private Set<Users> likes = new HashSet<>();

	@OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<Comment> comments = new HashSet<>();

	@OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<PostShare> shares = new HashSet<>();

	// Optional convenience method
	public int getUniqueShareCount() {
		return (int) shares.stream().map(share -> share.getSharedTo().getId()).distinct().count();
	}
}
