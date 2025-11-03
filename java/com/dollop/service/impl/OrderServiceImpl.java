package com.dollop.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.dollop.dto.OrderDTO;
import com.dollop.dto.OrderItemDTO;
import com.dollop.dto.OrderPlacedMessage;
import com.dollop.dto.OrderStatusMessage;
import com.dollop.dto.OrderStockMessage;
import com.dollop.enm.Role;
import com.dollop.enm.Status;
import com.dollop.entity.Order;
import com.dollop.entity.OrderItem;
import com.dollop.entity.Product;
import com.dollop.entity.Users;
import com.dollop.exception.ResourceNotFoundException;
import com.dollop.repository.OrderItemRepository;
import com.dollop.repository.OrderRepository;
import com.dollop.repository.ProductRepository;
import com.dollop.repository.UserRepository;
import com.dollop.service.IOrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements IOrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserServiceImpl userServiceImpl;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private KafkaTemplate<String, OrderStatusMessage> kafkaTemplate;

	@Autowired
	private KafkaTemplate<String, OrderStockMessage> kafkaTemplate1;

	@Autowired
	private KafkaTemplate<String, OrderPlacedMessage> kafkaTemplate2;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Override
	@Transactional
	public Order placeOrder(OrderDTO orderDTO) {

		Optional<Users> admin = userRepository.findAll().stream().filter(a -> Role.ADMIN.equals(a.getRole()))
				.findFirst();
		Long userId = userServiceImpl.getUserId();

		Order order = new Order();
		order.setOrderDate(LocalDateTime.now());
		order.setStatus(Status.PLACED);
		order.setTotalAmount(orderDTO.getTotalAmount());

		Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		order.setUser(user);

		List<OrderItem> items = new ArrayList<>();

		for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
			Product product = productRepository.findById(itemDTO.getProductId())
					.orElseThrow(() -> new RuntimeException("Product not found"));

			OrderItem orderItem = new OrderItem();
			orderItem.setProduct(product);
			orderItem.setQuantity(itemDTO.getQuantity());

			orderItem.setPrice(
					product.getProductPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())).doubleValue());

			orderItem.setOrder(order);
			items.add(orderItem);

			int newStock = product.getProductStock() - itemDTO.getQuantity().intValue();

			if (newStock < 0) {
				throw new ResourceNotFoundException("Insufficient stock for product: " + product.getProductName());
			}
			if (newStock <= 5) {

				OrderStockMessage orderStockMessage = new OrderStockMessage(product.getProductId(), newStock,
						admin.get().getUserId());

				kafkaTemplate1.send("product-stock-topic", orderStockMessage);
			}
			product.setProductStock(newStock);
		}

		order.setOrderItems(items);
		Order o = orderRepository.save(order);
		if (o != null) {
			OrderPlacedMessage orderPlacedMessage = new OrderPlacedMessage(o.getOrderId(), admin.get().getUserId());
			kafkaTemplate2.send("order-placed-topic", orderPlacedMessage);
		}
		return o;
	}

	@Override
	public Page<OrderDTO> getMyOrders(int page, int size, String status) {
		Long userId = userServiceImpl.getUserId();

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));

		Page<Order> ordersPage;

		if (status != null && !status.isEmpty()) {
			// Filter by userId + status
			ordersPage = orderRepository.findByUser_UserIdAndStatus(userId, Status.valueOf(status.toUpperCase()),
					pageable);
		} else {
			// Filter only by userId
			ordersPage = orderRepository.findByUser_UserId(userId, pageable);
		}

		if (ordersPage.isEmpty()) {
			throw new ResourceNotFoundException("There are no orders yet!!");
		}

		// Convert Orders to OrderDTOs
		List<OrderDTO> orderDTOs = ordersPage.getContent().stream().map(order -> {
			List<OrderItemDTO> itemDTOs = orderItemRepository.findAllByOrder_OrderId(order.getOrderId()).stream()
					.map(item -> new OrderItemDTO(order.getOrderId(), item.getProduct().getProductId(),
							item.getProduct().getProductName(), item.getQuantity(), item.getPrice().longValue()))
					.toList();

			return new OrderDTO(order.getOrderId(), order.getUser().getUserId(), order.getTotalAmount(),
					order.getOrderDate(), order.getStatus(), itemDTOs);
		}).toList();

		return new PageImpl<>(orderDTOs, pageable, ordersPage.getTotalElements());
	}

	@Override
	public List<OrderDTO> getAllOrders() {
		List<Order> orders = orderRepository.findAllDesc();
		if (orders.isEmpty())
			throw new ResourceNotFoundException("There is yet no orders!!");
		List<OrderDTO> orderDTOs = orders.stream().map(order -> {
			// Fetch all items for this order
			List<OrderItemDTO> itemDTOs = orderItemRepository.findAllByOrder_OrderId(order.getOrderId()).stream()
					.map(item -> new OrderItemDTO(order.getOrderId(), item.getProduct().getProductId(),
							item.getProduct().getProductName(), item.getQuantity(), item.getPrice().longValue()))
					.toList();

			return new OrderDTO(order.getOrderId(), order.getUser().getUserId(), order.getTotalAmount(),
					order.getOrderDate(), order.getStatus(), itemDTOs);
		}).toList();

		return orderDTOs;
	}

	@Override
	public Order getOrderByOrderId(Long id) {
		Optional<Order> orders = orderRepository.findById(id);
		if (orders.isEmpty())
			throw new ResourceNotFoundException("There is yet no orders!!");
		return orders.get();
	}

	@Override
	public Order updateOrderStatus(Status status, Long orderId, Long userId) {
		Order existingOrder = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("There is no order on this id : " + orderId));

		if (Status.CANCELLED.equals(status)) {
			existingOrder.setStatus(status);
			System.err.println("existingOrder : " + existingOrder);
			Order order = orderRepository.save(existingOrder);

			if (order != null) {
				for (OrderItem itmes : existingOrder.getOrderItems()) {
					Product product = itmes.getProduct();
					Long quantity = itmes.getQuantity();
					product.setProductStock(product.getProductStock() + quantity.intValue());
					productRepository.save(product);
				}
			}
			List<Users> allUsers = userRepository.findAll();
			Optional<Users> adminOpt = allUsers.stream().filter(user -> Role.ADMIN.equals(user.getRole())).findFirst();
			System.err.println("on cancel admin is : " + adminOpt);
			OrderStatusMessage orderStatusMessage = new OrderStatusMessage(orderId, status.toString(),
					adminOpt.get().getUserId());
			if (adminOpt.isPresent())
				kafkaTemplate.send("order-status-topic", orderStatusMessage);

			return order;

		}
		OrderStatusMessage orderStatusMessage = new OrderStatusMessage(orderId, status.toString(), userId);
		kafkaTemplate.send("order-status-topic", orderStatusMessage);
		// Send real-time notification
		String text = "Order #" + orderId + " is now " + status;
		String topic = "/topic/order-status-user-" + userId;
		messagingTemplate.convertAndSend(topic, text);

		// Save in DB for offline users
