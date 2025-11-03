package com.dollop.repository;

import org.springframework.data.domain.Pageable; // ✅ correct
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dollop.dto.OrderDTO;
import com.dollop.enm.Status;
import com.dollop.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("SELECT o FROM Order o WHERE o.user.userId = :userId ORDER BY o.orderId DESC")
	Page<Order> findByUser_UserId(Long userId, Pageable pageable);

	@Query("SELECT FUNCTION('MONTH', o.orderDate), FUNCTION('YEAR', o.orderDate), SUM(o.totalAmount) " + "FROM Order o "
			+ "WHERE o.orderDate >= :fromDate AND o.status = 'DELIVERED' "
			+ "GROUP BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate) "
			+ "ORDER BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate) ")
	List<Object[]> getMonthlyRevenue(@Param("fromDate") LocalDateTime fromDate);

	@Query("SELECT o FROM Order o ORDER BY o.orderId DESC")
	List<Order> findAllDesc();

	// Paginated query for all orders
	Page<Order> findAll(Pageable pageable);

	// Paginated query by status
	Page<Order> findByStatus(Status status, Pageable pageable);

	Page<Order> findByUser_UserIdAndStatus(Long userId, Status valueOf, Pageable pageable);

	@Query("""
			    SELECT COALESCE(SUM(o.totalAmount), 0)
			    FROM Order o
			    WHERE o.status = 'DELIVERED'
			      AND (:startDate IS NULL OR o.orderDate >= :startDate)
			      AND (:endDate IS NULL OR o.orderDate <= :endDate)
			""")
	Double calculateRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
