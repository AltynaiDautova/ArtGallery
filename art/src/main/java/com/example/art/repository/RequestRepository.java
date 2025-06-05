package com.example.art.repository;

import com.example.art.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {
    boolean existsByEmail(String email);
}