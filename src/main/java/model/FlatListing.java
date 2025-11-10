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
    public String toTelegramMessage() {
        // форматируем дату красиво
        String date = publishedAt != null
                ? publishedAt.format(DateTimeFormatter.ofPattern("dd.MM в HH:mm"))
                : "время неизвестно";

        return  "🏠 <b>" + title + "</b>\n" +
                "💰 Цена: <b>" + price + "</b> ₽\n" +
                "📍 Район: " + (district != null ? district : "не указан") + "\n" +
                "🕒 Опубликовано: " + date + "\n" +
                "🔗 <a href=\"" + url + "\">Открыть объявление</a>";
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

}