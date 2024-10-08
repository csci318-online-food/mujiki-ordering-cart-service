package com.csci318.microservice.cart.Services.Impl;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.Domain.Entities.Cart;
import com.csci318.microservice.cart.Domain.Entities.CartItem;
import com.csci318.microservice.cart.Domain.Relations.*;
import com.csci318.microservice.cart.Domain.Services.CartPriceCalculator;
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

import java.sql.Timestamp;
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

    @Value("${promotion.url.service}")
    private String PROMOTION_URL;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final RestTemplate restTemplate;
    private final CartPriceCalculator cartPriceCalculator;

    private final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                           CartMapper cartMapper, CartItemMapper cartItemMapper, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.restTemplate = restTemplate;
        this.cartPriceCalculator = new CartPriceCalculator();
    }

    /**
     * Create a new cart for the user   .
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

    @Override
    @Transactional
    public CartDTOResponse addItemToCart(UUID cartId, CartItemDTORequest cartItemRequest) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with ID: " + cartId));

        // Handle different restaurant rule
        cart.handleDifferentRestaurant(cartItemRequest.getRestaurantId(), () -> {
            cartItemRepository.deleteByCartId(cartId);
            cartPriceCalculator.calculateTotalPrice(cart, null);
        });

        Item item = restTemplate.getForObject(ITEM_URL + "/" + cartItemRequest.getItemId(), Item.class);

        // Check if the item already exists in the cart
        CartItem existingCartItem = cartItemRepository.findByCartIdAndItemId(cartId, item.getId());

        if (existingCartItem != null) {
            // Update the quantity and price in CartItem entity
            existingCartItem.increaseQuantity(item.getPrice());
            cartItemRepository.save(existingCartItem);
        } else {
            // Add new item to the cart
            CartItem newCartItem = CartItem.createNew(cartId, item);
            cartItemRepository.save(newCartItem);
        }

        // Recalculate total price in the Cart entity
        cartPriceCalculator.calculateTotalPrice(cart, cartItemRepository.findByCartId(cartId));
        cartRepository.save(cart);

        return cartMapper.toDtos(cart);
    }

    /*
     * Workflow to create an order from the cart.
     * Do the payment with the current cart total price.
     * Create an order with the cart items if the payment is successful (Balance >= total price).
     * If not successful, throw an exception.
     * cartId in cartItem will be set to null and orderId will be set to the order ID.
     */

    public Order createOrder(UUID cartId, UUID paymentId, UUID promotionId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with ID: " + cartId));
        double totalPrice = cart.getTotalPrice();

        // Apply Promotion after having total price from cart (TEST)
        if (promotionId != null) {
            try {
                // Check the promotion id is valid
                Promotion promotion = restTemplate.getForObject(PROMOTION_URL + "/" + promotionId, Promotion.class);
                if (promotion.isActive() && promotion.getExpiryDate().after(new Timestamp(System.currentTimeMillis())) && promotion.getStock() > 0) {
                    double discountAmount = promotion.getPercentage();
                    totalPrice = totalPrice - discountAmount;
                    cart.setTotalPrice(totalPrice);
                    this.cartRepository.save(cart); // update the cart with the new total price
                    restTemplate.put(PROMOTION_URL + "/apply/" + promotionId, Promotion.class);
                } else {
                    throw new RuntimeException("Promotion is expired, out of stock, or inactive");
                }
            } catch (RestClientException e) {
                log.error("Error occurred while communicating with the promotion service", e);
                throw new RuntimeException("Error occurred while communicating with the promotion service: " + e.getMessage());
            }
        }

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
                        restTemplate.postForObject(ORDER_URL + "/" + order.getId() + "/add-order-item", orderItem, OrderItem.class);

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
