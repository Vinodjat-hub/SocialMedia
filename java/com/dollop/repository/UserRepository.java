package com.dollop.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dollop.entity.Users;
import com.dollop.enm.Role;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

	Optional<Users> findByEmail(String email);

	List<Users> findByRoleAndIsDeletedOrderByEmail(Role role, Boolean isDeleted);

	boolean existsByEmail(String email);

	@Query(value = "SELECT u.user_id, u.name, u.email, u.created_at " + "FROM users u "
			+ "WHERE u.created_at >= CURRENT_DATE - INTERVAL 7 DAY", nativeQuery = true)
	List<Object[]> getAllUsersOfLastSevenDays();

	Page<Users> findByNameContainingIgnoreCaseAndRole(String name, Role role, Pageable pageable);

	Page<Users> findByCreatedAtAndRole(LocalDateTime createdAt, Role role, Pageable pageable);

	Page<Users> findByRole(Role role, Pageable pageable);

}
