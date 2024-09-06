package com.csci318.microservice.cart.Utils;

import com.csci318.microservice.cart.Repositories.CartRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DBInitalizer implements CommandLineRunner {

    private final CartRepository cartRepository;

    public DBInitalizer(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // if (cartRepository.count() == 0) {
        // }
    }
}
