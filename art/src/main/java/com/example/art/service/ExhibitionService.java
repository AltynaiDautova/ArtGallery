package com.example.art.service;

import com.example.art.exception.NotFoundException;
import com.example.art.model.Exhibition;
import com.example.art.model.VisitorRegistration;
import com.example.art.repository.ExhibitionRepository;
import com.example.art.repository.PaintingRepository;
import com.example.art.repository.VisitorRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final PaintingRepository paintingRepository;
    private final VisitorRegistrationRepository visitorRegistrationRepository;

    @Autowired
    public ExhibitionService(ExhibitionRepository exhibitionRepository,
                             PaintingRepository paintingRepository,
                             VisitorRegistrationRepository visitorRegistrationRepository) {
        this.exhibitionRepository = exhibitionRepository;
        this.paintingRepository = paintingRepository;
        this.visitorRegistrationRepository = visitorRegistrationRepository;
    }

    public List<Exhibition> getAllCurrentExhibitions() {
        LocalDate today = LocalDate.now();
        return exhibitionRepository.findByDateGreaterThanEqualOrderByDateAsc(today);
    }

    public Exhibition getExhibitionById(Long id) {
        return exhibitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Выставка с ID " + id + " не найдена"));
    }

    @Transactional
    public VisitorRegistration registerVisitor(Long exhibitionId, String name,
                                               String email, String phone, LocalDate visitDate) {
        Exhibition exhibition = getExhibitionById(exhibitionId);

        VisitorRegistration registration = new VisitorRegistration();
        registration.setExhibition(exhibition);
        registration.setName(name);
        registration.setEmail(email);
        registration.setPhone(phone);
        registration.setVisitDate(visitDate);
        registration.setRegistrationDate(LocalDateTime.now());

        return visitorRegistrationRepository.save(registration);
    }
    public void saveExhibition(Exhibition exhibition) {
        exhibitionRepository.save(exhibition);
    }
}