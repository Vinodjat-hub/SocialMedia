package com.dollop.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.dollop.dto.OrderDTO;
import com.dollop.enm.Status;
import com.dollop.entity.Order;

public interface IOrderService {

	public Order placeOrder(OrderDTO orderDTo);

	public Page<OrderDTO> getMyOrders(int page, int size, String sort);

	public List<OrderDTO> getAllOrders();

	public Order getOrderByOrderId(Long id);

	public Order updateOrderStatus(Status status, Long orderId, Long userId);

	public Double getCompaniesRevenue(LocalDate startDate,LocalDate endDate);
}
