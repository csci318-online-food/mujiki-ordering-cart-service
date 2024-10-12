package com.csci318.microservice.cart.Domain.Services;

import java.util.List;

import com.csci318.microservice.cart.Domain.Entities.Cart;
import com.csci318.microservice.cart.Domain.Entities.CartItem;
import com.csci318.microservice.cart.Domain.Relations.Promotion;

import io.micrometer.common.lang.Nullable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CartPriceCalculator {
    // Update the total price of the cart based on all items
    public void calculateTotalPrice(Cart cart, List<CartItem> cartItems, @Nullable Promotion promotion) {
        double total = 0.0;

        if (cartItems != null) {
            for (CartItem cartItem : cartItems) {
                if (cartItem.getPrice() != null) {
                    total += cartItem.getPrice();
                }
            }
        }

        if (promotion != null) {
            double discounted = total * promotion.getPercentage() / 100.0;
            total -= discounted;
        }

        cart.setTotalPrice(total);
    }
}
