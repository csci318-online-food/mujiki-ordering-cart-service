package com.csci318.microservice.cart.Entities;

import jakarta.persistence.*;
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
@Entity
@Table(name = "cart")
public class Cart {
    /*/
     * Rules of the cart:
     * Cart can add items and update price
     * If add any item from another restaurant, the cart quantity and price will be clear and update that item from new restaurant in
     * If Cart is process to ordered, the cart quantity and price will be clear
     * And then cart-item will be process to ordered and save to order table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "total_price")
    private Double totalPrice;


    @Column(name = "is-ordered")
    private boolean isOrdered = false;

    @Column(name = "create_at")
    private Timestamp createAt;

    @Column(name = "modify_at")
    private Timestamp modifyAt;

    @Column(name = "modify_by")
    private String modifyBy;

    @Column(name = "create_by")
    private String createBy;
}
