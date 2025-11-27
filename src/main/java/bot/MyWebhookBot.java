package bot;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;


public class MyWebhookBot extends TelegramWebhookBot {


    private final String token;
    private final String path;
    private final String username;


    public MyWebhookBot(String token, String path, String username) {
        this.token = token;
        this.path = path;
        this.username = username;
    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.getMessage() != null && update.getMessage().hasText()) {
            return CommandsHandler.handle(update);
        }
        return null;
    }


    @Override public String getBotPath() { return path; }
    @Override public String getBotUsername() { return username; }
    @Override public String getBotToken() { return token; }
}