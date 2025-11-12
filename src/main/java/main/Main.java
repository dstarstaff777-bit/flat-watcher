package main;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import flat_watcher.FlatWatcherBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.io.IOException;
import java.net.InetSocketAddress;

import java.io.OutputStream;




public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {

        try {
            System.out.println("Запуск бота...");

            String baseUrl = System.getenv("RENDER_EXTERNAL_URL");
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new RuntimeException("Ошибка: переменная среды RENDER_EXTERNAL_URL не установлена");
            }

            String webhookUrl = baseUrl + "/webhook";
            System.out.println("Webhook URL: " + webhookUrl);

            // Создаём бота
            FlatWatcherBot bot = new FlatWatcherBot(webhookUrl);
            // Регистрируем его через API Telegram
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            SetWebhook setWebhook = SetWebhook.builder()
                    .url(webhookUrl)
                    .build();

            botsApi.registerBot(bot, setWebhook);

            // Запускаем веб-сервер на Render
            startWebhookServer(bot);

            System.out.println("✅ Бот успешно запущен и webhook активен!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startWebhookServer(FlatWatcherBot bot) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/webhook", (HttpExchange exchange) -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // Это не запрос от Telegram, а ping от Render — игнорируем.
                System.out.println("📡 Получен внешний ping (" + exchange.getRequestMethod() + ") — пропускаем.");
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            // Читаем тело запроса
            String json = new String(exchange.getRequestBody().readAllBytes()).trim();

            // Если тело пустое — тоже игнорируем
            if (json.isEmpty() || json.equals("{}")) {
                System.out.println("📡 Пустое тело запроса — игнорируем (возможно Render health-check).");
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            System.out.println("📩 Update received: " + json);

            try {
                ObjectMapper mapper = new ObjectMapper();
                var update = mapper.readValue(json, org.telegram.telegrambots.meta.api.objects.Update.class);

                var response = bot.onWebhookUpdateReceived(update);
                if (response != null) bot.execute(response);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
    }
}
