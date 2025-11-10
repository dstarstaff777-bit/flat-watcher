package main;


import com.sun.net.httpserver.HttpServer;
import flat_watcher.FlatWatcherBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;



public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        log.info("Запуск бота...");
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
            log.error("Ошибка при запуске бота: ", e);
        }
    }

    //  Мини-сервер для Render (чтобы приложение не «усыплялось»)
    private static void startHealthServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/webhook", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("Получено обновление от Telegram: " + body);

            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("OK".getBytes());
            }
        });

        server.start();
        System.out.println("🩺 Health server запущен на порту 8080");
    }
}
