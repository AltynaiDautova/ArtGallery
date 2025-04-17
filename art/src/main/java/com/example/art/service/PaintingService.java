package com.example.art.service;

import com.example.art.model.Artist;
import com.example.art.model.Category;
import com.example.art.model.Painting;
import com.example.art.model.PaintingImage;
import com.example.art.repository.ArtistRepository;
import com.example.art.repository.CategoryRepository;
import com.example.art.repository.PaintingImageRepository;
import com.example.art.repository.PaintingRepository;
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

    // Получение списка всех картин
    public List<Painting> getAllPaintings() {
        return paintingRepository.findAll();
    }

    // Получение отфильтрованного списка картин
    public List<Painting> getFilteredPaintings(
            Long artistId,
            Long categoryId,
            Integer creationYear,
            String genre,
            String material,
            String title) {

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

        return paintingRepository.findAll(spec);
    }

    // Получение картины по ID
    public Optional<Painting> getPaintingById(Long id) {
        return paintingRepository.findById(id);
    }

    // Получение списка всех художников
    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    // Получение списка всех категорий
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Получение списка доступных годов создания
    public List<Integer> getAvailableYears() {
        return paintingRepository.findDistinctCreationYears();
    }

    // Получение списка доступных жанров
    public List<String> getAvailableGenres() {
        return paintingRepository.findDistinctGenres();
    }

    // Получение списка доступных материалов
    public List<String> getAvailableMaterials() {
        return paintingRepository.findDistinctMaterials();
    }

    // Сохранение картины с URL изображения
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

    // Сохранение картины с загруженным изображением
    @Transactional
    public void savePaintingWithImage(Painting painting, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        // Создаем директорию для загрузки, если ее нет
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("Не удалось создать директорию загрузки");
        }

        // Генерируем уникальное имя файла
        String uuidFile = UUID.randomUUID().toString();
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String resultFilename = uuidFile + "." + fileExtension;
        Path destination = Paths.get(uploadPath + File.separator + resultFilename);

        // Сохраняем файл
        file.transferTo(destination);

        // Создаем и сохраняем изображение
        PaintingImage image = new PaintingImage();
        image.setImageUrl("/img/" + resultFilename);
        image.setPainting(painting);
        painting.setImage(image);

        paintingRepository.save(painting);
    }

    // Обновление картины
    @Transactional
    public void updatePainting(Long id, Painting paintingDetails, MultipartFile file) throws IOException {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Картина не найдена"));

        // Обновляем основные данные
        painting.setTitle(paintingDetails.getTitle());
        painting.setArtist(paintingDetails.getArtist());
        painting.setCategory(paintingDetails.getCategory());
        painting.setCreationYear(paintingDetails.getCreationYear());
        painting.setGenre(paintingDetails.getGenre());
        painting.setPeriod(paintingDetails.getPeriod());
        painting.setSize(paintingDetails.getSize());
        painting.setMaterial(paintingDetails.getMaterial());
        painting.setInventoryNumber(paintingDetails.getInventoryNumber());

        // Если загружено новое изображение
        if (file != null && !file.isEmpty()) {
            // Удаляем старое изображение
            if (painting.getImage() != null) {
                deleteImageFile(painting.getImage().getImageUrl());
                imageRepository.delete(painting.getImage());
            }

            // Сохраняем новое изображение
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

    // Удаление картины
    @Transactional
    public void deletePainting(Long id) throws IOException {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Картина не найдена"));

        // Удаляем связанное изображение
        if (painting.getImage() != null) {
            deleteImageFile(painting.getImage().getImageUrl());
            imageRepository.delete(painting.getImage());
        }

        paintingRepository.delete(painting);
    }

    // Вспомогательный метод для удаления файла изображения
    private void deleteImageFile(String imageUrl) throws IOException {
        if (imageUrl != null && imageUrl.startsWith("/img/")) {
            String filename = imageUrl.substring("/img/".length());
            Path filePath = Paths.get(uploadPath + File.separator + filename);
            Files.deleteIfExists(filePath);
        }
    }

    // Получение расширения файла
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}