package com.example.art.controller;

import com.example.art.model.Painting;
import com.example.art.service.PaintingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/paintings")
public class PaintingController {

    private final PaintingService paintingService;

    public PaintingController(PaintingService paintingService) {
        this.paintingService = paintingService;
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        // Добавляем пустой объект Painting для формы
        model.addAttribute("painting", new Painting());

        // Добавляем списки художников и категорий для выпадающих списков
        model.addAttribute("artists", paintingService.getAllArtists());
        model.addAttribute("categories", paintingService.getAllCategories());

        return "add-painting"; // Имя Thymeleaf шаблона
    }

    @PostMapping("/add")
    public String addPainting(
            @ModelAttribute Painting painting,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        // Проверка на пустой файл
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        // Сохранение картины с изображением
        paintingService.savePaintingWithImage(painting, file);

        // Перенаправление на список картин после успешного сохранения
        return "redirect:/paintings";

    }
    @GetMapping("")
    public String listPaintings(Model model) {
        List<Painting> paintings = paintingService.getAllPaintings(); // Получаем список картин
        model.addAttribute("paintings", paintings); // Передаем картины в шаблон
        return "paintings-list"; // Название шаблона для списка картин
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