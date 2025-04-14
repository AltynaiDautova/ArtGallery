package com.example.art.model;

import jakarta.persistence.*;

@Entity
@Table(name = "painting_image")
public class PaintingImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    // Можно убрать ссылку на картину, если не используешь двустороннюю связь
    // Или оставить с mappedBy, если тебе она нужна
    @OneToOne(mappedBy = "image")
    private Painting painting;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Painting getPainting() { return painting; }
    public void setPainting(Painting painting) { this.painting = painting; }
}
