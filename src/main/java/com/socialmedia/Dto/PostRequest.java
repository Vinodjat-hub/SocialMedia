package com.socialmedia.Dto;

import com.socialmedia.annotations.Trimmed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Trimmed
public class PostRequest {

	private String content;
}
