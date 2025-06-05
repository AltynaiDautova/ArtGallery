package com.example.art.repository;

import com.example.art.model.Painting;
import com.example.art.model.Painting.PaintingStatus;
import com.example.art.model.Sale;
import com.example.art.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    // Основные методы фильтрации
    List<Sale> findByUserOrderBySaleDateDesc(User user);

    List<Sale> findByUserAndSaleDateBetweenOrderBySaleDateDesc(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Sale> findByUserAndSaleDateAfterOrderBySaleDateDesc(
            User user,
            LocalDateTime startDate
    );

    List<Sale> findByUserAndSaleDateBeforeOrderBySaleDateDesc(
            User user,
            LocalDateTime endDate
    );

    // Методы для получения последних продаж
    List<Sale> findTop5ByOrderBySaleDateDesc();
    List<Sale> findAllByOrderBySaleDateDesc(Pageable pageable);

    // Методы с использованием спецификаций
    List<Sale> findAll(Specification<Sale> spec, Sort sort);
    Page<Sale> findAll(Specification<Sale> spec, Pageable pageable);



    // Дополнительные методы для аналитики
    @Query("SELECT SUM(s.price) FROM Sale s WHERE s.user = :user")
    Double getTotalSalesAmountByUser(@Param("user") User user);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.user = :user AND s.painting.status = :status")
    Long countByUserAndStatus(@Param("user") User user,
                              @Param("status") PaintingStatus status);

    @Query("SELECT s FROM Sale s JOIN FETCH s.painting p WHERE p.artist.id = :artistId")
    List<Sale> findByArtistIdWithPainting(@Param("artistId") Long artistId);

    // Метод для поиска по части имени клиента или email
    @Query("SELECT s FROM Sale s JOIN s.user u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Sale> searchByCustomer(@Param("query") String query);
}