package com.example.art.repository;

import com.example.art.model.PaintingImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaintingImageRepository extends JpaRepository<PaintingImage, Long> {
}