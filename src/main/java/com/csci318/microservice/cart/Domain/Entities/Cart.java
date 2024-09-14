package com.csci318.microservice.cart.Domain.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // Handle a case where an item from a different restaurant is added
    public void handleDifferentRestaurant(UUID newRestaurantId, Runnable clearCart) {
        if (this.restaurantId == null || !this.restaurantId.equals(newRestaurantId)) {
            this.restaurantId = newRestaurantId;
            clearCart.run();
        }
    }
}
