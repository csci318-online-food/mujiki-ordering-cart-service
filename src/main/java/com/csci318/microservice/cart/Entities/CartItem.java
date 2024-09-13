package com.csci318.microservice.cart.Entities;

import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Entities.Relation.Item;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

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

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private Double price; // Price of the item at the time of adding to the cart

    // Update quantity when adding the same item
    public void increaseQuantity(double itemPrice) {
        this.quantity += 1;
        this.price += itemPrice;
    }

    public static CartItem createNew(UUID cartId, Item item) {
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);
        cartItem.setItemId(item.getId());
        cartItem.setQuantity(1);
        cartItem.setPrice(item.getPrice());
        return cartItem;
    }
}
