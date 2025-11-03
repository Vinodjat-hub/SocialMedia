package com.dollop.controller;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.dollop.dto.OrderPlacedMessage;
import com.dollop.dto.OrderStatusMessage;
import com.dollop.dto.OrderStockMessage;
import com.dollop.entity.Notification;
import com.dollop.repository.NotificationRepository;

@RestController
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    public NotificationController(SimpMessagingTemplate messagingTemplate,
                                  NotificationRepository notificationRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
    }

 // 🔹 Order placed events
    @KafkaListener(topics = "order-placed-topic", groupId = "order-group")
    public void listenOrderPlaced(OrderPlacedMessage message) {
        System.err.println("🎉 Received order-placed: " + message);

        String topic = "/topic/order-placed-user-" + message.getUserId();

        Map<String, Object> payload = Map.of(
            "message", "Order placed with ID: " + message.getOrderId(),
            "orderId", message.getOrderId(),
            "type", "ORDER_PLACED"
        );        

        messagingTemplate.convertAndSend(topic, payload);

        // ✅ Pass orderId, productId as well
        saveNotification(message.getUserId(), (String) payload.get("message"), "ORDER_PLACED", message.getOrderId(), null);
    }

    // 🔹 Order status updates
    @KafkaListener(topics = "order-status-topic", groupId = "order-group")
    public void listenOrderStatus(OrderStatusMessage message) {
        System.err.println("🎉 Received order-status: " + message);

        String topic = "/topic/order-status-user-" + message.getUserId();

        Map<String, Object> payload = Map.of(
            "message", "Order #" + message.getOrderId() + " is now " + message.getStatus(),
            "orderId", message.getOrderId(),
            "type", "ORDER_STATUS"
        );
        messagingTemplate.convertAndSend(topic, payload);

        saveNotification(message.getUserId(), (String) payload.get("message"), "ORDER_STATUS", message.getOrderId(), null);
    }

    // 🔹 Product stock alerts
    @KafkaListener(topics = "product-stock-topic", groupId = "product-group")
    public void listenProductStock(OrderStockMessage message) {
        System.err.println("🎉 Received product-stock: " + message);

        String topic = "/topic/product-stock-user-" + message.getUserId();

        Map<String, Object> payload = Map.of(
            "message", "Product #" + message.getProductId() + " is low in stock: " + message.getStock(),
            "productId", message.getProductId(),
            "stock", message.getStock(),
            "type", "PRODUCT_STOCK"
        );
        messagingTemplate.convertAndSend(topic, payload);

        saveNotification(message.getUserId(), (String) payload.get("message"), "PRODUCT_STOCK", null, message.getProductId());
    }


 // 🔹 Generic notification sender (for future use if needed)
    public void sendNotification(Long userId, String type, String message, Long orderId, Long productId) {
        String topic = "/topic/" + type.toLowerCase() + "-user-" + userId;

        Map<String, Object> payload = Map.of(
                "message", message,
                "type", type,
                "orderId", orderId,
                "productId", productId
        );

        messagingTemplate.convertAndSend(topic, payload);
        System.out.println("📣 [" + type + "] sent to user " + userId + ": " + message);

        // ✅ Save with orderId/productId
        saveNotification(userId, message, type, orderId, productId);
    }


    // 🔹 Persist notification in DB
    private void saveNotification(Long userId, String message, String type, Long orderId, Long productId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setOrderId(orderId);     // save if available
        notification.setProductId(productId); // save if available
        notificationRepository.save(notification);
    }

}
