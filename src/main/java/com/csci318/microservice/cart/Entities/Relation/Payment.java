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
public class Payment {
    private UUID id;
    private UUID userId;
    private String holderName;
    private String cardNumber;
    private Double balance;
}
