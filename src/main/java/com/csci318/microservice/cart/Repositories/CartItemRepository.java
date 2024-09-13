package com.csci318.microservice.cart.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.csci318.microservice.cart.Domain.Entities.CartItem;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cartId = :cartId")
    void deleteByCartId(@Param("cartId") UUID cartId);


    @Query("SELECT c FROM CartItem c WHERE c.cartId = :cartId AND c.itemId = :itemId")
    CartItem findByCartIdAndItemId(UUID cartId, UUID itemId);

    @Query("SELECT c FROM CartItem c WHERE c.cartId = :cartId")
    List<CartItem> findByCartId(UUID cartId);
}
