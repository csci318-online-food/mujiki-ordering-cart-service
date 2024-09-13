package com.csci318.microservice.cart.Entities;

import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Entities.Relation.Item;
import com.csci318.microservice.cart.Repositories.CartItemRepository;
import com.csci318.microservice.cart.Repositories.CartRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
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

    // Update the total price of the cart based on all items
    public void calculateTotalPrice(CartItemRepository cartItemRepository) {
        double total = 0.0;
        List<CartItem> cartItems = cartItemRepository.findByCartId(this.id);

        for (CartItem cartItem : cartItems) {
            if (cartItem.getPrice() != null) {
                total += cartItem.getPrice();
            }
        }

        this.totalPrice = total;
    }

    // Handle a case where an item from a different restaurant is added
    public void handleDifferentRestaurant(UUID newRestaurantId, CartItemRepository cartItemRepository) {
        if (this.restaurantId == null || !this.restaurantId.equals(newRestaurantId)) {
            this.restaurantId = newRestaurantId;
            clearCartItems(cartItemRepository);
        }
    }

    // Clear cart items when switching restaurants
    public void clearCartItems(CartItemRepository cartItemRepository) {
        cartItemRepository.deleteByCartId(this.id);
        this.totalPrice = 0.0;
    }
}
