package com.example.art.controller;

import com.example.art.model.User;
import com.example.art.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new User()); // Для формы регистрации
        return "login";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        if (authService.userExists(user.getUsername())) {
            model.addAttribute("registerMessage", "Пользователь уже существует!");
        } else {
            authService.register(user);
            model.addAttribute("registerMessage", "Регистрация прошла успешно! Теперь войдите.");
        }
        model.addAttribute("user", new User()); // Сброс формы
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = authService.authenticate(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            if ("admin".equals(user.getRole())) {
                return "redirect:/paintings";
            } else {
                return "redirect:/view/paintings";
            }
        }
        model.addAttribute("message", "Неверный логин или пароль!");
        model.addAttribute("user", new User()); // Добавляем пустой объект user для формы регистрации
        return "login";
    }
}
