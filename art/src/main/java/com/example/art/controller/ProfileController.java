package com.example.art.controller;

import com.example.art.model.User;
import com.example.art.repository.UserRepository;
import com.example.art.util.PasswordUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Value("${upload.path}")
    private String uploadPathString;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadPathString);
    }

    @GetMapping
    public String showProfilePage(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", currentUser);
        return "profile";
    }

    @GetMapping("/edit")
    public String showEditProfileForm(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", currentUser);
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(HttpSession session,
                                @ModelAttribute User updatedUser,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                                @RequestParam(value = "removeAvatar", defaultValue = "false") String removeAvatarStr,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            // Обновление данных
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setEmail(updatedUser.getEmail());
            currentUser.setPhone(updatedUser.getPhone());

            // Аватар
            boolean removeAvatar = "true".equals(removeAvatarStr);
            handleAvatarUpdate(currentUser, avatarFile, removeAvatar);

            userRepository.save(currentUser);
            session.setAttribute("user", currentUser);

            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
            return "redirect:/profile";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обработке изображения: " + e.getMessage());
            return "redirect:/profile/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении профиля: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", currentUser);
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(HttpSession session,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (!PasswordUtils.checkPassword(currentPassword, currentUser.getPassword())) {
            model.addAttribute("error", "Текущий пароль неверный");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Новые пароли не совпадают");
            return "change-password";
        }

        currentUser.setPassword(PasswordUtils.hashPassword(newPassword));
        userRepository.save(currentUser);
        session.setAttribute("user", currentUser);

        redirectAttributes.addFlashAttribute("success", "Пароль успешно изменён");
        return "redirect:/profile";
    }

    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    private void handleAvatarUpdate(User user, MultipartFile avatarFile, boolean removeAvatar) throws IOException {
        if (removeAvatar) {
            if (user.getAvatarUrl() != null) {
                deleteFile(user.getAvatarUrl());
                user.setAvatarUrl(null);
            }
        } else if (avatarFile != null && !avatarFile.isEmpty()) {
            if (!avatarFile.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("Пожалуйста, загрузите изображение");
            }

            String newAvatarPath = saveFile(avatarFile);

            if (user.getAvatarUrl() != null) {
                deleteFile(user.getAvatarUrl());
            }

            user.setAvatarUrl(newAvatarPath);
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + extension;

        Path targetLocation = uploadPath.resolve(fileName);
        file.transferTo(targetLocation.toFile());

        return "/img/" + fileName; // URL-доступ через /img/**
    }

    private void deleteFile(String relativeUrl) throws IOException {
        if (relativeUrl == null || relativeUrl.isEmpty()) return;

        String filename = Paths.get(relativeUrl).getFileName().toString();
        Path filePath = uploadPath.resolve(filename);
        Files.deleteIfExists(filePath);
    }
}