//        saveNotification(message.getUserId(), text, "ORDER_STATUS");
		existingOrder.setStatus(status);
		System.err.println("existingOrder : " + existingOrder);
		return orderRepository.save(existingOrder);
	}

//	@Override
//	public Double getCompaniesRevenue(LocalDate startDate, LocalDate endDate) {
//		System.err.println("startDate : "+startDate);
//		System.err.println("endDate : "+endDate);
//		List<Order> orders = orderRepository.findAll();
//		Double totalRevenue = 0.0;
//		if(startDate != null && endDate == null || startDate == null && endDate != null) {
//			if(startDate != null) {
//				totalRevenue = orders.stream().filter(order -> order.getStatus() == Status.DELIVERED && order.getOrderDate().toLocalDate().equals(startDate))
//						.mapToDouble(Order::getTotalAmount).sum();
//			}
//			if(endDate != null) {
//				totalRevenue = orders.stream().filter(order -> order.getStatus() == Status.DELIVERED && order.getOrderDate().toLocalDate().equals(endDate))
//						.mapToDouble(Order::getTotalAmount).sum();
//			}
//			
//		}
//		if (startDate != null && endDate != null) {
//		    totalRevenue = orderRepository.findAll().stream()
//		        .filter(order -> order.getStatus() == Status.DELIVERED
//		            && !order.getOrderDate().toLocalDate().isBefore(startDate)  // >= startDate
//		            && !order.getOrderDate().toLocalDate().isAfter(endDate))    // <= endDate
//		        .mapToDouble(Order::getTotalAmount)
//		        .sum();
//		}
//
//		if(startDate == null && endDate == null) {
//			totalRevenue = orderRepository.findAll().stream().filter(order -> order.getStatus() == Status.DELIVERED)
//					.mapToDouble(Order::getTotalAmount).sum();
//		}
//		System.err.println("***totalRevenue****"+totalRevenue);
//		return totalRevenue;
//	}

	@Override
	public Double getCompaniesRevenue(LocalDate startDate, LocalDate endDate) {
		LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
		LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

		Double totalRevenue = orderRepository.calculateRevenue(startDateTime, endDateTime);

		return totalRevenue;
	}

	public Map<String, Double> getLastSixMonthsRevenueByMonth() {
		LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

		List<Object[]> results = orderRepository.getMonthlyRevenue(sixMonthsAgo);
		Map<String, Double> revenueByMonth = new LinkedHashMap<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

		for (Object[] row : results) {
			Integer month = (Integer) row[0];
			Integer year = (Integer) row[1];
			Number total = (Number) row[2];

			YearMonth yearMonth = YearMonth.of(year, month);
			String key = yearMonth.format(formatter);

			revenueByMonth.put(key, total.doubleValue());
		}
		return revenueByMonth;
	}

