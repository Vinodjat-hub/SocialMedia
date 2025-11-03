package com.dollop.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.dollop.enm.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "orders")  // ✅ Avoid reserved keyword
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"user", "orderItems"})  // 🚀 Prevent recursion
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // ✅ Foreign key to Users
    @JsonBackReference
    private Users user;
    
    private LocalDateTime orderDate;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)  // ✅ Store enum as String in DB
    private Status status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<OrderItem> orderItems;  // ✅ Relationship fixed
}
