package notifer;

import model.UserSearchCriteria;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import util.Config;


public class TelegramNotifier extends DefaultAbsSender {
    private static final String BOT_TOKEN = Config.getProperty("telegram.bot.token");
    private static final String CHAT_ID = Config.getProperty("telegram.chat.id");

    public TelegramNotifier() {
        super(new DefaultBotOptions());

        // Получаем данные из системных переменных Render

        if (BOT_TOKEN == null || CHAT_ID == null) {
            System.err.println(" Ошибка: TELEGRAM_BOT_TOKEN или TELEGRAM_CHAT_ID не заданы!");
        }
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    /**
     * Отправляет простое текстовое сообщение в Telegram.
     */
    public void sendMessage(String text) {
        if (BOT_TOKEN == null || CHAT_ID== null) {
            System.err.println("Не могу отправить сообщение: не заданы TELEGRAM_BOT_TOKEN или TELEGRAM_CHAT_ID");
            return;
        }

        try {
            SendMessage message = new SendMessage(CHAT_ID, text);
            execute(message);
            System.out.println("Сообщение отправлено: " + text);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки в Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


