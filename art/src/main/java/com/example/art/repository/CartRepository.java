package com.example.art.repository;

import com.example.art.model.Cart;
import com.example.art.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndIsActive(User user, Boolean isActive);
}
