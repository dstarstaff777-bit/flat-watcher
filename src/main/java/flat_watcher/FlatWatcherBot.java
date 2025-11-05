package flat_watcher;

import model.FlatListing;
import notifer.TelegramNotifier;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import parser.AvitoParser;

import java.time.Duration;
import java.util.ArrayList;
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            System.out.println("Пришло сообщение: " + text);

            if (text.equalsIgnoreCase("/find")) {
                checkNewFlats(chatId);
                return new SendMessage(chatId.toString(), "Проверяю новые объявления за последние 60 минут...");
            };
            return new SendMessage(chatId.toString(), "Привет используй команду /find для поиска новых объявлений " );
        }
        return null;
    }

    //  Проверка новых объявлений
    public void checkNewFlats(Long chatId) {
        try {
            System.out.println("Начинаем проверку новых обьявлений");

            String searchUrl = "https://avito.ru/uzlovaya/kvartiry/prodam";
            Duration maxAge = Duration.ofHours(1);

            List <FlatListing> listings = parser.fetchListings(searchUrl, maxAge);

            if (listings.isEmpty()) {
                notifier.sendMessage(chatId,"🕐 За последние 60 минут новых объявлений не найдено.");
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
                notifier.sendMessage(chatId, msg);
            }
            System.out.println("Проверка завершена, отправлено " + listings.size() + " обьявлений.");

        } catch (Exception e) {
            notifier.sendMessage(chatId, "⚠️ Ошибка при получении объявлений: " + e.getMessage());
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


