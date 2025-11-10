package flat_watcher;

import model.FlatListing;
import model.UserSearchCriteria;
import notifer.TelegramNotifier;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import parser.AvitoParser;
import util.Config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FlatWatcherBot extends TelegramWebhookBot {

    private final String webhookUrl;
    private final String botToken;
    private final String botUsername;
    private final UserSearchCriteria criteria;
    private final TelegramNotifier notifier;
    private final AvitoParser parser;

    public FlatWatcherBot(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.botToken = Config.getProperty("telegram.bot.token");
        this.botUsername = Config.getProperty("telegram.bot.username");

        this.criteria = new UserSearchCriteria();
        this.notifier = new TelegramNotifier();
        this.parser = new AvitoParser();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return "webhook";
    }

    /**
     * Обработка входящих сообщений
     */
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText().trim();

            switch (text) {
                case "/start" -> {
                    return send(chatId,
                            "👋 Привет! Я отслеживаю новые объявления на Avito.\n" +
                                    "Используй команду /find чтобы найти новые предложения за последний час.");
                }
                case "/find" -> {
                    return handleFind(chatId);
                }
                default -> {
                    return send(chatId, "Неизвестная команда.");
                }
            }
        }
        return null;
    }

    /**
     * Проверка новых объявлений за последний час
     */
    private BotApiMethod<?> handleFind(long chatId) {
        notifier.sendMessage(chatId, "🔍 Ищу новые объявления за последний час...");

        List<FlatListing> listings = parser.fetchListings(criteria.getBaseUrl(), Duration.ofMinutes(60));

        if (listings.isEmpty()) {
            notifier.sendMessage(chatId, "❌ Новых объявлений за последний час не найдено.");
            return null;
        }

        notifier.sendMessage(chatId, "✅ Найдено объявлений: " + listings.size());

        for (FlatListing flat : listings) {
            notifier.sendMessage(chatId, flat.toTelegramMessage());
        }

        return null;
    }

    private SendMessage send(long chatId, String text) {
        return SendMessage.builder().chatId(chatId).text(text).build();
    }
}

