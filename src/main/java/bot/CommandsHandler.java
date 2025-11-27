package bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


public class CommandsHandler {


    public static SendMessage handle(Update upd) {
        String chatId = upd.getMessage().getChatId().toString();
        String text = upd.getMessage().getText();


        return switch (text) {
            case "/start" -> new SendMessage(chatId, "Привет! Я ищу новые квартиры на Avito.");
            case "/stats" -> new SendMessage(chatId, "Статистика будет позже.");
            default -> new SendMessage(chatId, "Команда не найдена.");
        };
    }
}