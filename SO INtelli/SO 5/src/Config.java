public class Config {
    // === PROCese interzise (conțin acest text în nume) ===
    public static final String[] BLOCKED_KEYWORDS = {
            "steam",
            "chrome",
            "epic",
            "valorant",
            "game"
    };


    // Limita de timp în milisecunde (30 minute)
    public static final long BLOCK_LIMIT_MS = 30*1000;

    // Folder monitorizat (punctul 20)
    public static final String MONITORED_FOLDER = "C:\\Users\\danie\\AppData\\Roaming";

    // Log file
    public static final String LOG_FILE = "app.log";

    // === Telegram ===
    // TODO: pune aici token-ul primit de la BotFather, de forma: 123456789:ABC....
    public static final String TELEGRAM_BOT_TOKEN = "8572903939:AAEuHjMbRZkbalvYQ8FBRlgIPRtTsrRLPjY";
    // TODO: pune aici chat id-ul unde vrei să trimiți mesajele (ex. ID-ul tău)
    public static final String[] TELEGRAM_CHAT_IDS = {
            "884060708",
            "6958201023"
    };


    // Comandă text pentru deblocare Steam (trimisă prin Telegram)
    public static final String UNBLOCK_STEAM_COMMAND = "/unblock_steam";

    // Dacă e true, Steam este blocat; dacă devine false, nu mai închidem Steam
    public static volatile boolean steamBlocked = true;
}
