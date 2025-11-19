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
import util.FlatFilter;
import util.SeleniumFetcher;

import java.time.Duration;
import java.util.List;

public class FlatWatcherBot extends TelegramWebhookBot {

    private final String webhookUrl;
    private final String botToken;
    private final String botUsername;
    private final AvitoParser parser;
    private final TelegramNotifier notifier;
    SeleniumFetcher fetcher = new SeleniumFetcher();

    public FlatWatcherBot(String webhookUrl, TelegramNotifier notifier, AvitoParser parser) {
        this.webhookUrl = webhookUrl;
        this.notifier = notifier;
        this.botToken = Config.getProperty("telegram.bot.token");
        this.botUsername = Config.getProperty("telegram.bot.username");
        this.parser = parser;
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

        notifier.sendMessage(chatId, "🔍 Ищу новые объявления за последний час...");

        List<FlatListing> all = parser.fetch("https://www.avito.ru/uzlovaya/kvartiry/prodam?p=1");

        List<FlatListing> fresh = FlatFilter.filterLastHour(all);

        if (fresh.isEmpty()) {
            notifier.sendMessage(chatId, "❌ Новых объявлений за последний час не найдено.");
            return null;
        }

        notifier.sendMessage(chatId, "✅ Найдено новых объявлений: " + fresh.size());

        for (FlatListing f : fresh) {
            notifier.sendMessage(chatId, f.toTelegramMessage());
        }

        return null;
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