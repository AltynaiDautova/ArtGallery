package com.example.art.repository;

import com.example.art.model.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
    List<Exhibition> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
}