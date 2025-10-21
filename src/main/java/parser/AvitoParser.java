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
import java.util.regex.Pattern;


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
                String district = ad.select("div[data-marker=item-address]").text();
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
                //-- комнаты --
                int rooms = 0;
                java.util.regex.Matcher matcher = Pattern.compile("(\\d+)").matcher(title);
                if(matcher.find()) {
                    rooms = Integer.parseInt(matcher.group(1));
                }

                // дата публикации
                String dateText = ad.select("time[itemprop='datePublished']").attr("datetime");
                LocalDateTime publishedAt = null;
                if(dateText != null && !dateText.isEmpty()) {
                    try {
                        publishedAt = LocalDateTime.parse(dateText,java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    } catch (Exception ignored) {}
                }

                // фильтр: только свежие
                if (Duration.between(publishedAt, LocalDateTime.now()).compareTo(maxAge) <= 0) {
                    FlatListing flat = new FlatListing(title, price, district, url, 0, publishedAt);
                    listings.add(flat);
                }

            } catch (Exception e) {
                System.err.println("Ошибка парсинга объявления: " + e.getMessage());
            }
        }

        return listings;
    }

}