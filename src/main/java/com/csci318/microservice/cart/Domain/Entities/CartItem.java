package com.csci318.microservice.cart.Domain.Entities;

import com.csci318.microservice.cart.Domain.Relations.Item;
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
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cart_id")
    private UUID cartId;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private Double price; // Price of the item at the time of adding to the cart

    public void increaseQuantity(double itemPrice) {
        this.quantity += 1;
        this.price += itemPrice;
    }

    // Create a new CartItem instance for a new item
    public static CartItem createNew(UUID cartId, Item item) {
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);
        cartItem.setItemId(item.getId());
        cartItem.setRestaurantId(item.getRestaurantId());
        cartItem.setQuantity(1);
        cartItem.setPrice(item.getPrice());
        return cartItem;
    }
}
