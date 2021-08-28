package world.bentobox.parkour.listeners;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
public class MakeCourseListener extends AbstractListener {

    /**
     * @param addon
     */
    public MakeCourseListener(Parkour addon) {
        super(addon);
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
                user.notify("parkour.start-set");
                island.putMetaData(START, new MetaDataValue(Util.getStringLocation(e.getBlock().getLocation())));
            } else if (metaEnd.isEmpty()) {
                user.notify("parkour.end-set");
                island.putMetaData(END, new MetaDataValue(Util.getStringLocation(e.getBlock().getLocation())));
            } else {
                user.notify("parkour.already-set");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCheckPointPlaced(BlockPlaceEvent e) {
        if (!e.getBlock().getType().equals(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE) || !addon.inWorld(e.getBlock().getLocation())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        User user = Objects.requireNonNull(User.getInstance(e.getPlayer()));
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && addon.getIslands().userIsOnIsland(e.getBlock().getWorld(), user)) {
            user.notify("parkour.checkpoint-set");
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
                user.notify("parkour.start-removed");
                island.removeMetaData(START);
            } else if (metaEnd.filter(mdv -> isLocEquals(l, mdv.asString())).isPresent()) {
                user.notify("parkour.end-removed");
                island.removeMetaData(END);
            } else {
                island.removeMetaData(START);
                user.notify("parkour.resetting-start-end");
                island.removeMetaData(END);
            }
        }
    }

}
