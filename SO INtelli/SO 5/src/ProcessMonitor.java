import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ProcessMonitor implements Runnable {

    private final Map<String, Instant> firstSeen = new HashMap<>();

    @Override
    public void run() {
        Logger.log("ProcessMonitor started.");
        while (true) {
            try {
                scanProcesses();
                Thread.sleep(10_000); // verificare la fiecare 10 secunde pentru reacție rapidă
            } catch (Exception e) {
                Logger.log("ProcessMonitor error: " + e.getMessage());
            }
        }
    }

    private void scanProcesses() {
        try {
            Process process = new ProcessBuilder("tasklist.exe", "/fo", "csv", "/nh").start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                Instant now = Instant.now();

                while ((line = br.readLine()) != null) {
                    String[] cols = parseCsvLine(line);
                    if (cols.length < 2) continue;
                    String imageName = cols[0].toLowerCase();
                    String pid = cols[1];

                    if (isBlockedProcess(imageName)) {
                        String key = imageName + "#" + pid;

                        // Dacă este prima dată când detectăm procesul
                        firstSeen.putIfAbsent(key, now);

                        // Calculăm cât timp a trecut de la detectare
                        long aliveMs = now.toEpochMilli() - firstSeen.get(key).toEpochMilli();

                        // Dacă a trecut mai mult de BLOCK_LIMIT_MS, îl închidem
                        if (aliveMs >= Config.BLOCK_LIMIT_MS) {
                            killProcess(imageName, pid);
                            firstSeen.remove(key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.log("scanProcesses error: " + e.getMessage());
        }
    }


    private boolean isBlockedProcess(String imageNameLower) {
        for (String kw : Config.BLOCKED_KEYWORDS) {
            if (imageNameLower.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void killProcess(String imageName, String pid) {
        try {
            Logger.log("Trying to kill process: " + imageName + " PID=" + pid);

            // Folosește /IM + /T pentru a închide toate procesele copil
            Process p = new ProcessBuilder("taskkill", "/IM", imageName, "/F", "/T").start();
            p.waitFor();

            Logger.log("Process killed: " + imageName + " PID=" + pid);

            // --- NOTIFICARE TELEGRAM către toate chat-id-urile ---
            for (String chatId : Config.TELEGRAM_CHAT_IDS) {
                try {
                    String urlString = "https://api.telegram.org/bot"
                            + Config.TELEGRAM_BOT_TOKEN
                            + "/sendMessage";

                    String data = "chat_id=" + URLEncoder.encode(chatId, "UTF-8")
                            + "&text=" + URLEncoder.encode("Blocked process killed: " + imageName + " PID=" + pid, "UTF-8");

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
                    Logger.log("Error sending Telegram message to chat " + chatId + ": " + e.getMessage());
                }
            }
            // --- sfârșit notificare ---

        } catch (Exception e) {
            Logger.log("Error killing process " + pid + ": " + e.getMessage());
        }
    }

    private String[] parseCsvLine(String line) {
        line = line.trim();
        if (line.startsWith("\"") && line.endsWith("\"")) {
            line = line.substring(1, line.length() - 1);
        }
        return line.split("\",\"");
    }
}
