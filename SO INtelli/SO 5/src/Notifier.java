import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Notifier {

    public static void notifyAllChats(String text) {
        Logger.log("NOTIFY: " + text);
        for (String chatId : Config.TELEGRAM_CHAT_IDS) {
            sendTelegramMessage(chatId, text);
        }
    }

    private static void sendTelegramMessage(String chatId, String text) {
        try {
            String urlString = "https://api.telegram.org/bot"
                    + Config.TELEGRAM_BOT_TOKEN
                    + "/sendMessage";

            String data = "chat_id=" + URLEncoder.encode(chatId, "UTF-8")
                    + "&text=" + URLEncoder.encode(text, "UTF-8");

            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Content-Length",
                    String.valueOf(postData.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
            }

            int responseCode = conn.getResponseCode();
            Logger.log("Telegram response code: " + responseCode + " for chat " + chatId);
        } catch (Exception e) {
            Logger.log("Error sending Telegram message: " + e.getMessage());
        }
    }
}
