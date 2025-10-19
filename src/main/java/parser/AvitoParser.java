package parser;

import model.FlatListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.SeleniumFetcher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class AvitoParser {


    private LocalDateTime parseDate(String text) {
        LocalDateTime now = LocalDateTime.now();

        if (text.startsWith("Сегодня")) {
            String time = text.replace("Сегодня в ", "").trim();
            LocalTime localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(now.toLocalDate(), localTime);
        } else if (text.startsWith("Вчера")) {
            String time = text.replace("Вчера в ", "").trim();
            LocalTime localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(now.toLocalDate().minusDays(1), localTime);
        }
        // fallback — если формат другой
        return now.minusDays(2);
    }

    public List<FlatListing> fetchListings(String url, Duration maxAge) {
        List<FlatListing> listings = new ArrayList<>();
        String html = SeleniumFetcher.fetchPageSource(url);
        Document doc = Jsoup.parse(html);

        Elements ads = doc.select("div[data-marker=item]");
        for (Element ad : ads) {
            try {
                String title = ad.select("h3[itemprop=name]").text();
                String urlPath = ad.select("a[itemprop=url]").attr("href");
                String fullUrl = "https://www.avito.ru" + urlPath;
                String priceText = ad.select("span[itemprop=price]").attr("content").trim();
                int price = 0;
                if(priceText != null && !priceText.isEmpty()) {
                    try {
                        price = Integer.parseInt(priceText);
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка парсинга цены: " + priceText);
                        price = 0;
                    }
                } else {
                    System.err.println("Цена отсутствует для обьявления: " + fullUrl);
                }

                String district = ad.select("div[data-marker=item-address]").text();

                // дата публикации
                String dateText = ad.select("span[data-marker=item-date]").text();
                LocalDateTime publishedAt = parseDate(dateText);

                // фильтр: только свежие
                if (Duration.between(publishedAt, LocalDateTime.now()).compareTo(maxAge) <= 0) {
                    FlatListing flat = new FlatListing(title, price, 0, district, fullUrl);
                    listings.add(flat);
                }

            } catch (Exception e) {
                System.err.println("Ошибка парсинга объявления: " + e.getMessage());
            }
        }

        return listings;
    }


    private int extractRoomsFromTitle(String title) {
        if (title.contains("1-к")) return 1;
        if (title.contains("2-к")) return 2;
        if (title.contains("3-к")) return 3;
        return 0;

    }
    public static void printTable(List<FlatListing> flats) {
        String leftAlignFormat = "| %-50s | %-10s | %-20s | %-20s | %n";
        System.out.format("+--------------------------------------+----------------+---------------+-----------+%n");
        System.out.format("| Заголовок                            | Цена           | Кол-во комнат | Район     |%n");
        System.out.format("+--------------------------------------+----------------+---------------+-----------+%n");

        for (FlatListing flat : flats) {
            System.out.format(leftAlignFormat,
                    flat.getTitle(),
                    flat.getPrice(),
                    flat.getRooms(),
                    flat.getDistrict()
            );
        }
        System.out.format("+--------------------------------------+----------------+---------------+-----------+%n");
    }
}



