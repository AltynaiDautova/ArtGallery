package com.example.art.service;

import com.example.art.model.Artist;
import com.example.art.model.Category;
import com.example.art.model.Painting;
import com.example.art.model.PaintingImage;
import com.example.art.repository.ArtistRepository;
import com.example.art.repository.CategoryRepository;
import com.example.art.repository.PaintingImageRepository;
import com.example.art.repository.PaintingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

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
        File destination = new File(uploadPath + File.separator + resultFilename);
        file.transferTo(destination);

        // Создаём объект изображения
        PaintingImage image = new PaintingImage();
        image.setImageUrl("/img/" + resultFilename);

        // Устанавливаем изображение картине
        painting.setImage(image);
        image.setPainting(painting); // можно и не ставить, но если нужна двусторонняя логика — полезно

        // Сохраняем картину — изображение сохранится автоматически (cascade = ALL)
        paintingRepository.save(painting);
    }


    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    public List<Painting> getAllPaintings() {
        return paintingRepository.findAll(); // Возвращаем все картины из репозитория
    }

}