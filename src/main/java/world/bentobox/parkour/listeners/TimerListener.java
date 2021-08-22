package world.bentobox.parkour.listeners;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;

/**
 * @author tastybento
 *
 */
public class TimerListener implements Listener {

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static final String START = "Parkour_Start";
    private static final String END = "Parkour_End";
    private Parkour addon;
    private Map<UUID, Long> timers = new HashMap<>();


    /**
     * @param addon
     */
    public TimerListener(Parkour addon) {
        this.addon = addon;
        df2.setRoundingMode(RoundingMode.UP);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStartEndSet(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL)) return;
        if (e.getClickedBlock() == null || !e.getClickedBlock().getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE) || !addon.inWorld(e.getPlayer().getLocation())) {
            return;
        }
        Location l = e.getClickedBlock().getLocation();
        User user = Objects.requireNonNull(User.getInstance(e.getPlayer()));
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && addon.getIslands().userIsOnIsland(e.getClickedBlock().getWorld(), user)) {
            Island island = addon.getIslands().getProtectedIslandAt(l).get();
            Optional<MetaDataValue> metaStart = island.getMetaData(START);
            Optional<MetaDataValue> metaEnd = island.getMetaData(END);
            if (metaStart.filter(mdv -> isLocEquals(l, mdv.asString())).isPresent()) {
                if (!timers.containsKey(e.getPlayer().getUniqueId())) {
                    user.sendRawMessage("Parkour Start!");
                    e.getPlayer().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
                    timers.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
                }
            } else if (metaEnd.filter(mdv -> isLocEquals(l, mdv.asString())).isPresent()) {
                if (timers.containsKey(e.getPlayer().getUniqueId())) {
                    double duration = (System.currentTimeMillis() - timers.get(e.getPlayer().getUniqueId())) / 1000D;
                    user.sendRawMessage("Parkour End!");
                    e.getPlayer().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
                    user.sendRawMessage("You took " + df2.format(duration) + " seconds");
                    timers.remove(e.getPlayer().getUniqueId());
                } else {
                    user.sendRawMessage("You need to start before ending!");
                }


            }
        }
    }

    private boolean isLocEquals(Location l1, String l2) {
        Location l3 = Util.getLocationString(l2);
        return l1.getWorld().equals(l3.getWorld()) && l1.getBlockX() == l3.getBlockX() && l1.getBlockY() == l3.getBlockY() && l1.getBlockZ() == l3.getBlockZ();
    }
}
