package jp.jyn.jecon.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to resolve player names from UUIDs
 * Works with Minecraft 1.21.4+ where OfflinePlayer.getName() may return null
 */
public class PlayerNameResolver {
    private static final Logger LOGGER = Logger.getLogger(PlayerNameResolver.class.getName());
    private static Map<UUID, String> nameCache = null;
    private static long lastCacheUpdate = 0;
    private static final long CACHE_EXPIRY = 60000; // 1 minute

    /**
     * Get player name from UUID using multiple fallback methods
     *
     * @param uuid Player UUID
     * @return Player name or UUID string if name cannot be resolved
     */
    public static String getPlayerName(UUID uuid) {
        // Method 1: Try getName() first (works in older versions)
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String name = player.getName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        } catch (Exception e) {
            // Ignore and try next method
        }

        // Method 2: Try PlayerProfile API (Minecraft 1.21.4+) using reflection
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Method getPlayerProfile = player.getClass().getMethod("getPlayerProfile");
            Object profile = getPlayerProfile.invoke(player);
            Method getName = profile.getClass().getMethod("getName");
            String name = (String) getName.invoke(profile);
            if (name != null && !name.isEmpty()) {
                return name;
            }
        } catch (Exception e) {
            // Ignore and try next method
        }

        // Method 3: Try usercache.json
        String nameFromCache = getNameFromUserCache(uuid);
        if (nameFromCache != null && !nameFromCache.isEmpty()) {
            return nameFromCache;
        }

        // Method 4: Fallback to UUID string
        return uuid.toString();
    }

    /**
     * Read player name from usercache.json
     *
     * @param uuid Player UUID
     * @return Player name or null if not found
     */
    private static String getNameFromUserCache(UUID uuid) {
        try {
            // Refresh cache if expired
            if (nameCache == null || System.currentTimeMillis() - lastCacheUpdate > CACHE_EXPIRY) {
                loadUserCache();
            }

            return nameCache.get(uuid);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to read usercache.json", e);
            return null;
        }
    }

    /**
     * Load all player names from usercache.json into memory
     */
    private static void loadUserCache() {
        nameCache = new HashMap<>();

        try {
            File usercacheFile = new File("usercache.json");
            if (!usercacheFile.exists()) {
                LOGGER.warning("usercache.json not found");
                return;
            }

            // Read file content as string for compatibility
            StringBuilder sb = new StringBuilder();
            FileReader reader = new FileReader(usercacheFile);
            try {
                int c;
                while ((c = reader.read()) != -1) {
                    sb.append((char) c);
                }
            } finally {
                reader.close();
            }

            // Parse JSON with compatibility for different Gson versions
            JsonArray jsonArray;
            try {
                // Try new static API first (Gson 2.8.6+)
                try {
                    Method parseString = JsonParser.class.getMethod("parseString", String.class);
                    JsonElement element = (JsonElement) parseString.invoke(null, sb.toString());
                    jsonArray = element.getAsJsonArray();
                } catch (NoSuchMethodException e) {
                    // Fallback to old API (Gson < 2.8.6) - try instance method
                    try {
                        JsonParser parser = new JsonParser();
                        JsonElement element = parser.parse(sb.toString());
                        jsonArray = element.getAsJsonArray();
                    } catch (Exception e2) {
                        // Last resort: try static parse method
                        Method parse = JsonParser.class.getMethod("parse", String.class);
                        JsonElement element = (JsonElement) parse.invoke(null, sb.toString());
                        jsonArray = element.getAsJsonArray();
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse usercache.json", e);
                return;
            }
            processJsonArray(jsonArray);


            lastCacheUpdate = System.currentTimeMillis();
            LOGGER.info("Loaded " + nameCache.size() + " player names from usercache.json");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load usercache.json", e);
            nameCache = new HashMap<>(); // Use empty cache on error
        }
    }

    /**
     * Process JSON array and populate name cache
     */
    private static void processJsonArray(JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject playerObj = element.getAsJsonObject();

            if (!playerObj.has("uuid") || !playerObj.has("name")) {
                continue;
            }

            try {
                String uuidStr = playerObj.get("uuid").getAsString();
                String name = playerObj.get("name").getAsString();

                UUID uuid = UUID.fromString(uuidStr);
                nameCache.put(uuid, name);
            } catch (Exception e) {
                // Skip invalid entries
                continue;
            }
        }
    }

    /**
     * Clear the name cache to force reload on next request
     */
    public static void clearCache() {
        nameCache = null;
        lastCacheUpdate = 0;
    }
}
