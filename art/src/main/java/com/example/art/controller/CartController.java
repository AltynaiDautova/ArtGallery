package com.example.art.controller;

import com.example.art.model.Painting;
import com.example.art.model.Painting.PaintingStatus;
import com.example.art.model.User;
import com.example.art.service.CartService;
import com.example.art.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    @PostMapping("/add/{paintingId}")
    public String addToCart(@PathVariable Long paintingId, HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        User user = authService.getCurrentUser(request);
        try {
            boolean added = cartService.addToCart(user, paintingId);

            if (!added) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Картина уже была добавлена в корзину или недоступна для покупки.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Картина добавлена в корзину.");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:" + request.getHeader("Referer");
    }

    @GetMapping
    public String viewCart(Model model, HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        List<Painting> cartItems = cartService.getUserCartItems(user);

        model.addAttribute("paintings", cartItems);
        model.addAttribute("cartItemCount", cartService.getCartItemCount(user));

        double total = cartItems.stream()
                .filter(p -> p.getStatus() == PaintingStatus.AVAILABLE)
                .mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 50.0)
                .sum();
        model.addAttribute("totalPrice", total);

        return "cart/view";
    }

    @PostMapping("/remove/{paintingId}")
    public String removeFromCart(@PathVariable Long paintingId,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        User user = authService.getCurrentUser(request);
        cartService.removeFromCart(user, paintingId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Картина удалена из корзины");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("selectedItems") List<Long> selectedItemIds,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        User user = authService.getCurrentUser(request);
        try {
            double total = cartService.processCheckout(user, selectedItemIds);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Покупка оформлена! Сумма: $%.2f", total));

            return "redirect:/sales/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при оформлении покупки: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/sales/history")
    public String salesHistory(Model model, HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        model.addAttribute("sales", cartService.getUserSalesHistory(user));
        return "sales/history";
    }
}