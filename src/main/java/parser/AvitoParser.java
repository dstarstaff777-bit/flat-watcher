package parser;

import model.FlatListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.SeleniumFetcher;

import java.time.LocalDateTime;
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

            // Каждый блок объявления
            Elements items = doc.select("div[data-marker='item']");

            System.out.println("🔍 Найдено элементов: " + items.size());

            for (Element item : items) {
                FlatListing flat = new FlatListing();

                // Заголовок
                Element titleEl = item.selectFirst("h3[itemprop='name']");
                if (titleEl != null) flat.setTitle(titleEl.text());

                // Ссылка
                Element linkEl = item.selectFirst("a[itemprop='url']");
                if (linkEl != null) {
                    String href = linkEl.attr("href");
                    if (!href.startsWith("http")) href = "https://www.avito.ru" + href;
                    flat.setUrl(href);
                }

                // Цена
                Element priceEl = item.selectFirst("[data-marker='item-price']");
                String price = (priceEl != null)
                        ? priceEl.text().replaceAll("[^0-9]", "")
                        : result.priceText(); // если Selenium нашёл цену
                flat.setPrice(price);

                // Адрес (район)
                Element addressEl = item.selectFirst("[data-marker='item-address']");
                if (addressEl != null) flat.setDistrict(addressEl.text());

                // Кол-во комнат (из заголовка)
                flat.setRooms(extractRooms(flat.getTitle()));

                // Время публикации (пока приближённо)
                flat.setPublishedAt(LocalDateTime.now());

                flats.add(flat);
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка парсинга Avito: " + e.getMessage());
        }

        System.out.println("📦 Собрано объявлений: " + flats.size());
        return flats;
    }

    /**
     * Примерное определение количества комнат из названия.
     */
    private String extractRooms(String title) {
        if (title == null) return "?";
        title = title.toLowerCase();
        if (title.contains("1-ком")) return "1";
        if (title.contains("2-ком")) return "2";
        if (title.contains("3-ком")) return "3";
        if (title.contains("4-ком")) return "4";
        if (title.contains("5-ком")) return "5";
        return "?";
    }
}