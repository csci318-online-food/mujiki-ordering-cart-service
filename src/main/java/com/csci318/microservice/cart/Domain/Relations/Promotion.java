package com.csci318.microservice.cart.Domain.Relations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Promotion {
    private UUID id;
    private UUID restaurantId;
    private String code;
    private String description;
    private int percentage;
    private Timestamp expiryDate;
    private boolean isActive;
    private int stock;
}
