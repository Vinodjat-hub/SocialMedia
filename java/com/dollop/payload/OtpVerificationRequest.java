package com.dollop.payload;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String tempToken;
    private String otp;
    private String type;
}
