package com.csci318.microservice.cart.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTOResponse {

    private UUID cartItemId;

    private UUID cartId;

    private UUID restaurantId;

    private UUID itemId;

    private int quantity;

    private Double price;
}

