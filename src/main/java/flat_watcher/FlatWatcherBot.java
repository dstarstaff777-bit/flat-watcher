package flat_watcher;

import model.FlatListing;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.AvitoParser;
import util.Config;

import java.time.Duration;
import java.util.List;

public class FlatWatcherBot extends TelegramLongPollingBot {

    private final AvitoParser parser;
    private final String botToken = System.getenv("BOT_TOKEN");
    private final long ownerChatId = Long.parseLong(System.getenv("CHAT_ID"));

    public FlatWatcherBot() {
        this.parser = new AvitoParser();
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("BOT_TOKEN = " + System.getenv("BOT_TOKEN"));
        System.out.println("CHAT_ID = " + System.getenv("CHAT_ID"));
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendMessage(chatId,
                        "Привет! Я помогу найти квартиры 🏠\n" +
                                "Формат команды:\n" +
                                "/find <комнат> <макс_цена> <район> <минуты>\n\n" +
                                "Пример: /find 1 3000000 Центр 60");
            } else if (messageText.startsWith("/find")) {
                String[] parts = messageText.split(" ", 5);

                if (parts.length < 5) {
                    sendMessage(chatId,
                            "❌ Неправильный формат.\n" +
                                    "Используй: /find <комнат> <макс_цена> <район> <минуты>");
                    return;
                }

                int rooms;
                int maxPrice;
                String district;
                int minutes;
                try {
                    rooms = Integer.parseInt(parts[1]);
                    maxPrice = Integer.parseInt(parts[2]);
                    district = parts[3];
                    minutes = Integer.parseInt(parts[4]);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "❌ Ошибка: комнаты, цена и минуты должны быть числами.");
                    return;
                }

                // URL Avito
                String baseUrl = "https://www.avito.ru/uzlovaya/kvartiry/prodam";
                if (rooms == 1) {
                    baseUrl += "/1-komnatnye-ASgBAgICAUSSA8YQwM0U";
                } else if (rooms == 2) {
                    baseUrl += "/2-komnatnye-ASgBAgICAUSSA8YQwM0U";
                } else if (rooms == 3) {
                    baseUrl += "/3-komnatnye-ASgBAgICAUSSA8YQwM0U";
                }

                // Парсим объявления с ограничением по времени
                List<FlatListing> flats = parser.fetchListings(baseUrl, Duration.ofMinutes(minutes));

                // Фильтруем по цене и району
                List<FlatListing> filtered = flats.stream()
                        .filter(f -> f.getPrice() <= maxPrice)
                        .filter(f -> f.getDistrict().contains(district))
                        .toList();

                if (filtered.isEmpty()) {
                    sendMessage(chatId, "😔 Ничего не найдено по заданным параметрам.");
                } else {
                    for (FlatListing flat : filtered) {
                        String text = String.format(
                                "🏠 <b>%s</b>\n💰 %s ₽\n📍 %s\n🔗 <a href=\"%s\">Ссылка</a>",
                                flat.getTitle(),
                                flat.getPrice(),
                                flat.getDistrict(),
                                flat.getUrl()
                        );
                        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
                        msg.setParseMode("HTML");
                        try {
                            execute(msg);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                sendMessage(chatId, "Неизвестная команда. Напиши /start для справки.");
            }
        }
    }

    /**
     *  Метод для автопроверки новых обьявлений (по расписанию)
     */
    public void checkNewFlats() {
        List<FlatListing> flats = parser.fetchListings(
                "https://www.avito.ru/uzlovaya/kvartiry/prodam",
                Duration.ofMinutes(60) // фильтр: свежие за последние 60 минут
        );

        if (flats.isEmpty()) {
            sendMessage(ownerChatId, "Свежих объявлений за последние 60 минут нет 💤");
        } else {
            for (FlatListing flat : flats) {
                String text = String.format(
                        "🏠 <b>%s</b>\n💰 %s ₽\n📍 %s\n🔗 <a href=\"%s\">Ссылка</a>",
                        flat.getTitle(),
                        flat.getPrice(),
                        flat.getDistrict(),
                        flat.getUrl()
                );

                SendMessage msg = new SendMessage(String.valueOf(ownerChatId), text);
                msg.setParseMode("HTML");
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "ValeryHousebot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }
}
