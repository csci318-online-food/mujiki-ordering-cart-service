package com.csci318.microservice.cart.DTOs;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTORequest {

    private UUID userId; // The ID of the user owning the cart
//    private UUID restaurantId; // ID of the restaurant
//    private Double totalPrice; //  Price can be managed by the service
}

