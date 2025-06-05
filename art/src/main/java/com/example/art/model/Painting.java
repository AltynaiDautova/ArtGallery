package com.example.art.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "painting")
public class Painting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "painting_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "genre", length = 255)
    private String genre;

    @Column(name = "creation_year")
    private Integer creationYear;

    @Column(name = "period", length = 255)
    private String period;

    @Column(name = "size", length = 255)
    private String size;

    @Column(name = "material", length = 255)
    private String material;

    @Column(name = "inventory_number", length = 255)
    private String inventoryNumber;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private PaintingImage image;

    @Column(name = "price")
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaintingStatus status = PaintingStatus.AVAILABLE;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public Integer getCreationYear() { return creationYear; }
    public void setCreationYear(Integer creationYear) { this.creationYear = creationYear; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public PaintingImage getImage() { return image; }
    public void setImage(PaintingImage image) { this.image = image; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public PaintingStatus getStatus() { return status; }
    public void setStatus(PaintingStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Painting)) return false;
        Painting painting = (Painting) o;
        return Objects.equals(id, painting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Painting{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }

    public enum PaintingStatus {
        AVAILABLE("Доступна"),
        SOLD("Продана"),
        RESERVED("Зарезервирована");

        private final String displayName;

        PaintingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}