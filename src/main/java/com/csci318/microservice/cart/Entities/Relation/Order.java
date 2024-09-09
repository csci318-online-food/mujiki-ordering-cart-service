package com.csci318.microservice.cart.Entities.Relation;

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
public class Order {

    private UUID id;
    private UUID userId;
    private UUID restaurantId;
    private Double totalPrice;
    private String status; // "CONFIRMED", "CANCELLED", "COMPLETED"
    private Timestamp orderTime;
}