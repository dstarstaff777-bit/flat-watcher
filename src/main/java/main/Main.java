package main;

import com.github.dockerjava.api.model.HealthCheck;
import flat_watcher.FlatWatcherBot;
import org.apache.http.impl.bootstrap.HttpServer;
import org.openqa.selenium.remote.http.HttpHandler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) {

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
