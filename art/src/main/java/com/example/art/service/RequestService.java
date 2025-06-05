package com.example.art.service;

import com.example.art.model.Request;
import com.example.art.repository.RequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Transactional
    public void saveRequest(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Некорректный email адрес");
        }

        Request request = new Request();
        request.setEmail(email);
        request.setCreatedAt(LocalDateTime.now());
        requestRepository.save(request);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }
}
