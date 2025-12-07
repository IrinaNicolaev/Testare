import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramUpdateListener implements Runnable {

    private volatile boolean running = true;
    private long lastUpdateId = 0;

    @Override
    public void run() {
        Logger.log("TelegramUpdateListener started.");
        while (running) {
            try {
                pollUpdates();
                Thread.sleep(5000); // la 5 secunde
            } catch (Exception e) {
                Logger.log("TelegramUpdateListener error: " + e.getMessage());
            }
        }
    }

    private void pollUpdates() {
        try {
            if (Config.TELEGRAM_BOT_TOKEN.startsWith("8572903939:AAEuHjMbRZkbalvYQ8FBRlgIPRtTsrRLPjY")) {
                return;
            }

            String urlString = "https://api.telegram.org/bot"
                    + Config.TELEGRAM_BOT_TOKEN
                    + "/getUpdates?timeout=5";

            if (lastUpdateId > 0) {
                urlString += "&offset=" + (lastUpdateId + 1);
            }

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }

            String json = sb.toString();
            // Parsare foarte simplificată, fără librărie JSON
            // Căutăm "update_id" și "text"
            String[] parts = json.split("\\{");
            for (String part : parts) {
                if (part.contains("\"update_id\"") && part.contains("\"message\"")) {
                    // update_id
                    int idxId = part.indexOf("\"update_id\"");
                    if (idxId >= 0) {
                        int colon = part.indexOf(":", idxId);
                        int comma = part.indexOf(",", colon);
                        if (colon > 0 && comma > colon) {
                            String idStr = part.substring(colon + 1, comma).trim();
                            try {
                                long updId = Long.parseLong(idStr);
                                lastUpdateId = Math.max(lastUpdateId, updId);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    // text
                    int idxText = part.indexOf("\"text\"");
                    if (idxText >= 0) {
                        int colon = part.indexOf(":", idxText);
                        int quote1 = part.indexOf("\"", colon + 1);
                        int quote2 = part.indexOf("\"", quote1 + 1);
                        if (quote1 > 0 && quote2 > quote1) {
                            String text = part.substring(quote1 + 1, quote2);
                            handleCommand(text);
                        }
                    }
                }
            }

        } catch (Exception e) {
            Logger.log("TelegramUpdateListener poll error: " + e.getMessage());
        }
    }

    private void handleCommand(String text) {
        Logger.log("Received Telegram message: " + text);
        if (Config.UNBLOCK_STEAM_COMMAND.equalsIgnoreCase(text.trim())) {
            Config.steamBlocked = false;
            Notifier.notifyAllChats("Steam has been manually unblocked.");
        }
    }

    public void stop() {
        running = false;
    }
}
