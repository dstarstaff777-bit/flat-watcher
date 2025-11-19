package parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.FlatListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.FetchResult;
import util.SeleniumFetcher;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AvitoParser {

    private final SeleniumFetcher seleniumFetcher;
    private final ObjectMapper mapper = new ObjectMapper();

    public AvitoParser(SeleniumFetcher seleniumFetcher) {
        this.seleniumFetcher = seleniumFetcher;
    }

    public List<FlatListing> fetch(String searchUrl) {
        List<FlatListing> flats = new ArrayList<>();

        try {
            System.out.println("🌐 Загружаем страницу: " + searchUrl);
            parser.FetchResult result = seleniumFetcher.fetchPageSource(searchUrl);
            String html = result.html();

            if (html == null || html.isBlank()) {
                System.out.println("⚠️ Пустая страница — пропускаем");
                return flats;
            }

            Document doc = Jsoup.parse(html);

            /*
             * 1) Пробуем JSON-LD offers
             */
            if (extractFromJsonLd(doc, flats)) {
                System.out.println("📦 JSON-LD дал " + flats.size() + " объявлений");
                return flats;
            }

            /*
             * 2) Если JSON пустой → фоллбэк парсинг DOM
             */
            Elements items = doc.select("div[data-marker='item']");
            System.out.println("DOM: найдено элементов: " + items.size());

            for (Element item : items) {
                FlatListing flat = parseDomItem(item, result);
                if (flat != null) flats.add(flat);
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка AvitoParser: " + e.getMessage());
        }

        return flats;
    }


    // =============================== JSON-LD ===============================

    private boolean extractFromJsonLd(Document doc, List<FlatListing> out) {
        Elements scripts = doc.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            try {
                JsonNode root = mapper.readTree(script.data());
                if (root == null) continue;

                if (root.has("@graph")) {
                    for (JsonNode node : root.get("@graph"))
                        parseJsonOffers(node, out);
                } else {
                    parseJsonOffers(root, out);
                }
            } catch (Exception ignored) {}
        }
        return !out.isEmpty();
    }

    private void parseJsonOffers(JsonNode node, List<FlatListing> out) {
        if (!node.has("offers")) return;

        JsonNode offers = node.get("offers");
        if (offers.has("offers")) offers = offers.get("offers");
        if (!offers.isArray()) return;

        for (JsonNode offer : offers) {
            try {
                FlatListing f = new FlatListing();

                f.setUrl(offer.has("url") ? offer.get("url").asText(null) : null);
                f.setTitle(offer.has("name") ? offer.get("name").asText(null) : null);

                if (offer.has("price")) {
                    f.setPrice(offer.get("price").asText("").replaceAll("[^0-9]", ""));
                }

                // validFrom — дата публикации
                LocalDateTime published = parseDateIso(offer.path("validFrom").asText(null));
                f.setPublishedAt(published != null ? published : LocalDateTime.now());

                f.setRooms(extractRooms(f.getTitle()));

                out.add(f);
            } catch (Exception ignored) {}
        }
    }

    private LocalDateTime parseDateIso(String iso) {
        if (iso == null) return null;
        try {
            if (iso.length() == 10) {
                return LocalDate.parse(iso).atStartOfDay();
            }
            return LocalDateTime.parse(iso);
        } catch (Exception e) {
            return null;
        }
    }


    // =============================== DOM парсинг ===============================

    private FlatListing parseDomItem(Element item, FetchResult fetchResult) {
        FlatListing flat = new FlatListing();

        // --- Title ---
        Element titleEl = item.selectFirst("a[data-marker='item-title'], a[itemprop='url']");
        flat.setTitle(titleEl != null ? titleEl.text().trim() : null);

        // --- URL ---
        String href = titleEl != null ? titleEl.attr("href") : null;
        if (href != null && !href.startsWith("http"))
            href = "https://www.avito.ru" + href;
        flat.setUrl(href);

        // --- Price ---
        Element priceEl = item.selectFirst("[data-marker='item-price'], span[itemprop='price']");
        String price = priceEl != null ? priceEl.text().replaceAll("[^0-9]", "") : null;
        if ((price == null || price.isEmpty()) && fetchResult.priceText() != null)
            price = fetchResult.priceText().replaceAll("[^0-9]", "");
        flat.setPrice(price);

        // --- District ---
        Element addressEl = item.selectFirst("[data-marker='item-address']");
        if (addressEl != null) flat.setDistrict(addressEl.text().trim());

        // --- Date ---
        Element dateEl = item.selectFirst("[data-marker='item-date']");
        if (dateEl != null)
            flat.setPublishedAt(parseDate(dateEl.text().trim()));
        else
            flat.setPublishedAt(LocalDateTime.now());

        // --- Rooms ---
        flat.setRooms(extractRooms(flat.getTitle()));

        return flat;
    }


    // =============================== Разбор даты ===============================

    private LocalDateTime parseDate(String text) {
        text = text.toLowerCase().trim();

        try {
            if (text.contains("минут")) {
                int m = extractInt(text);
                return LocalDateTime.now().minusMinutes(m);
            }
            if (text.contains("час")) {
                int h = extractInt(text);
                return LocalDateTime.now().minusHours(h);
            }
            if (text.contains("сегодня")) {
                String t = text.replace("сегодня", "").trim();
                return LocalDateTime.of(LocalDate.now(), LocalTime.parse(t, DateTimeFormatter.ofPattern("H:mm")));
            }
            if (text.contains("вчера")) {
                String t = text.replace("вчера", "").trim();
                LocalTime lt = LocalTime.parse(t, DateTimeFormatter.ofPattern("H:mm"));
                return LocalDateTime.of(LocalDate.now().minusDays(1), lt);
            }


            text = text.replace(",", "");
            DateTimeFormatter f = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("ru"));
            return LocalDateTime.parse(text, f);

        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private int extractInt(String s) {
        String n = s.replaceAll("[^0-9]", "");
        return n.isEmpty() ? 0 : Integer.parseInt(n);
    }


    // =============================== Rooms ===============================

    private String extractRooms(String title) {
        if (title == null) return "?";
        title = title.toLowerCase();
        if (title.contains("1-к") || title.contains("1 ком")) return "1";
        if (title.contains("2-к") || title.contains("2 ком")) return "2";
        if (title.contains("3-к") || title.contains("3 ком")) return "3";
        if (title.contains("4-к") || title.contains("4 ком")) return "4";
        return "?";
    }
}