package com.csci318.microservice.cart.Services.Impl;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Entities.Cart;
import com.csci318.microservice.cart.Entities.CartItem;
import com.csci318.microservice.cart.Entities.Relation.Item;
import com.csci318.microservice.cart.Mappers.CartItemMapper;
import com.csci318.microservice.cart.Mappers.CartMapper;
import com.csci318.microservice.cart.Repositories.CartItemRepository;
import com.csci318.microservice.cart.Repositories.CartRepository;
import com.csci318.microservice.cart.Services.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {

    @Value("${item.url.service}")
    private String ITEM_URL;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final RestTemplate restTemplate;

    private final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                           CartMapper cartMapper, CartItemMapper cartItemMapper, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Create a new cart for the user.
     * @param cartDTORequest Request DTO for creating a new cart.
     * @return Response DTO of the created cart.
     */
    @Transactional
    public CartDTOResponse createCart(CartDTORequest cartDTORequest) {
        Cart cart = cartMapper.toEntities(cartDTORequest);
        cart = cartRepository.save(cart);
        return cartMapper.toDtos(cart);
    }

    /**
     * Add an item to the cart based on business rules.
     * @param cartId ID of the cart to which the item will be added.
     * @param cartItemRequest Request DTO containing the item details.
     * @return Response DTO of the updated cart.
     * @throws IllegalArgumentException if the cart does not exist.
     */
    @Transactional
    public CartDTOResponse addItemToCart(UUID cartId, CartItemDTORequest cartItemRequest) {
        Cart cart = this.cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with ID: " + cartId));

        if (cart == null) {
            throw new IllegalArgumentException("Cart not found with ID: " + cartId);
        }
        cart.setRestaurantId(cartItemRequest.getRestaurantId());
        cart = cartRepository.save(cart);

        // If adding an item from a different restaurant, clear the cart (Rule)
        if (cart.getRestaurantId() == null || !cart.getRestaurantId().equals(cartItemRequest.getRestaurantId())) {
            cart.setRestaurantId(cartItemRequest.getRestaurantId());
            cart.setTotalPrice(0.0);
            // Properly clear cart items for the new restaurant
            cartItemRepository.deleteByCartId(cartId);
        }

        Item item = restTemplate.getForObject(ITEM_URL + "/" + cartItemRequest.getItemId(), Item.class);
        // DEBUG
        log.info("Item: " + item);
        log.info("Item price: " + item.getPrice());

        // Check if the item already exists in the cart
        CartItem existingCartItem = cartItemRepository.findByCartIdAndItemId(cartId, item.getId());

        if (existingCartItem != null) {
            // Update the quantity and price of the existing item
            existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItemRequest.getQuantity());
            existingCartItem.setPrice(existingCartItem.getPrice() + item.getPrice());
            cartItemRepository.save(existingCartItem);
        } else {
            // Add new item to the cart
            CartItem newCartItem = cartItemMapper.toEntities(cartItemRequest);
            newCartItem.setCartId(cartId);
            newCartItem.setPrice(item.getPrice() * cartItemRequest.getQuantity());
            newCartItem.setOrderId(null); // Order ID is null as the item is not ordered yet
            newCartItem.setQuantity(cartItemRequest.getQuantity());
            cartItemRepository.save(newCartItem);
        }

        // Recalculate total price
        double totalPrice = cartItemRepository.findByCartId(cartId).stream()
                .mapToDouble(cartItem -> cartItem.getPrice() != null ? cartItem.getPrice() : 0.0)
                .sum();
        cart.setTotalPrice(totalPrice);

        // Save updated cart

        return cartMapper.toDtos(cart);
    }
}
