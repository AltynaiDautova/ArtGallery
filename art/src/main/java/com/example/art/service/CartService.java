package com.example.art.service;

import com.example.art.model.*;
import com.example.art.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaintingRepository paintingRepository;
    private final SaleRepository saleRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       PaintingRepository paintingRepository,
                       SaleRepository saleRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.paintingRepository = paintingRepository;
        this.saleRepository = saleRepository;
    }

    @Transactional
    public boolean addToCart(User user, Long paintingId) {
        // Получаем или создаем активную корзину пользователя
        Cart cart = cartRepository.findByUserAndIsActive(user, true)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setActive(true);
                    return cartRepository.save(newCart);
                });

        // Находим картину
        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(() -> new EntityNotFoundException("Картина не найдена"));

        // Проверяем доступность картины
        if (painting.getStatus() != Painting.PaintingStatus.AVAILABLE) {
            throw new IllegalStateException("Картина '" + painting.getTitle() + "' недоступна для покупки");
        }

        // Проверяем, не добавлена ли уже картина в корзину
        if (cartItemRepository.findByCartAndPainting(cart, painting).isPresent()) {
            return false;
        }

        // Добавляем картину в корзину
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setPainting(painting);
        cartItemRepository.save(item);

        return true;
    }

    public List<Painting> getUserCartItems(User user) {
        return cartRepository.findByUserAndIsActive(user, true)
                .map(cart -> cartItemRepository.findByCart(cart).stream()
                        .map(CartItem::getPainting)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public void removeFromCart(User user, Long paintingId) {
        Cart cart = cartRepository.findByUserAndIsActive(user, true)
                .orElseThrow(() -> new EntityNotFoundException("Корзина не найдена"));

        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(() -> new EntityNotFoundException("Картина не найдена"));

        cartItemRepository.deleteByCartAndPainting(cart, painting);
    }

    public int getCartItemCount(User user) {
        return cartRepository.findByUserAndIsActive(user, true)
                .map(cart -> cartItemRepository.countByCart(cart))
                .orElse(0);
    }

    @Transactional
    public double processCheckout(User user, List<Long> selectedItemIds) {
        Cart cart = cartRepository.findByUserAndIsActive(user, true)
                .orElseThrow(() -> new EntityNotFoundException("Корзина не найдена"));

        List<Painting> selectedPaintings = paintingRepository.findAllById(selectedItemIds);

        // Проверяем, что все выбранные картины найдены
        if (selectedPaintings.size() != selectedItemIds.size()) {
            throw new RuntimeException("Некоторые картины не найдены");
        }

        double total = 0;

        for (Painting painting : selectedPaintings) {
            // Проверяем доступность картины
            if (painting.getStatus() != Painting.PaintingStatus.AVAILABLE) {
                throw new IllegalStateException(
                        "Картина '" + painting.getTitle() + "' недоступна для покупки");
            }

            // Обновляем статус картины
            painting.setStatus(Painting.PaintingStatus.SOLD);
            paintingRepository.save(painting);

            // Создаем запись о продаже
            Sale sale = new Sale();
            sale.setUser(user);
            sale.setPainting(painting);
            sale.setSaleDate(LocalDateTime.now());
            sale.setPrice(painting.getPrice() != null ? painting.getPrice() : 50.0);
            saleRepository.save(sale);

            total += sale.getPrice();

            // Удаляем из корзины
            cartItemRepository.deleteByCartAndPainting(cart, painting);
        }

        // Деактивируем корзину после оформления заказа
        cart.setActive(false);
        cartRepository.save(cart);

        return total;
    }

    public List<Sale> getUserSalesHistory(User user) {
        return saleRepository.findByUserOrderBySaleDateDesc(user);
    }

    // Дополнительный метод для проверки доступности картины
    public boolean isPaintingAvailable(Long paintingId) {
        return paintingRepository.findById(paintingId)
                .map(p -> p.getStatus() == Painting.PaintingStatus.AVAILABLE)
                .orElse(false);
    }
}