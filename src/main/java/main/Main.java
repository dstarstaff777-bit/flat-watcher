package main;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

import flat_watcher.FlatWatcherBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void startHealthServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0",8080), 0);
            server.createContext("/", exchange -> {
                String response = "Bot is running!";
                byte[] bytes = response.getBytes();
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
            System.out.println("Health server started on port 8080");
        } catch (Exception e) {
            System.out.println("Error starting health server");
        }
    }
    public static void main(String[] args) {

        startHealthServer();

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
