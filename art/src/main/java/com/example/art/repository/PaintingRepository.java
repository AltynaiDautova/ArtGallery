package com.example.art.repository;

import com.example.art.model.Painting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaintingRepository extends JpaRepository<Painting, Long> {
}