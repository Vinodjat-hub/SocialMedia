package com.socialmedia.Dto;

import com.socialmedia.annotations.Trimmed;
import com.socialmedia.entity.Users;
import com.socialmedia.num.AccountType;
import com.socialmedia.num.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Trimmed
public class UserResponseDto {

	private String id;
	private String username;
	private String name;
	private String email;
	private String profilePictureUrl;
	private String bio;
	private Long followersCount;
	private Long followingCount;
	private Role role;
	private Long mutualCount;

	private AccountType accountType;

	public static UserResponseDto fromEntity(Users user) {
		if (user == null)
			return null;

		return UserResponseDto.builder().id(user.getId()).username(user.getUserName())
				.profilePictureUrl(user.getProfilePictureUrl()).build();
	}
}
