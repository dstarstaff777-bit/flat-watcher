package flat_watcher;

import model.FlatListing;
import notifer.TelegramNotifier;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import parser.AvitoParser;
import util.Config;
import java.util.List;

public class FlatWatcherBot extends TelegramWebhookBot {

    private final TelegramNotifier notifier;
    private final AvitoParser parser;

    public FlatWatcherBot() {
        this.notifier = new TelegramNotifier();
        this.parser = new AvitoParser();
    }

    @Override
    public String getBotUsername() {
        return "FlatWatcherBot"; // Имя твоего бота
    }

    @Override
    public String getBotToken() {
        return Config.getProperty("telegram.bot.token"); // Берётся из config.properties или переменных окружения
    }

    @Override
    public String getBotPath() {
        return "/webhook"; // Путь, по которому Telegram будет присылать обновления
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }

        String message = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        switch (message) {
            case "/start":
                notifier.sendMessage("👋 Привет! Я помогу тебе отслеживать новые квартиры на Avito.\n" +
                        "Используй команду /find чтобы запустить поиск.");
                break;

            case "/find":
                notifier.sendMessage("🔎 Проверяю новые объявления...");
                try {
                    checkNewFlats(chatId);
                } catch (Exception e) {
                    notifier.sendMessage("❌ Ошибка при проверке объявлений: " + e.getMessage());
                    e.printStackTrace();
                }
                break;

            default:
                notifier.sendMessage("⚠️ Неизвестная команда. Попробуй /find.");
        }

        return null;
    }

    /**
     * Проверяет наличие новых объявлений и отправляет результат пользователю.
     */
    public void checkNewFlats(long chatId) {
        try {

            List<FlatListing> listings = parser.fetchListings(
                    "https://www.avito.ru/uzlovaya/kvartiry/prodam",
                    java.time.Duration.ofHours(1)
            );

            if (listings.isEmpty()) {
                notifier.sendMessage("😕 За последний час новых объявлений не найдено.");
            } else {
                notifier.sendMessage("✨ Найдены новые объявления:");

                for (FlatListing flat : listings) {
                    String msg = String.format(
                            "🏠 %s\n💰 %d ₽\n📍 %s\n🕒 %s\n🔗 %s",
                            flat.getTitle(),
                            flat.getPrice(),
                            flat.getDistrict(),
                            flat.getPublishedAt(),
                            flat.getUrl()
                    );
                    notifier.sendMessage(msg);
                }
            }
        } catch (Exception e) {
            notifier.sendMessage("⚠️ Ошибка при парсинге: " + e.getMessage());
            e.printStackTrace();
        }
    }

}


