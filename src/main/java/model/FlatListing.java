package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlatListing {
    private String title;
    private int price;
    private String district;
    private String url;
    private int rooms;
    private LocalDateTime publishedAt;

    public FlatListing(String title, int price, String district, String url, int rooms, LocalDateTime publishedAt) {
        this.title = title;
        this.price = price;
        this.district = district;
        this.url = url;
        this.rooms = rooms;
        this.publishedAt = publishedAt;
    }

    // --- Геттеры и сеттеры ---
    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public String getDistrict() {
        return district;
    }

    public String getUrl() {
        return url;
    }

    public int getRooms() {
        return rooms;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    // --- Форматированная дата для Telegram ---
    public String getFormattedPublishedAt() {
        if (publishedAt == null) return "не указано";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return publishedAt.format(formatter);
    }
}
