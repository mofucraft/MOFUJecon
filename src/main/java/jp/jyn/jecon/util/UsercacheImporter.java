package jp.jyn.jecon.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jp.jyn.jecon.db.Database;

import java.io.File;
import java.io.FileReader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to import player names from usercache.json into the database
 */
public class UsercacheImporter {
    private static final Logger LOGGER = Logger.getLogger(UsercacheImporter.class.getName());

    /**
     * Import all player names from usercache.json into database
     * This is useful for populating names for existing accounts after migration
     *
     * @param db Database instance
     * @return Number of names imported
     */
    public static int importFromUsercache(Database db) {
        int imported = 0;

        try {
            File usercacheFile = new File("usercache.json");
            if (!usercacheFile.exists()) {
                LOGGER.info("usercache.json not found, skipping player name import");
                return 0;
            }

            LOGGER.info("Importing player names from usercache.json...");

            FileReader reader = new FileReader(usercacheFile);
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            reader.close();

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

                    // Update player name in database
                    db.updatePlayerName(uuid, name);
                    imported++;
                } catch (Exception e) {
                    // Skip invalid entries
                    continue;
                }
            }

            LOGGER.info("Imported " + imported + " player names from usercache.json");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to import from usercache.json", e);
        }

        return imported;
    }
}
