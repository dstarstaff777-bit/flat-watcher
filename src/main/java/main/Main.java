package main;


import flat_watcher.FlatWatcherBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook;


public class Main {

    public static void main(String[] args) {

        try {
            // Запускаем health server (чтобы Render не засыпал)
            startHealthServer();

            String baseUrl = System.getenv("RENDER_EXTERNAL_URL");
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "flat-watcher.onrender.com";
            }
            String webhookUrl = baseUrl + "/webhook";
            System.out.println("Webhook URL: " + webhookUrl );

            FlatWatcherBot bot = new FlatWatcherBot();
            DefaultWebhook defaultWebhook = new DefaultWebhook();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class, defaultWebhook);

            SetWebhook setWebhook = SetWebhook.builder()
                    .url(webhookUrl)
                    .build();

            botsApi.registerBot(bot, setWebhook);
            System.out.println("Webhook установлен: " + webhookUrl);
        } catch (
                TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // простой healthcheck сервер (Render требует открытый порт)
    private static void startHealthServer() {
        try {
            com.sun.net.httpserver.HttpServer server =
                    com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8080), 0);
            server.createContext("/", exchange -> {
                String resp = "OK";
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(resp.getBytes());
                }
            });
            server.start();
            System.out.println("Health сервер запущен на порту 8080");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}