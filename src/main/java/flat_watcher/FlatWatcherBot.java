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

    private final String botToken;
    private final long chatId;

    public FlatWatcherBot() {
        // Получаем параметры из config.properties
        this.botToken = Config.getProperty("telegram.bot.token");
        String chatIdStr = Config.getProperty("telegram.chat.id");

        if (botToken == null || chatIdStr == null || chatIdStr.isEmpty()) {
            throw new IllegalArgumentException("❌ BOT_TOKEN или CHAT_ID property is not set!");
        }

        this.chatId = Long.parseLong(chatIdStr);
    }

    @Override
    public String getBotUsername() {
        return "ValeryHousebot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();

            if (message.equalsIgnoreCase("/start")) {
                sendText(chatId, "👋 Привет! Я бот FlatWatcher.\n" +
                        "Я помогаю отслеживать свежие объявления на Avito.\n\n" +
                        "Чтобы найти новые квартиры — отправь /find");
            } else if (message.equalsIgnoreCase("/find")) {
                sendText(chatId, "🔎 Проверяю новые объявления за последние 60 минут...");

                try {
                    AvitoParser parser = new AvitoParser();
                    // пример: ищем объявления за последний час
                    List<FlatListing> listings = parser.fetchListings(
                            "https://www.avito.ru/uzlovaya/kvartiry/prodam",
                            Duration.ofHours(1)
                    );

                    if (listings.isEmpty()) {
                        sendText(chatId, "📭 За последние 60 минут новых объявлений не найдено.");
                    } else {
                        sendText(chatId, "✅ Найдено " + listings.size() + " новых объявлений:");

                        for (FlatListing flat : listings) {
                            String msg = String.format(
                                    "🏠 %s\n" +
                                            "💰 Цена: %s ₽\n" +
                                            "🛏 Комнат: %s\n" +
                                            "📍 Район: %s\n" +
                                            "🕒 Опубликовано: %s\n" +
                                            "🔗 %s",
                                    flat.getTitle(),
                                    flat.getPrice() > 0 ? flat.getPrice() : "не указана",
                                    flat.getRooms() > 0 ? flat.getRooms() : "не указано",
                                    (flat.getDistrict() != null && !flat.getDistrict().isBlank()) ? flat.getDistrict() : "не указан",
                                    flat.getFormattedPublishedAt(),
                                    flat.getUrl()
                            );

                            sendText(chatId, msg);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    sendText(chatId, "❌ Ошибка при парсинге: " + e.getMessage());
                }
            } else {
                sendText(chatId, "🤖 Неизвестная команда. Используй /find для поиска новых объявлений.");
            }
        }
    }
    /* Метод для отправки текста в Telegram
     */
    private void sendText(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
        }
    }
}
