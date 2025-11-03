package com.socialmedia.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialmedia.entity.TempreroryUser;

public interface TempreroryUserRepo extends JpaRepository<TempreroryUser, String> {

	TempreroryUser findByEmail(String email); // ✅ Correct capitalization

}
