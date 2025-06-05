package com.example.art.service;

import com.example.art.model.Painting.PaintingStatus;
import com.example.art.model.Sale;
import com.example.art.model.User;
import com.example.art.repository.SaleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public List<Sale> findRecentSales() {
        return saleRepository.findTop5ByOrderBySaleDateDesc();
    }

    public List<Sale> findRecentSales(int count) {
        return saleRepository.findAllByOrderBySaleDateDesc(Pageable.ofSize(count));
    }

    public List<Sale> getUserSalesHistory(User user, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null) {
            return saleRepository.findByUserAndSaleDateBetweenOrderBySaleDateDesc(
                    user,
                    dateFrom.atStartOfDay(),
                    dateTo.plusDays(1).atStartOfDay()
            );
        } else if (dateFrom != null) {
            return saleRepository.findByUserAndSaleDateAfterOrderBySaleDateDesc(
                    user,
                    dateFrom.atStartOfDay()
            );
        } else if (dateTo != null) {
            return saleRepository.findByUserAndSaleDateBeforeOrderBySaleDateDesc(
                    user,
                    dateTo.plusDays(1).atStartOfDay()
            );
        }
        return saleRepository.findByUserOrderBySaleDateDesc(user);
    }

    public List<Sale> findSalesWithFilters(LocalDateTime startDate, LocalDateTime endDate,
                                           PaintingStatus status, String customer) {
        return saleRepository.findAll(createFilterSpecification(startDate, endDate, status, customer),
                Sort.by(Sort.Direction.DESC, "saleDate"));
    }

    public Page<Sale> findSalesWithFiltersPaged(LocalDateTime startDate, LocalDateTime endDate,
                                                PaintingStatus status, String customer, Pageable pageable) {
        return saleRepository.findAll(createFilterSpecification(startDate, endDate, status, customer), pageable);
    }

    private Specification<Sale> createFilterSpecification(LocalDateTime startDate, LocalDateTime endDate,
                                                          PaintingStatus status, String customer) {
        return where(withDateAfter(startDate))
                .and(withDateBefore(endDate))
                .and(withStatus(status))
                .and(withCustomer(customer));
    }

    private Specification<Sale> withDateAfter(LocalDateTime date) {
        return (root, query, cb) ->
                date != null ? cb.greaterThanOrEqualTo(root.get("saleDate"), date) : null;
    }

    private Specification<Sale> withDateBefore(LocalDateTime date) {
        return (root, query, cb) ->
                date != null ? cb.lessThanOrEqualTo(root.get("saleDate"), date) : null;
    }

    private Specification<Sale> withStatus(PaintingStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.join("painting").get("status"), status);
        };
    }

    private Specification<Sale> withCustomer(String customer) {
        return (root, query, cb) -> {
            if (customer == null || customer.isEmpty()) return null;

            String pattern = "%" + customer.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.join("user").get("fullName")), pattern),
                    cb.like(cb.lower(root.join("user").get("email")), pattern)
            );
        };
    }

    // Добавленные методы:

    public Sale findById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale с id " + id + " не найден"));
    }

    public Sale save(Sale sale) {
        return saleRepository.save(sale);
    }
}
