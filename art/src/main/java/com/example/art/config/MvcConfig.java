package com.example.art.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Преобразуем путь к корректному формату
        String formattedPath = Paths.get(uploadPath).toUri().toString();

        registry.addResourceHandler("/img/**")
                .addResourceLocations(formattedPath)
                .setCachePeriod(3600); // Кэширование на 1 час (опционально)
    }
}