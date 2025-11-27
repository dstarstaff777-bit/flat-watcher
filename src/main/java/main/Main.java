package main;


import bot.MyWebhookBot;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import parser.AvitoParser;
import scheduler.ParserWorker;
import com.sun.net.httpserver.HttpServer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) throws Exception {

        String token = System.getenv("BOT_TOKEN");
        String username = System.getenv("BOT_USERNAME");
        String webhookUrl = System.getenv("WEBHOOK_URL");   // например https://myapp.onrender.com/webhook
        String webhookPath = "/webhook";

        // 1. Создаем API
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

        // 2. Бот
        MyWebhookBot bot = new MyWebhookBot(token, username, webhookPath);

        // 3. Регистрируем webhook
        api.registerBot(bot, SetWebhook.builder().url(webhookUrl).build());

        // 4. Поднимаем HTTP сервер
        startWebhookServer(bot, 8080, webhookPath);

        // 5. Парсер
        AvitoParser parser = new AvitoParser("https://www.avito.ru/moskva/kvartiry/prodam");

        // 6. Scheduler
        ParserWorker worker = new ParserWorker(parser, bot, 5);
        worker.start();

        System.out.println("SERVER STARTED");
    }

    private static void startWebhookServer(MyWebhookBot bot, int port, String path) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext(path, exchange -> {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());

                ObjectMapper mapper = new ObjectMapper();
                Update update = mapper.readValue(body, Update.class);

                bot.onWebhookUpdateReceived(update);

                String response = "OK";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        server.start();
        System.out.println("[Webhook] Running on port " + port);
    }
}