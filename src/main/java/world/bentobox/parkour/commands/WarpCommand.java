package world.bentobox.parkour.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;

/**
 * Warps to a
 * @author tastybento
 *
 */
public class WarpCommand extends CompositeCommand {


    private Location warpSpot;

    public WarpCommand(CompositeCommand parent) {
        super(parent, "warp");
    }

    @Override
    public void setup() {
        this.setPermission("warp");
        setOnlyPlayer(true);
        setDescription("parkour.commands.parkour.warp.description");
        this.setParametersHelp("parkour.commands.parkour.warp.parameters");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        ParkourManager pm = ((Parkour) getAddon()).getParkourManager();
        if (args.size() > 1) {
            this.showHelp(this, user);
            return false;
        }
        if (args.isEmpty()) {
            Optional<Island> island = getIslands().getIslandAt(user.getLocation());
            if (island.isEmpty() || !((Parkour)getAddon()).inWorld(user.getWorld())) {
                user.sendMessage("parkour.errors.not-on-island");
                this.showHelp(this, user);
                return false;
            } else {
                warpSpot = pm.getWarpSpot(island.get()).orElse(null);
                if (warpSpot == null) {
                    user.sendMessage("parkour.warp.no-warp");
                    return false;
                }
                return true;
            }
        }
        // Check name to warp
        // Get the warp spot
        Map<String, Location> warps = pm.getWarps();
        String target = args.get(0).trim();
        warpSpot = warps.entrySet().stream().filter(e -> e.getKey().toLowerCase().startsWith(target.toLowerCase())).findFirst().map(Map.Entry::getValue)
                .orElse(null);
        if (warpSpot == null) {
            // Unknown warp
            user.sendMessage("parkour.warp.unknown-course");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        user.sendMessage("parkour.warp.warping");
        // Teleport user
        user.getPlayer().playSound(user.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
        user.getPlayer().playSound(warpSpot, Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
        Util.teleportAsync(user.getPlayer(), warpSpot.clone().add(new Vector(0.5, 0.5, 0.5)), TeleportCause.COMMAND);
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        ArrayList<String> options = new ArrayList<>(((Parkour)getAddon()).getParkourManager().getWarps().keySet());
        if (options.size() < 10) {
            return Optional.of(options);
        }
        // List is too long; require at least the first letter
        String lastArg = !args.isEmpty() ? args.get(args.size()-1) : "";
        if (args.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.tabLimit(options, lastArg));
    }

}
