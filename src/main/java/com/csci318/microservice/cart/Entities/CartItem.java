package com.csci318.microservice.cart.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cart_id")
    private UUID cartId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private Double price; // Price of the item at the time of adding to the cart

    @Column(name = "create_at")
    private Timestamp createAt;

    @Column(name = "modify_at")
    private Timestamp modifyAt;

    @Column(name = "modify_by", length = 64)
    private String modifyBy;

    @Column(name = "create_by", length = 64)
    private String createBy;

    // Update quantity when adding the same item
    public void updateQuantity(int additionalQuantity) {
        this.quantity += additionalQuantity;
    }

    // Logic to transition from cart to order context
    public void processToOrder(UUID orderId) {
        this.cartId = null; // Clear cart reference
        this.orderId = orderId; // Set order reference
    }
}
