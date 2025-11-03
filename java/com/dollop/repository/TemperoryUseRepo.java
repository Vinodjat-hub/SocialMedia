package com.dollop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dollop.entity.TemperoryUser;
import com.dollop.entity.Users;

public interface TemperoryUseRepo extends JpaRepository<TemperoryUser, Long> {

    // Corrected method names to match entity fields
    Optional<TemperoryUser> getUserByEmail(String email);

    Optional<TemperoryUser> getUserByTempToken(String tempToken);

	Users findByEmail(String email);
}
