package com.csci318.microservice.cart.Entities.Relation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Item {
    private UUID id;
    private String name;
    private UUID restaurantId;
    private String description;
    private double price;
    private boolean availability;
}
