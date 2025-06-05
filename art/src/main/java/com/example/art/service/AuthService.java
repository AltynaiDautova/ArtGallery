package com.example.art.service;

import com.example.art.model.User;
import com.example.art.repository.UserRepository;
import com.example.art.util.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.example.art.util.PasswordUtils.checkPassword;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setActive(true);
        return userRepository.save(user);
    }

    public boolean login(String username, String password) {
        User user = userRepository.findByUsername(username);
        return user != null &&
                checkPassword(password, user.getPassword());
    }
    public boolean userExists(String username) {
        return userRepository.findByUsername(username) != null;
    }
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && checkPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    public User getCurrentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }

}