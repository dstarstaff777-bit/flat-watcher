package notifer;

import model.UserSearchCriteria;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import util.Config;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URI;

public class TelegramNotifier {
    private static final String BOT_TOKEN = Config.getProperty("telegram.bot.token");
    private static final String CHAT_ID = Config.getProperty("telegram.chat.id");

    private static final HttpClient client = HttpClient.newHttpClient();


    public static void send(long chatId, String text, String botToken) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String body = "chat_id=" + chatId + "&text=" + encode(text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encode(String text) {
        return text.replace("&", "%26").replace("\n", "%0A").replace(" ", "%20");
    }
}

