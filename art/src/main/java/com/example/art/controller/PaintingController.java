package com.example.art.controller;

import com.example.art.model.Painting;
import com.example.art.service.PaintingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/paintings")
public class PaintingController {

    private final PaintingService paintingService;

    // Предопределенный список материалов
    private static final List<String> MATERIALS = Arrays.asList(
            "Холст, масло",
            "Дерево, масло",
            "Бумага, акварель",
            "Картон, темпера",
            "Металл, эмаль",
            "Фреска",
            "Другое"
    );

    public PaintingController(PaintingService paintingService) {
        this.paintingService = paintingService;
    }

    // Просмотр списка картин с фильтрацией
    @GetMapping
    public String listPaintings(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer creationYear,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String title,
            Model model) {

        // Добавляем отфильтрованный список картин
        model.addAttribute("paintings", paintingService.getFilteredPaintings(
                artistId, categoryId, creationYear, genre, material, title));

        // Добавляем списки для выпадающих меню фильтров
        model.addAttribute("artists", paintingService.getAllArtists());
        model.addAttribute("categories", paintingService.getAllCategories());
        model.addAttribute("availableYears", paintingService.getAvailableYears());
        model.addAttribute("availableGenres", paintingService.getAvailableGenres());
        model.addAttribute("availableMaterials", paintingService.getAvailableMaterials());

        return "paintings-list";
    }

    // Просмотр деталей конкретной картины
    @GetMapping("/{id}")
    public String viewPainting(@PathVariable Long id, Model model) {
        Painting painting = paintingService.getPaintingById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Картина не найдена"));

        model.addAttribute("painting", painting);
        model.addAttribute("materials", MATERIALS);
        return "painting-details";
    }

    // Форма добавления новой картины
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("painting", new Painting());
        model.addAttribute("artists", paintingService.getAllArtists());
        model.addAttribute("categories", paintingService.getAllCategories());
        model.addAttribute("materials", MATERIALS);
        return "add-painting";
    }

    // Обработка добавления новой картины
    @PostMapping("/add")
    public String addPainting(
            @ModelAttribute Painting painting,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "otherMaterial", required = false) String otherMaterial
    ) throws IOException {

        if ("Другое".equals(painting.getMaterial())) {
            painting.setMaterial(otherMaterial);
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        paintingService.savePaintingWithImage(painting, file);
        return "redirect:/paintings";
    }

    // Форма редактирования картины
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Painting painting = paintingService.getPaintingById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Картина не найдена"));

        model.addAttribute("painting", painting);
        model.addAttribute("artists", paintingService.getAllArtists());
        model.addAttribute("categories", paintingService.getAllCategories());
        model.addAttribute("materials", MATERIALS);
        return "edit-painting";
    }

    // Обработка обновления картины
    @PostMapping("/update/{id}")
    public String updatePainting(
            @PathVariable Long id,
            @ModelAttribute Painting painting,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "otherMaterial", required = false) String otherMaterial
    ) throws IOException {

        if ("Другое".equals(painting.getMaterial())) {
            painting.setMaterial(otherMaterial);
        }

        paintingService.updatePainting(id, painting, file);
        return "redirect:/paintings/" + id;
    }

    // Удаление картины
    @PostMapping("/delete/{id}")
    public String deletePainting(@PathVariable Long id) throws IOException {
        paintingService.deletePainting(id);
        return "redirect:/paintings";
    }

    // Альтернативный вариант с URL изображения вместо загрузки файла
    @PostMapping("/add-with-url")
    public String addPaintingWithUrl(
            @ModelAttribute Painting painting,
            @RequestParam String imageUrl
    ) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL изображения не может быть пустым");
        }

        paintingService.savePainting(painting, imageUrl);
        return "redirect:/paintings";
    }


    // Альтернативный вариант с URL изображения вместо загрузки файла
    /*
    @PostMapping("/add")
    public String addPainting(
            @ModelAttribute Painting painting,
            @RequestParam String imageUrl
    ) {
        // Валидация URL
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL изображения не может быть пустым");
        }

        paintingService.savePainting(painting, imageUrl);
        return "redirect:/paintings";
    }
    */
}