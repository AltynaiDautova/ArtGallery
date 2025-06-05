package com.example.art.controller;

import com.example.art.model.Painting;
import com.example.art.model.Sale;
import com.example.art.model.User;
import com.example.art.service.AuthService;
import com.example.art.service.PaintingService;
import com.example.art.service.SaleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final PaintingService paintingService;
    private final AuthService authService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SaleController(SaleService saleService, PaintingService paintingService, AuthService authService) {
        this.saleService = saleService;
        this.paintingService = paintingService;
        this.authService = authService;
    }

    @GetMapping("/history")
    public String salesHistory(HttpServletRequest request,
                               @RequestParam(required = false) String dateFrom,
                               @RequestParam(required = false) String dateTo,
                               Model model) {
        User user = authService.getCurrentUser(request);
        LocalDate from = parseDate(dateFrom);
        LocalDate to = parseDate(dateTo);

        List<Sale> sales = saleService.getUserSalesHistory(user, from, to);
        double totalAmount = sales.stream()
                .mapToDouble(s -> s.getPrice() != null ? s.getPrice() : 0.0)
                .sum();

        model.addAttribute("sales", sales);
        model.addAttribute("totalAmount", totalAmount);
        return "sales/history";
    }

    @GetMapping("/history-admin")
    public String salesHistoryForAdmin(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) Painting.PaintingStatus status,
                                       @RequestParam(required = false) String customer,
                                       Model model) {
        LocalDateTime start = parseDateTime(startDate, true);
        LocalDateTime end = parseDateTime(endDate, false);

        List<Sale> sales = saleService.findSalesWithFilters(start, end, status, customer);
        model.addAttribute("sales", sales);
        model.addAttribute("statuses", Painting.PaintingStatus.values());

        return "sales/history-admin";
    }

    @GetMapping("/{id}/edit")
    public String editSaleForm(@PathVariable Long id, Model model) {
        Sale sale = saleService.findById(id);
        if (sale == null) {
            return "redirect:/sales/history-admin";
        }
        model.addAttribute("sale", sale);
        model.addAttribute("statuses", Painting.PaintingStatus.values());
        return "sales/edit-sale";
    }

    @PostMapping("/{id}/edit")
    public String updateSale(@PathVariable Long id,
                             @RequestParam Double price,
                             @RequestParam String saleDate,
                             @RequestParam Painting.PaintingStatus status) {

        Sale sale = saleService.findById(id);
        if (sale == null) {
            return "redirect:/sales/history-admin";
        }

        sale.setPrice(price);
        LocalDateTime parsedDate = LocalDateTime.parse(saleDate);
        sale.setSaleDate(parsedDate);

        paintingService.updatePaintingStatus(sale.getPainting().getId(), status);

        saleService.save(sale);

        return "redirect:/sales/history-admin";
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        return LocalDate.parse(dateStr, dateFormatter);
    }

    private LocalDateTime parseDateTime(String dateStr, boolean startOfDay) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        return startOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
    }
}
