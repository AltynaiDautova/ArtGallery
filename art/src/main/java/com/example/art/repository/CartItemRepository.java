package com.example.art.repository;

import com.example.art.model.Cart;
import com.example.art.model.CartItem;
import com.example.art.model.Painting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndPainting(Cart cart, Painting painting);
    void deleteByCartAndPainting(Cart cart, Painting painting);

    int countByCart(Cart cart);
}