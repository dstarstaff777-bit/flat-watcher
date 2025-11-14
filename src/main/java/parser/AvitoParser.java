package parser;

import model.FlatListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.SeleniumFetcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class AvitoParser {

    private final SeleniumFetcher seleniumFetcher;

    public AvitoParser(SeleniumFetcher seleniumFetcher) {
        this.seleniumFetcher = seleniumFetcher;
    }

    public List<FlatListing> fetch(String searchUrl) {
        List<FlatListing> flats = new ArrayList<>();

        try {
            System.out.println("🌐 Загружаем страницу: " + searchUrl);

            FetchResult result = seleniumFetcher.fetchPageSource(searchUrl);
            String html = result.html();

            if (html == null || html.isEmpty()) {
                System.out.println("⚠️ Пустая страница, парсинг пропущен");
                return flats;
            }

            Document doc = Jsoup.parse(html);

            // Новый рабочий селектор в 2025
            Elements items = doc.select("div[data-marker='item']");

            System.out.println("🔍 Найдено элементов: " + items.size());

            for (Element item : items) {
                FlatListing flat = new FlatListing();

                /*
                 * 1. Заголовок (НОВЫЙ СЕЛЕКТОР)
                 */
                Element titleEl = item.selectFirst("a[data-marker='item-title']");
                String title = titleEl != null ? titleEl.text().trim() : null;
                flat.setTitle(title);


                /*
                 * 2. Ссылка
                 */
                String href = null;

                Element linkEl = item.selectFirst("a[data-marker='item-title']");
                if (linkEl != null) {
                    href = linkEl.attr("href");
                }

                if (href == null || href.isEmpty()) {
                    linkEl = item.selectFirst("a[itemprop='url']");
                    if (linkEl != null) href = linkEl.attr("href");
                }

                if (href != null) {
                    if (!href.startsWith("http"))
                        href = "https://www.avito.ru" + href;

                    flat.setUrl(href);
                }


                /*
                 * 3. Цена
                 */
                Element priceEl = item.selectFirst("span[data-marker='item-price']");
                String price = null;

                if (priceEl != null) {
                    price = priceEl.text().replaceAll("[^0-9]", "");
                } else if (result.priceText() != null) {
                    // Если Selenium нашёл цену на странице объявления
                    price = result.priceText().replaceAll("[^0-9]", "");
                }

                flat.setPrice(price);


                /*
                 * 4. Район
                 */
                Element addressEl = item.selectFirst("div[data-marker='item-address']");
                if (addressEl != null) {
                    flat.setDistrict(addressEl.text().trim());
                }


                /*
                 * 5. Дата публикации (реальная, из карточки)
                 */
                Element dateEl = item.selectFirst("div[data-marker='item-date']");
                LocalDateTime publishedAt = parseDate(dateEl != null ? dateEl.text() : null);
                flat.setPublishedAt(publishedAt);


                /*
                 * 6. Кол-во комнат
                 */
                flat.setRooms(extractRooms(title));


                flats.add(flat);
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка парсинга Avito: " + e.getMessage());
        }

        System.out.println("📦 Собрано объявлений: " + flats.size());
        return flats;
    }

    private LocalDateTime parseDate(String text) {
        if (text == null) return null;

        text = text.trim();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        try {
            if (text.contains("Только что")) return now.minusMinutes(1);

            if (text.contains("минут")) {
                int m = Integer.parseInt(text.replaceAll("\\D+", ""));
                return now.minusMinutes(m);
            }

            if (text.contains("час")) {
                int h = Integer.parseInt(text.replaceAll("\\D+", ""));
                return now.minusHours(h);
            }

            if (text.startsWith("Сегодня")) {
                String time = text.replace("Сегодня", "").trim(); // "13:20"
                return LocalDateTime.of(today, LocalTime.parse(time));
            }

            // Формат
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM 'в' HH:mm", new Locale("ru"));
            return LocalDateTime.parse(text, fmt);

        } catch (Exception e) {
            System.out.println("⚠️ Не удалось разобрать дату: " + text);
            return now;
        }
    }

    private String extractRooms(String title) {
        if (title == null) return "?";
        title = title.toLowerCase();

        if (title.contains("1-к")) return "1";
        if (title.contains("2-к")) return "2";
        if (title.contains("3-к")) return "3";
        if (title.contains("4-к")) return "4";
        if (title.contains("5-к")) return "5";

        return "?";
    }
}