package notifer;

import model.UserSearchCriteria;

import util.Config;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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