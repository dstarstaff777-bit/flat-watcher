package flat_watcher;

import model.FlatListing;
import model.UserSearchCriteria;
import notifer.TelegramNotifier;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.AvitoParser;
import util.Config;
import util.SeleniumFetcher;

import java.time.Duration;
import java.util.List;

public class FlatWatcherBot extends TelegramWebhookBot {

    private final String webhookUrl;
    private final String botToken;
    private final String botUsername;
    private final AvitoParser parser;
    SeleniumFetcher fetcher = new SeleniumFetcher();

    public FlatWatcherBot(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.botToken = Config.getProperty("telegram.bot.token");
        this.botUsername = Config.getProperty("telegram.bot.username");
        this.parser = new AvitoParser(fetcher);
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

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }

        if (update.getMessage().getFrom().getIsBot()) {
            return null; // не реагируем на собственные сообщения
        }

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        System.out.println("📩 Команда: " + text);

        return switch (text) {
            case "/start" -> send(chatId,
                    "👋 Привет!\n" +
                            "Я отслеживаю новые объявления с Avito.\n" +
                            "Используй команду /find чтобы получить новые объявления за последний час.");

            case "/find" -> handleFind(chatId);

            default -> send(chatId, "Неизвестная команда 😕");
        };
    }

    /**
     * Обработчик команды /find
     */
    private BotApiMethod<?> handleFind(long chatId) {

        // Первое сообщение — вернуть сразу
        SendMessage searching = SendMessage.builder()
                .chatId(chatId)
                .text("🔍 Ищу новые объявления за последний час...")
                .build();

        // Но отправить его нужно "изнутри", потому что возвращать можно только одно
        try {
            execute(searching);
        } catch (Exception ignored) {}

        // Основной парсинг Avito
        List<FlatListing> listings = parser.fetch(
                "https://www.avito.ru/uzlovaya/kvartiry/prodam?p=1"
        );

        if (listings.isEmpty()) {
            return send(chatId, "❌ Новых объявлений за последний час не найдено.");
        }

        // Сбор ответа в один большой текст
        StringBuilder sb = new StringBuilder();
        sb.append("✨ Найдено объявлений: ").append(listings.size()).append("\n\n");

        for (FlatListing flat : listings) {
            sb.append(flat.toTelegramMessage()).append("\n\n");
        }

        return SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(sb.toString())
                .build();
    }

    /**
     * Вспомогательный метод отправки
     */
    private SendMessage send(long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }
}