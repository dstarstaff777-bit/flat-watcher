package flat_watcher;

import model.FlatListing;
import notifer.TelegramNotifier;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import parser.AvitoParser;

import java.time.Duration;
import java.util.List;

public class FlatWatcherBot extends TelegramWebhookBot {

    private final String webhookUrl;
    private final TelegramNotifier notifier;
    private final AvitoParser parser;

    public FlatWatcherBot(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.notifier = new TelegramNotifier();
        this.parser = new AvitoParser();
    }

    //  Обработка сообщений, пришедших от Telegram
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String message = update.getMessage().getText().trim();

                if (message.equalsIgnoreCase("/start")) {
                    return new SendMessage(update.getMessage().getChatId().toString(),
                            "👋 Привет! Я слежу за новыми объявлениями на Avito.\n" +
                                    "Используй команду /find, чтобы проверить свежие объявления.");
                }

                if (message.equalsIgnoreCase("/find")) {
                    notifier.sendMessage("🔍 Проверяю новые объявления...");
                    checkNewFlats(update.getMessage().getChatId().toString());
                    return null;
                }

                return new SendMessage(update.getMessage().getChatId().toString(),
                        "Неизвестная команда. Используй /find для проверки новых объявлений.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //  Проверка новых объявлений
    public void checkNewFlats(String chatId) {
        try {
            List<FlatListing> listings = parser.fetchListings(
                    System.getenv("AVITO_URL"),
                    Duration.ofMinutes(60)
            );

            if (listings.isEmpty()) {
                notifier.sendMessage("🕐 За последние 60 минут новых объявлений не найдено.");
                return;
            }

            for (FlatListing flat : listings) {
                String msg = String.format(
                        "🏠 %s\n💰 Цена: %d ₽\n📍 Район: %s\n🕓 %s\n🔗 %s",
                        flat.getTitle(),
                        flat.getPrice(),
                        flat.getDistrict(),
                        flat.getPublishedAt(),
                        flat.getUrl()
                );
                notifier.sendMessage(msg);
            }

        } catch (Exception e) {
            notifier.sendMessage("⚠️ Ошибка при получении объявлений: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //  Данные для Telegram API
    @Override
    public String getBotUsername() {
        return System.getenv("TELEGRAM_BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public String getBotPath() {
        return "/webhook";
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }
}


