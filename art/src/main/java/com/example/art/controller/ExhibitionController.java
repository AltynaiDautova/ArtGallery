package com.example.art.controller;

import com.example.art.model.Exhibition;
import com.example.art.service.ExhibitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/view/exhibitions")
public class ExhibitionController {

    @Autowired
    private ExhibitionService exhibitionService;

    @GetMapping
    public String getAllExhibitions(Model model) {
        List<Exhibition> exhibitions = exhibitionService.getAllCurrentExhibitions();
        model.addAttribute("exhibitions", exhibitions);
        return "exhibitions";
    }

    @GetMapping("/{id}")
    public String getExhibitionDetails(@PathVariable Long id, Model model) {
        Exhibition exhibition = exhibitionService.getExhibitionById(id);
        model.addAttribute("exhibition", exhibition);
        return "exhibition-details";
    }

    @PostMapping("/register")
    public String registerForExhibition(
            @RequestParam Long exhibitionId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            RedirectAttributes redirectAttributes) {

        try {
            exhibitionService.registerVisitor(exhibitionId, name, email, phone, visitDate);
            redirectAttributes.addFlashAttribute("successMessage", "Вы успешно зарегистрировались на выставку!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка регистрации: " + e.getMessage());
        }

        return "redirect:/view/exhibitions/" + exhibitionId;
    }

    // ...
    @GetMapping("/new")
    public String showAddExhibitionForm(Model model) {
        model.addAttribute("exhibition", new Exhibition());
        return "add-exhibition";
    }

    @PostMapping("/add")
    public String addExhibition(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes
            // @RequestParam("imageFile") MultipartFile imageFile // если нужен upload файла
    ) {
        Exhibition exhibition = new Exhibition();
        exhibition.setName(name);
        exhibition.setDescription(description);
        exhibition.setDate(date);
        exhibition.setImageUrl(imageUrl); // либо обработайте загруженный файл

        exhibitionService.saveExhibition(exhibition);

        redirectAttributes.addFlashAttribute("successMessage", "Выставка успешно добавлена!");
        return "redirect:/view/exhibitions";
    }
}
