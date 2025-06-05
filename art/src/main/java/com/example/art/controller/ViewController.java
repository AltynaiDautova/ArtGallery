package com.example.art.controller;

import com.example.art.model.Painting;
import com.example.art.service.PaintingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/view")
public class ViewController {

    @Autowired
    private PaintingService paintingService;

    @GetMapping("/paintings")
    public String viewPaintings(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer creationYear,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Painting.PaintingStatus status,
            Model model) {

        // Используем обновленный метод getFilteredPaintings с параметром status
        List<Painting> paintings = paintingService.getFilteredPaintings(
                artistId, categoryId, creationYear, genre, material, title, status);
        model.addAttribute("paintings", paintings);

        // Получаем уникальных художников через сервис
        model.addAttribute("artists", paintingService.getAllArtists());

        // Получаем уникальные категории через сервис
        model.addAttribute("categories", paintingService.getAllCategories());

        // Получаем уникальные годы создания через сервис
        model.addAttribute("availableYears", paintingService.getAvailableYears());

        // Получаем уникальные жанры через сервис
        model.addAttribute("availableGenres", paintingService.getAvailableGenres());

        // Получаем уникальные материалы через сервис
        model.addAttribute("availableMaterials", paintingService.getAvailableMaterials());

        return "paintings";
    }

    @GetMapping("/paintings/{id}")
    public String viewPaintingDetails(@PathVariable Long id, Model model) {
        Optional<Painting> paintingOptional = paintingService.getPaintingById(id);
        if (paintingOptional.isEmpty()) {
            return "redirect:/view/paintings";
        }
        model.addAttribute("painting", paintingOptional.get());
        return "painting-details-user";
    }
}