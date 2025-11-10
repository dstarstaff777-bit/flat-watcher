package main;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import flat_watcher.FlatWatcherBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;




public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {

        String baseUrl = System.getenv("RENDER_EXTERNAL_URL");
        if (baseUrl == null) {
            throw new RuntimeException("RENDER_EXTERNAL_URL not set");
        }

        String webhookUrl = baseUrl + "/webhook";

        FlatWatcherBot bot = new FlatWatcherBot(webhookUrl);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot, SetWebhook.builder().url(webhookUrl).build());

        startWebhookServer(bot);

        System.out.println("✅ Webhook registered: " + webhookUrl);
    }

    public static void startWebhookServer(FlatWatcherBot bot) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/webhook", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String json = new String(exchange.getRequestBody().readAllBytes());
                System.out.println("📨 Update received: " + json);

                Update update = new ObjectMapper().readValue(json, Update.class);
                BotApiMethod<?> response = bot.onWebhookUpdateReceived(update);

                if (response != null) {
                    if (response instanceof SendMessage sendMessage) {
                        try {
                            bot.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("OK".getBytes());
            exchange.getResponseBody().close();
        });

        server.start();
        System.out.println("🌍 Local Webhook server running on port 8080");
    }
}
