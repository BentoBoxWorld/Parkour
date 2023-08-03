package world.bentobox.parkour;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.objects.ParkourData;

public class ParkourManager {

    // Database handler for level data
    private final Database<ParkourData> handler;
    /**
     * A cache of high scores. Key is island UUID
     */
    private final Map<String, ParkourData> cache;
    private Parkour addon;

    /**
     * Handles storing a retrieval of score data for islands
     * @param addon Parkour addon
     */
    public ParkourManager(Parkour addon) {
        this.addon = addon;
        // Get the BentoBox database
        // Set up the database handler to store and retrieve data
        // Note that these are saved by the BentoBox database
        handler = new Database<>(addon, ParkourData.class);
        // Initialize the cache
        cache = new ConcurrentHashMap<>();
        // Load all
        handler.loadObjects().forEach(hs -> cache.put(hs.getUniqueId(), hs));
    }

    private ParkourData getIsland(Island island) {
        if (!addon.inWorld(island.getWorld())) {
            throw new IllegalArgumentException("Island is not in Parkour world: " + island.getWorld());
        }
        return cache.computeIfAbsent(island.getUniqueId(), k -> getFromDb(island));
    }

    private ParkourData getFromDb(Island island) {
        if (handler.objectExists(island.getUniqueId())) {
            ParkourData hs = handler.loadObject(island.getUniqueId());
            if (hs != null) {
                return hs;
            }
        }
        return new ParkourData(island.getUniqueId());
    }

    private void saveIsland(Island island) {
        handler.saveObjectAsync(getIsland(island));
    }

    /**
     * Add a score for a player to this island
     * @param island island
     * @param user player
     * @param time time in milliseconds
     */
    public void addScore(Island island, User user, long time) {
        ParkourData h = getIsland(island);
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
        return (int) stream.takeWhile(x -> !x.getKey().equals(uuid)).map(Entry::getKey).count() + 1;
    }

    /**
     * Get the time previously take by this player
     * @param island island
     * @param uniqueId player's UUID
     * @return time or 0L if never down
     */
    public long getTime(Island island, UUID uniqueId) {
        return getIsland(island).getRankings().getOrDefault(uniqueId, 0L);
    }

    /**
     * @return the HighScores
     */
    public Collection<ParkourData> getParkourData() {
        return cache.values();
    }

    public Optional<Location> getStart(Island island) {
        return Optional.ofNullable(getIsland(island).getStart());
    }

    public Optional<Location> getEnd(Island island) {
        return Optional.ofNullable(getIsland(island).getEnd());
    }

    public Optional<Location> getWarpSpot(Island island) {
        return Optional.ofNullable(getIsland(island).getWarpSpot());
    }

    public void setStart(Island island, Location location) {
        getIsland(island).setStart(location);
        // Save every time right now
        saveIsland(island);
    }

    public void setEnd(Island island, Location location) {
        getIsland(island).setEnd(location);
        // Save every time right now
        saveIsland(island);
    }

    public void setWarpSpot(Island island, Location location) {
        getIsland(island).setWarpSpot(location);
        // Save every time right now
        saveIsland(island);
    }

    /**
     * Get a map of warps to courses
     * @return map with the key being the name of the island owner and value being the warp location
     */
    public Map<String, Location> getWarps() {
        Map<String, Location> map = new HashMap<>();
        for (ParkourData pd : getParkourData()) {
            if (pd.getWarpSpot() == null) continue;
            UUID owner = addon.getIslands().getIslandById(pd.getUniqueId()).map(Island::getOwner).orElse(null);
            if (owner != null) {
                String name = addon.getPlayers().getName(owner);
                map.put(name, pd.getWarpSpot());
            }
        }
        return map;
    }
}
