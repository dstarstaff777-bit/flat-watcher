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
    }
    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
    /**
     * Отправляет простое текстовое сообщение в Telegram.
     *
     * @param text текст сообщения
     */
    public void sendMessage(String text) {
        if (CHAT_ID == null || BOT_TOKEN == null) {
            System.err.println("Ошибка: CHAT_ID или BOT_TOKEN не заданы!");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(CHAT_ID);
        message.setText(text);

        try {
            execute(message);
            System.out.println("Сообщение отправлено: " + text);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


