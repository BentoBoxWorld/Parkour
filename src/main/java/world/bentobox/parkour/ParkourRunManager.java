package world.bentobox.parkour;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;

public class ParkourRunManager {

  Parkour addon;

  public ParkourRunManager(Parkour addon) {
    this.addon = addon;
  }

  private final Map<UUID, Location> checkpoints = new HashMap<>();
  private final Map<UUID, Long> timers = new HashMap<>();

  public Map<UUID, Location> getCheckpoints() {
    return checkpoints;
  }

  public Map<UUID, Long> getTimers() {
    return timers;
  }

  public void clear(UUID uuid) {
    checkpoints.remove(uuid);
    timers.remove(uuid);
  }


}
