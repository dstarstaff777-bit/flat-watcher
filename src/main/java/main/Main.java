package main;

import flat_watcher.FlatWatcherBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import util.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) {
        System.out.println("chatId: " + Config.getProperty("telegram.chat.id"));
        System.out.println("token: " + Config.getProperty("telegram.bot.token"));

        System.out.println("Начинаем парсить");
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            FlatWatcherBot bot = new FlatWatcherBot();
            botsApi.registerBot(bot);
            System.out.println("Бот запущен");

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    System.out.println("Проверка новых объявлений....");
                    bot.checkNewFlats();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 60, TimeUnit.MINUTES);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}