//	public List<OrderDTO> findByStatus(Status status) {
//		List<Order> list = orderRepository.findByStatus(status);
//		if (list.isEmpty())
//			throw new ResourceNotFoundException("Order's not found!!");
//
//		List<OrderDTO> orderDTOs = list.stream().map(order -> {
//			// Fetch all items for this order
//			List<OrderItemDTO> itemDTOs = orderItemRepository.findAllByOrder_OrderId(order.getOrderId()).stream()
//					.map(item -> new OrderItemDTO(order.getOrderId(), item.getProduct().getProductId(),
//							item.getProduct().getProductName(), item.getQuantity(), item.getPrice().longValue()))
//					.toList();
//
//			return new OrderDTO(order.getOrderId(), order.getUser().getUserId(), order.getTotalAmount(),
//					order.getOrderDate(), order.getStatus(), itemDTOs);
//		}).toList();
//
//		return orderDTOs;
//	}

	public Page<OrderDTO> getAllOrdersWithPagination(int page, int size, String sort) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
		Page<Order> orderPage;

		if (sort != null && !sort.isEmpty()) {
			Status status = Status.valueOf(sort.toUpperCase());
			orderPage = orderRepository.findByStatus(status, pageable);
		} else {
			orderPage = orderRepository.findAll(pageable);
		}

		// Map Orders to OrderDTOs
		List<OrderDTO> dtoList = orderPage.getContent().stream().map(order -> {
			OrderDTO dto = new OrderDTO();
			dto.setOrderId(order.getOrderId());
			dto.setUserId(order.getUser().getUserId());
			dto.setTotalAmount(order.getTotalAmount());
			dto.setOrderDate(order.getOrderDate());
			dto.setStatus(order.getStatus());
			dto.setOrderItems(
					order.getOrderItems().stream()
							.map(oi -> new OrderItemDTO(oi.getOrder().getOrderId(), oi.getProduct().getProductId(),
									oi.getProduct().getProductName(), oi.getQuantity(), oi.getPrice().longValue()))
							.toList());
			return dto;
		}).toList();

		// Return as Page<OrderDTO>
		return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
	}

}
