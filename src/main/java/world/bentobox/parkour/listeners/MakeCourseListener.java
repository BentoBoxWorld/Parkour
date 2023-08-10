package world.bentobox.parkour.listeners;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.Parkour;

/**
 * @author tastybento
 *
 */
public class MakeCourseListener extends AbstractListener {

    private static final Material CHECKPOINT = Material.POLISHED_BLACKSTONE_PRESSURE_PLATE;
    private static final Material START_END = Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
    private static final Material WARP_SPOT = Material.WARPED_PRESSURE_PLATE;
    /**
     * @param addon Parkour addon
     */
    public MakeCourseListener(Parkour addon) {
        super(addon);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWarpSet(BlockPlaceEvent e) {
        if (!e.getBlock().getType().equals(WARP_SPOT) || !addon.inWorld(e.getBlock().getLocation())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        User user = User.getInstance(e.getPlayer());
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && addon.getIslands().userIsOnIsland(e.getBlock().getWorld(), user)) {
            Island island = addon.getIslands().getProtectedIslandAt(l).get();
            Optional<Location> start = addon.getParkourManager().getStart(island);
            Optional<Location> end = addon.getParkourManager().getEnd(island);
            if (start.isEmpty()) {
                user.notify("parkour.no-start-yet");
                return;
            } else if (end.isEmpty()) {
                user.notify("parkour.no-end-yet");
                return;
            }
            Optional<Location> warpSpot = addon.getParkourManager().getWarpSpot(island);
            if (warpSpot.isEmpty()) {
                user.notify("parkour.warp.set");
            } else {
                user.notify("parkour.warp.replaced");
            }
            // shift from block to player location
            addon.getParkourManager().setWarpSpot(island, l.add(0.5,0,0.5));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStartEndSet(BlockPlaceEvent e) {
        if (!e.getBlock().getType().equals(START_END) || !addon.inWorld(e.getBlock().getLocation())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        User user = User.getInstance(e.getPlayer());
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && addon.getIslands().userIsOnIsland(e.getBlock().getWorld(), user)) {
            Island island = addon.getIslands().getProtectedIslandAt(l).get();
            Optional<Location> start = addon.getParkourManager().getStart(island);
            Optional<Location> end = addon.getParkourManager().getEnd(island);
            if (start.isEmpty()) {
                user.notify("parkour.start-set");
                addon.getParkourManager().setStart(island, l);
            } else if (end.isEmpty()) {
                user.notify("parkour.end-set");
                addon.getParkourManager().setEnd(island, l);
            } else {
                user.notify("parkour.already-set");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCheckPointPlaced(BlockPlaceEvent e) {
        if (!e.getBlock().getType().equals(CHECKPOINT) || !addon.inWorld(e.getBlock().getLocation())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        User user = User.getInstance(e.getPlayer());
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && addon.getIslands().userIsOnIsland(e.getBlock().getWorld(), user)) {
            user.notify("parkour.checkpoint-set");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreak(BlockBreakEvent e) {
        if ((!e.getBlock().getType().equals(START_END) && !e.getBlock().getType().equals(WARP_SPOT))
                || !addon.inWorld(e.getBlock().getLocation())) {
            return;
        }
        Location l = e.getBlock().getLocation();
        User user = User.getInstance(e.getPlayer());
        if (addon.getIslands().getProtectedIslandAt(l).isPresent()
                && addon.getIslands().userIsOnIsland(e.getBlock().getWorld(), user)) {
            Island island = addon.getIslands().getProtectedIslandAt(l).get();
            Optional<Location> start = addon.getParkourManager().getStart(island);
            Optional<Location> end = addon.getParkourManager().getEnd(island);
            Optional<Location> warpSpot = addon.getParkourManager().getWarpSpot(island);
            if (start.filter(mdv -> isLocEquals(l, mdv)).isPresent()) {
                user.notify("parkour.start-removed");
                addon.getParkourManager().setStart(island, null);
            } else if (end.filter(mdv -> isLocEquals(l, mdv)).isPresent()) {
                user.notify("parkour.end-removed");
                addon.getParkourManager().setEnd(island, null);
            } else if (warpSpot.filter(mdv -> isLocEquals(l, mdv)).isPresent()) {
                user.notify("parkour.warp.removed");
                addon.getParkourManager().setWarpSpot(island, null);
            } else if (!e.getBlock().getType().equals(WARP_SPOT)) {
                addon.getParkourManager().setStart(island, null);
                user.notify("parkour.resetting-start-end");
                addon.getParkourManager().setEnd(island, null);
            }
        }
    }

}
