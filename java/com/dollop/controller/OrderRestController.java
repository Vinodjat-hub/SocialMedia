package com.dollop.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dollop.dto.OrderDTO;
import com.dollop.enm.Status;
import com.dollop.entity.Order;
import com.dollop.entity.Users;
import com.dollop.service.impl.InvoiceService;
import com.dollop.service.impl.OrderServiceImpl;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/api/order")
public class OrderRestController {

	@Autowired
	private OrderServiceImpl orderServiceImpl;
	@Autowired
	private InvoiceService invoiceService;

	@PostMapping("/placeOrder")
	public ResponseEntity<Order> placeOrder(@RequestBody OrderDTO orderDTO) {
		System.err.println("orderDTO <---> " + orderDTO);
		return new ResponseEntity<Order>(orderServiceImpl.placeOrder(orderDTO), HttpStatus.OK);
	}

	@GetMapping("/myOrder")
	public ResponseEntity<Page<OrderDTO>> getMyOrder(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, @RequestParam(required = false) String status
			) {
		Page<OrderDTO> order = orderServiceImpl.getMyOrders(page, size, status);
		return new ResponseEntity<Page<OrderDTO>>(order, HttpStatus.OK);
	}

//	@GetMapping("/AllOrder")
//	public ResponseEntity<List<OrderDTO>> getAllOrder() {
//		List<OrderDTO> list = orderServiceImpl.getAllOrders();
//		return new ResponseEntity<List<OrderDTO>>(list, HttpStatus.OK);
//	}
	
	@GetMapping("/AllOrder")
	public ResponseEntity<Page<OrderDTO>> getAllOrderByPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, @RequestParam(required = false) String status
			) {
		Page<OrderDTO> list = orderServiceImpl.getAllOrdersWithPagination(page, size, status);
		return new ResponseEntity<Page<OrderDTO>>(list, HttpStatus.OK);
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
		Order order = orderServiceImpl.getOrderByOrderId(id);
		return new ResponseEntity<Order>(order, HttpStatus.OK);
	}

	@PutMapping("/update/{orderId}/{status}/{userId}")
	public ResponseEntity<Order> updateOrderStatus(@PathVariable Status status, @PathVariable Long orderId,
			@PathVariable Long userId) {
		return new ResponseEntity<Order>(orderServiceImpl.updateOrderStatus(status, orderId, userId), HttpStatus.OK);
	}

	@GetMapping("/totalRevenue")
	public ResponseEntity<Double> getAllRevenue( @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		Double totalRevenue = orderServiceImpl.getCompaniesRevenue(startDate, endDate);
		System.err.println("totalRevenue ========== "+totalRevenue);
		return new ResponseEntity<Double>(totalRevenue, HttpStatus.OK);
	}

	@GetMapping("/monthlyRevenue")
	public ResponseEntity<Map<String, Double>> getRevenueMonthly() {
		Map<String, Double> lastSixMonthRevenue = orderServiceImpl.getLastSixMonthsRevenueByMonth();
		System.err.println("lastSixMonthRevenue -------------------- " + lastSixMonthRevenue);
		return new ResponseEntity<Map<String, Double>>(lastSixMonthRevenue, HttpStatus.OK);
	}

	@GetMapping("/invoice/{orderId}/{userId}")
	public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId, @PathVariable Long userId) {
		Order order = orderServiceImpl.getOrderByOrderId(orderId);
		byte[] pdfBytes = invoiceService.generateInvoice(order, userId);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
	}
//
//	@GetMapping("/getByStatus/{status}")
//	public ResponseEntity<List<OrderDTO>> getAllByStatus(@PathVariable Status status) {
//		return new ResponseEntity<List<OrderDTO>>(orderServiceImpl.findByStatus(status), HttpStatus.OK);
//	}

}
