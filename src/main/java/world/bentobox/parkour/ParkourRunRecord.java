package world.bentobox.parkour;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

public record ParkourRunRecord(Map<UUID, Location> checkpoints, Map<UUID, Long> timers, List<UUID> currentlyTeleporting) {
    /**
     * Clears any current times or checkpoints
     * @param uuid UUID of runner
     */
    public void clear(UUID uuid) {
        checkpoints.remove(uuid);
        timers.remove(uuid);
        currentlyTeleporting.remove(uuid);
    }

}
