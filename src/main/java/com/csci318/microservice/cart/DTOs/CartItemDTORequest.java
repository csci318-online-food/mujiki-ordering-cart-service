package com.csci318.microservice.cart.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTORequest {

    // Use in path
//    private UUID cartId; // ID of the cart (can be null if it's a new cart)

    private UUID restaurantId; // Restaurant ID associated with the item

    private UUID orderId; // Order ID associated with the item

    private UUID itemId; // The ID of the item being added to the cart

    private int quantity; // Quantity of the item being added

    private Double price; // Price of the item
}
