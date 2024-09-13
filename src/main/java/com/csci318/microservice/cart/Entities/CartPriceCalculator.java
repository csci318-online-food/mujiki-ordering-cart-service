package com.csci318.microservice.cart.Entities;

import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CartPriceCalculator {
    // Update the total price of the cart based on all items
    public void calculateTotalPrice(Cart cart, List<CartItem> cartItems) {
        double total = 0.0;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getPrice() != null) {
                total += cartItem.getPrice();
            }
        }

        cart.setTotalPrice(total);
    }
}
