package world.bentobox.parkour.listeners;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;

/**
 * @author tastybento
 *
 */
public class MakeTrackListener implements Listener {

    private static final String START = "Parkour_Start";
    private static final String END = "Parkour_End";
    private Parkour addon;



    /**
     * @param addon
     */
    public MakeTrackListener(Parkour addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStartEndSet(BlockPlaceEvent e) {
        if (!e.getBlock().getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE) || !addon.inWorld(e.getBlock().getLocation())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        User user = Objects.requireNonNull(User.getInstance(e.getPlayer()));
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && addon.getIslands().userIsOnIsland(e.getBlock().getWorld(), user)) {
            Island island = addon.getIslands().getProtectedIslandAt(l).get();
            Optional<MetaDataValue> metaStart = island.getMetaData(START);
            Optional<MetaDataValue> metaEnd = island.getMetaData(END);
            if (metaStart.isEmpty()) {
                user.sendRawMessage("Start set!");
                island.putMetaData(START, new MetaDataValue(Util.getStringLocation(e.getBlock().getLocation())));
            } else if (metaEnd.isEmpty()) {
                user.sendRawMessage("End set!");
                island.putMetaData(END, new MetaDataValue(Util.getStringLocation(e.getBlock().getLocation())));
            } else {
                user.sendRawMessage("Start and End already set! Break one to reset.");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreak(BlockBreakEvent e) {
        if (!e.getBlock().getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE) || !addon.inWorld(e.getBlock().getLocation())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        User user = Objects.requireNonNull(User.getInstance(e.getPlayer()));
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && addon.getIslands().userIsOnIsland(e.getBlock().getWorld(), user)) {
            Island island = addon.getIslands().getProtectedIslandAt(l).get();
            Optional<MetaDataValue> metaStart = island.getMetaData(START);
            Optional<MetaDataValue> metaEnd = island.getMetaData(END);
            if (metaStart.filter(mdv -> isLocEquals(l, mdv.asString())).isPresent()) {
                user.sendRawMessage("Start removed!");
                island.removeMetaData(START);
            } else if (metaEnd.filter(mdv -> isLocEquals(l, mdv.asString())).isPresent()) {
                user.sendRawMessage("End removed!");
                island.removeMetaData(END);
            } else {
                island.removeMetaData(START);
                user.sendRawMessage("Resetting start and end points!");
                island.removeMetaData(END);
            }
        }
    }

    private boolean isLocEquals(Location l1, String l2) {
        Location l3 = Util.getLocationString(l2);
        return l1.getWorld().equals(l3.getWorld()) && l1.getBlockX() == l3.getBlockX() && l1.getBlockY() == l3.getBlockY() && l1.getBlockZ() == l3.getBlockZ();
    }
}
