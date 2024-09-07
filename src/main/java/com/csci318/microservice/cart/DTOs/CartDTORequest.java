package com.csci318.microservice.cart.DTOs;

import java.util.UUID;

public class CartDTORequest {
    private UUID userId;
    private Double totalPrice;
    private String status;
}
