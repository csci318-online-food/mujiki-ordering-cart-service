package com.csci318.microservice.cart.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cart")
public class Cart {
    /*/
     * Rules of the cart:
     * Cart can add items and update price
     * If add any item from another restaurant, the cart quantity and price will be clear and update that item from new restaurant in
     * If Cart is process to ordered, the cart quantity and price will be clear
     * And then cart-item will be process to ordered and save to order table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId; // User ID reference

    @Column(name = "restaurant_id")
    private UUID restaurantId; // Restaurant ID reference

    @Column(name = "total_price")
    private Double totalPrice = 0.0;

    @Column(name = "create_at")
    private Timestamp createAt;

    @Column(name = "modify_at")
    private Timestamp modifyAt;

    @Column(name = "modify_by", length = 64)
    private String modifyBy;

    @Column(name = "create_by", length = 64)
    private String createBy;

    // Add item to cart and update total price
    public void addItem(CartItem item) {
        if (restaurantId == null || restaurantId.equals(item.getRestaurantId())) {
            restaurantId = item.getRestaurantId(); // Set restaurant if not already set
            totalPrice += item.getPrice() * item.getQuantity();
        } else {
            // Clear cart if the restaurant is different
            clearCart();
            restaurantId = item.getRestaurantId();
            totalPrice = item.getPrice() * item.getQuantity();
        }
    }

    // Clear the cart
    public void clearCart() {
        restaurantId = null;
        totalPrice = 0.0;
    }
}
