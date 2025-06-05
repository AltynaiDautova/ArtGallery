package com.example.art.repository;

import com.example.art.model.VisitorRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorRegistrationRepository extends JpaRepository<VisitorRegistration, Long> {
}