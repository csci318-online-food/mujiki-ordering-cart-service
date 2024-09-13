package com.csci318.microservice.cart.Services.Impl;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Entities.Cart;
import com.csci318.microservice.cart.Entities.CartItem;
import com.csci318.microservice.cart.Entities.Relation.Item;
import com.csci318.microservice.cart.Entities.Relation.Order;
import com.csci318.microservice.cart.Entities.Relation.OrderItem;
import com.csci318.microservice.cart.Entities.Relation.Payment;
import com.csci318.microservice.cart.Exceptions.ControllerExceptionHandler.DataAccessException;
import com.csci318.microservice.cart.Mappers.CartItemMapper;
import com.csci318.microservice.cart.Mappers.CartMapper;
import com.csci318.microservice.cart.Repositories.CartItemRepository;
import com.csci318.microservice.cart.Repositories.CartRepository;
import com.csci318.microservice.cart.Services.CartService;
import org.hibernate.ObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {

    @Value("${item.url.service}")
    private String ITEM_URL;


    @Value("${payment.url.service}")
    private String PAYMENT_URL;

    @Value("${order.url.service}")
    private String ORDER_URL;

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
    public Cart addItemToCart(UUID cartId, CartItem cartItemRequest) {
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
            existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
            existingCartItem.setPrice(existingCartItem.getPrice() + item.getPrice());
            cartItemRepository.save(existingCartItem);
        } else {
            // Add new item to the cart
            CartItem newCartItem = new CartItem();
            newCartItem.setCartId(cartId);
            newCartItem.setQuantity(newCartItem.getQuantity() + 1);
            newCartItem.setPrice(item.getPrice() * newCartItem.getQuantity());
            cartItemRepository.save(newCartItem);
        }

        // Recalculate total price
        double totalPrice = cartItemRepository.findByCartId(cartId).stream()
                .mapToDouble(cartItem -> cartItem.getPrice() != null ? cartItem.getPrice() : 0.0)
                .sum();
        cart.setTotalPrice(totalPrice);

        return cart;
    }

    /*
     * Workflow to create an order from the cart.
     * Do the payment with the current cart total price.
     * Create an order with the cart items if the payment is successful (Balance >= total price).
     * If not successful, throw an exception.
     * cartId in cartItem will be set to null and orderId will be set to the order ID.
     */

    public Order createOrder(UUID cartId, UUID paymentId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with ID: " + cartId));
        double totalPrice = cart.getTotalPrice();

        try {
            List<Payment> payments = getAllPaymentsFromUser(cart.getUserId());

            Payment payment = payments.stream()
                    .filter(p -> p.getId().equals(paymentId))
                    .findFirst()
                    .orElseThrow(() -> new ObjectNotFoundException(Payment.class, "Payment not found with ID: " + paymentId));

            if (payment.getBalance() >= totalPrice) {
                // Create an order
                Order order = new Order();
                order.setId(UUID.randomUUID());
                order.setUserId(cart.getUserId());
                order.setRestaurantId(cart.getRestaurantId());
                order.setTotalPrice(totalPrice);
                order.setOrderTime(LocalDateTime.now());
                order.setStatus("CONFIRMED");

                try {
                    // Attempt to create the order via the order service
                    this.restTemplate.postForObject(ORDER_URL + "/create-order", order, Order.class);
                    log.info("Order created: " + order);
                } catch (HttpClientErrorException.Forbidden e) {
                    log.error("Access to the order service is forbidden", e);
                    throw new RuntimeException("Access to the order service is forbidden: " + e.getMessage());
                } catch (HttpServerErrorException.InternalServerError e) {
                    log.error("Internal server error occurred while creating order", e);
                    throw new RuntimeException("Internal server error occurred while creating order: " + e.getMessage());
                } catch (RestClientException e) {
                    log.error("Error occurred while communicating with the order service", e);
                    throw new RuntimeException("Error occurred while communicating with the order service: " + e.getMessage());
                }

                // Update cart information
                try {
                    cart.setTotalPrice(0.0);
                    cart.setRestaurantId(null);
                    cartRepository.save(cart);
                } catch (DataAccessException e) {
                    log.error("Failed to update cart in the database", e);
                    throw new RuntimeException("Failed to update cart: " + e.getMessage());
                }

                // Update the payment balance
                try {
                    payment.setBalance(payment.getBalance() - totalPrice);
                    updatePaymentBalance(payment, paymentId);
                    log.info("Payment balance updated successfully");
                } catch (HttpClientErrorException.Forbidden e) {
                    log.error("Access to the payment service is forbidden", e);
                    throw new RuntimeException("Access to the payment service is forbidden: " + e.getMessage());
                } catch (HttpServerErrorException.InternalServerError e) {
                    log.error("Internal server error occurred while updating payment balance. Status Code: " + e.getStatusCode() +
                            ", Response Body: " + e.getResponseBodyAsString(), e);
                    throw new RuntimeException("Internal server error occurred while updating payment balance: " + e.getResponseBodyAsString());
                } catch (RestClientException e) {
                    log.error("Error occurred while communicating with the payment service", e);
                    throw new RuntimeException("Error occurred while communicating with the payment service: " + e.getMessage());
                }

                // Process cart items to order
                try {
                    List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
                    List<OrderItem> orderItems = new ArrayList<>();

                    for (CartItem cartItem : cartItems) {
                        // Process cart item to order item
                        OrderItem orderItem = new OrderItem();
                        orderItem.setId(UUID.randomUUID());
                        orderItem.setOrderId(order.getId());
                        orderItem.setRestaurantId(cartItem.getRestaurantId());
                        orderItem.setItemId(cartItem.getItemId());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItem.setPrice(cartItem.getPrice());

                        // Add to order items list
                        orderItems.add(orderItem);

                        // Save order item
                        restTemplate.postForObject(ORDER_URL + "/create-order-item", orderItem, OrderItem.class);

                        // Delete cart item
                        cartItemRepository.delete(cartItem);
                    }
                } catch (DataAccessException e) {
                    log.error("Failed to process cart items to order", e);
                    throw new RuntimeException("Failed to process cart items to order: " + e.getMessage());
                }
                try {
                // Retrieve the order from the order service
                    Order orderReturned = restTemplate.getForObject(ORDER_URL + "/" + order.getId(), Order.class);
                    log.info("Order returned: {}", orderReturned);
                    return order;
                } catch (HttpClientErrorException.Forbidden e) {
                    log.error("Access to the order service is forbidden", e);
                    throw new RuntimeException("Access to the order service is forbidden: " + e.getMessage());
                } catch (HttpServerErrorException.InternalServerError e) {
                    log.error("Internal server error occurred while retrieving order", e);
                    throw new RuntimeException("Internal server error occurred while retrieving order: " + e.getMessage());
                } catch (RestClientException e) {
                    log.error("Error occurred while communicating with the order service", e);
                    throw new RuntimeException("Error occurred while communicating with the order service: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("Payment failed: insufficient balance");
            }
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Access to the payment service is forbidden", e);
            throw new RuntimeException("Access to the payment service is forbidden: " + e.getMessage());
        } catch (IllegalArgumentException | ObjectNotFoundException e) {
            log.error("Validation error: " + e.getMessage(), e);
            throw new RuntimeException("Validation error: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error occurred while communicating with external services", e);
            throw new RuntimeException("Error occurred while communicating with external services: " + e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred", e);
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }



    public List<Payment> getAllPaymentsFromUser(UUID userId) {
        String url = PAYMENT_URL + "/user/" + userId;
        ResponseEntity<List<Payment>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    public Payment updatePaymentBalance(Payment paymentDTORequest, UUID paymentId) {
        String url = PAYMENT_URL + "/update-balance/" + paymentId;
        HttpEntity<Payment> requestEntity = new HttpEntity<>(paymentDTORequest);
        try {
            ResponseEntity<Payment> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Payment.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Error occurred while updating payment balance", e);
            throw new RuntimeException("Error occurred while updating payment balance: " + e.getMessage(), e);
        }
    }

}
