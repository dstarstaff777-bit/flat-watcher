package main;


import com.sun.net.httpserver.HttpServer;
import flat_watcher.FlatWatcherBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


public class Main {

    public static void main(String[] args) {

        try {
            // 1️ Запускаем health server (Render требует открытый порт)
            startHealthServer();

            // 2️ Получаем URL сервиса Render
            String baseUrl = System.getenv("RENDER_EXTERNAL_URL");
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "https://flat-watcher.onrender.com"; // fallback
            }

            String webhookUrl = baseUrl + "/webhook";
            System.out.println("Webhook URL: " + webhookUrl);

            // 3️ Создаём webhook
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(webhookUrl)
                    .build();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // 4️ Регистрируем бота
            FlatWatcherBot bot = new FlatWatcherBot(webhookUrl);
            botsApi.registerBot(bot, setWebhook);

            System.out.println("✅ Бот успешно запущен и webhook активен!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  Мини-сервер для Render (чтобы приложение не «усыплялось»)
    private static void startHealthServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", exchange -> {
                String response = "Service is running!";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            });

            //  создаём маршрут /webhook, чтобы Telegram мог достучаться
            server.createContext("/webhook", exchange -> {
                if ("POST".equals(exchange.getRequestMethod())) {
                    System.out.println("📩 Получено обновление от Telegram");
                }
                String response = "Webhook received";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            server.start();
            System.out.println("🌍 Health server запущен на порту 8080");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
