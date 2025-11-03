package com.dollop.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
	
    private String status;
    private String message;
    private String token;
    private String tempToken;
}
