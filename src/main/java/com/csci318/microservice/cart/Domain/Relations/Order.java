package com.csci318.microservice.cart.Domain.Relations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import com.csci318.microservice.cart.Constants.OrderStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {
    private UUID id;
    private UUID userId;
    private UUID restaurantId;
    private Double totalPrice;
    private OrderStatus status;
    private LocalDateTime orderTime;
}
