package world.bentobox.parkour;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.objects.HighScores;

public class ParkourManager {

    // Database handler for level data
    private final Database<HighScores> handler;
    // A cache of high scores. Key is island UUID
    private final Map<String, HighScores> cache;

    /**
     * Handles storing a retrieval of score data for islands
     * @param addon Parkour addon
     */
    public ParkourManager(Parkour addon) {
        // Get the BentoBox database
        // Set up the database handler to store and retrieve data
        // Note that these are saved by the BentoBox database
        handler = new Database<>(addon, HighScores.class);
        // Initialize the cache
        cache = new ConcurrentHashMap<>();
    }

    private HighScores getIsland(Island island) {
        return cache.computeIfAbsent(island.getUniqueId(), k -> getFromDb(island));
    }

    private HighScores getFromDb(Island island) {
        if (handler.objectExists(island.getUniqueId())) {
            HighScores hs = handler.loadObject(island.getUniqueId());
            if (hs != null) {
                return hs;
            }
        }
        return new HighScores(island.getUniqueId());
    }

    private void saveIsland(Island island) {
        handler.saveObjectAsync(getFromDb(island));
    }

    /**
     * Add a score for a player to this island
     * @param island island
     * @param user player
     * @param time time in milliseconds
     */
    public void addScore(Island island, User user, long time) {
        HighScores h = getIsland(island);
        h.getRankings().put(user.getUniqueId(), time);
        h.setRunCount(h.getRunCount() + 1);
        // Save every time right now
        saveIsland(island);
    }

    /**
     * Clear all island scores and delete island from database
     * @param island island
     */
    public void clearScores(Island island) {
        getIsland(island).getRankings().clear();
        // Delete island from database
        handler.deleteID(island.getUniqueId());
    }

    /**
     * Remove a score for a specific player
     * @param island island
     * @param user player
     */
    public void removeScore(Island island, User user) {
        getIsland(island).getRankings().remove(user.getUniqueId());
        // Save every time right now
        saveIsland(island);
    }

    /**
     * Get the rankings for island up to size
     * @param island island
     * @param size max size of rankings
     * @return map of player UUID to time for this island
     */
    public Map<UUID, Long> getRankings(Island island, long size) {
        return Collections.unmodifiableMap(getIsland(island).getRankings().entrySet().stream()
                .filter(e -> e.getValue() > 0) // Zero or less is illegal
                .sorted(Map.Entry.comparingByValue()).limit(size)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));
    }

    /**
     * Get the rank of the player for this island
     * @param island island
     * @param uuid player UUID
     * @return rank placing - note - placing of 1 means top ranked
     */
    public int getRank(Island island, UUID uuid) {
        Stream<Entry<UUID, Long>> stream = getIsland(island).getRankings().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.comparingByValue());
        return stream.takeWhile(x -> !x.getKey().equals(uuid)).map(Map.Entry::getKey).collect(Collectors.toList()).size() + 1;
    }

    public long getTime(Island island, UUID uniqueId) {
        return getIsland(island).getRankings().getOrDefault(uniqueId, 0L);
    }
}
