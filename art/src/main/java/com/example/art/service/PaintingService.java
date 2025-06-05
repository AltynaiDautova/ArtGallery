package com.example.art.service;

import com.example.art.model.*;
import com.example.art.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaintingService {

    private final PaintingRepository paintingRepository;
    private final ArtistRepository artistRepository;
    private final CategoryRepository categoryRepository;
    private final PaintingImageRepository imageRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public PaintingService(
            PaintingRepository paintingRepository,
            ArtistRepository artistRepository,
            CategoryRepository categoryRepository,
            PaintingImageRepository imageRepository) {
        this.paintingRepository = paintingRepository;
        this.artistRepository = artistRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
    }

    public List<Painting> getFilteredPaintings(
            Long artistId,
            Long categoryId,
            Integer creationYear,
            String genre,
            String material,
            String title,
            Painting.PaintingStatus status) {

        Specification<Painting> spec = Specification.where(null);

        if (artistId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("artist").get("id"), artistId));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId));
        }

        if (creationYear != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("creationYear"), creationYear));
        }

        if (genre != null && !genre.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("genre"), genre));
        }

        if (material != null && !material.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("material"), material));
        }

        if (title != null && !title.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return paintingRepository.findAll(spec);
    }

    @Transactional
    public void updatePaintingStatus(Long paintingId, Painting.PaintingStatus status) {
        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(() -> new EntityNotFoundException("Картина не найдена"));
        painting.setStatus(status);
        paintingRepository.save(painting);
    }

    // Остальные методы остаются без изменений
    public List<Painting> getAllPaintings() {
        return paintingRepository.findAll();
    }

    public Optional<Painting> getPaintingById(Long id) {
        return paintingRepository.findById(id);
    }

    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Integer> getAvailableYears() {
        return paintingRepository.findDistinctCreationYears();
    }

    public List<String> getAvailableGenres() {
        return paintingRepository.findDistinctGenres();
    }

    public List<String> getAvailableMaterials() {
        return paintingRepository.findDistinctMaterials();
    }

    @Transactional
    public void savePainting(Painting painting, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL изображения не может быть пустым");
        }

        PaintingImage image = new PaintingImage();
        image.setImageUrl(imageUrl);
        image.setPainting(painting);

        painting.setImage(image);
        paintingRepository.save(painting);
    }

    @Transactional
    public void savePaintingWithImage(Painting painting, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("Не удалось создать директорию загрузки");
        }

        String uuidFile = UUID.randomUUID().toString();
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String resultFilename = uuidFile + "." + fileExtension;
        Path destination = Paths.get(uploadPath + File.separator + resultFilename);

        file.transferTo(destination);

        PaintingImage image = new PaintingImage();
        image.setImageUrl("/img/" + resultFilename);
        image.setPainting(painting);
        painting.setImage(image);

        paintingRepository.save(painting);
    }

    @Transactional
    public void updatePainting(Long id, Painting paintingDetails, MultipartFile file) throws IOException {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Картина не найдена"));

        painting.setTitle(paintingDetails.getTitle());
        painting.setArtist(paintingDetails.getArtist());
        painting.setCategory(paintingDetails.getCategory());
        painting.setCreationYear(paintingDetails.getCreationYear());
        painting.setGenre(paintingDetails.getGenre());
        painting.setPeriod(paintingDetails.getPeriod());
        painting.setSize(paintingDetails.getSize());
        painting.setMaterial(paintingDetails.getMaterial());
        painting.setInventoryNumber(paintingDetails.getInventoryNumber());

        if (file != null && !file.isEmpty()) {
            if (painting.getImage() != null) {
                deleteImageFile(painting.getImage().getImageUrl());
                imageRepository.delete(painting.getImage());
            }

            String uuidFile = UUID.randomUUID().toString();
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String resultFilename = uuidFile + "." + fileExtension;
            Path destination = Paths.get(uploadPath + File.separator + resultFilename);
            file.transferTo(destination);

            PaintingImage newImage = new PaintingImage();
            newImage.setImageUrl("/img/" + resultFilename);
            newImage.setPainting(painting);
            painting.setImage(newImage);
        }

        paintingRepository.save(painting);
    }

    @Transactional
    public void deletePainting(Long id) throws IOException {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Картина не найдена"));

        if (painting.getImage() != null) {
            deleteImageFile(painting.getImage().getImageUrl());
            imageRepository.delete(painting.getImage());
        }

        paintingRepository.delete(painting);
    }

    private void deleteImageFile(String imageUrl) throws IOException {
        if (imageUrl != null && imageUrl.startsWith("/img/")) {
            String filename = imageUrl.substring("/img/".length());
            Path filePath = Paths.get(uploadPath + File.separator + filename);
            Files.deleteIfExists(filePath);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}