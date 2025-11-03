package com.dollop.entity;

import java.time.LocalDateTime;

import com.dollop.enm.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TemperoryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;
    private String email;
    private String password;
    private String mobileNumber;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime created_at;
    private String status;

    private String tempToken;
    private String otp;
    private LocalDateTime otpGeneratedAt;
    private LocalDateTime otpExpiryAt;

    // ✅ Add safe defaults
    private Boolean isVerified = false;
    private Boolean isDeleted = false;
}
