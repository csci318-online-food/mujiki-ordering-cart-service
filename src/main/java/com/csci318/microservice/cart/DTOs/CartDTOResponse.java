package com.csci318.microservice.cart.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTOResponse {

    private UUID id;

    private UUID userId;

    private UUID restaurantId;

    private Double totalPrice;

}
