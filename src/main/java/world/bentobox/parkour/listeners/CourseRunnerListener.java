package world.bentobox.parkour.listeners;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourRunRecord;

/**
 * Handles the mechanics of the race.
 *
 * @author tastybento
 */
public class CourseRunnerListener extends AbstractListener {

    ParkourRunRecord parkourRunManager;

    /**
     * @param addon Parkour addon
     */
    public CourseRunnerListener(Parkour addon) {
        super(addon);
        this.parkourRunManager = addon.getParkourRunRecord();
    }

    /**
     * Handle arriving visitors
     *
     * @param e IslandEnterEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorArrive(IslandEnterEvent e) {
        // Check if visitor
        User user = User.getInstance(e.getPlayerUUID());
        Island island = e.getIsland();
        // Do not run following code unless player is online (still) and the island being entered is a Parkour island
        if (!user.isOnline() || !addon.inWorld(island.getWorld())) {
            return;
        }
        Optional<Location> start = addon.getParkourManager().getStart(island);
        Optional<Location> end = addon.getParkourManager().getEnd(island);
        if (start.isEmpty()) {
            user.notify("parkour.no-start-yet");
        } else if (end.isEmpty()) {
            user.notify("parkour.no-end-yet");
        } else if (!parkourRunManager.timers().containsKey(e.getPlayerUUID())) {
            user.notify("parkour.to-start");
        }
        if (island.getFlag(addon.PARKOUR_CREATIVE) <= island.getRank(user)) {
            user.setGameMode(GameMode.CREATIVE);
        } else {
            user.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorLeave(IslandExitEvent e) {
        // If the user leaves any island, end and clear the session.
        User user = User.getInstance(e.getPlayerUUID());
        if (parkourRunManager.checkpoints().containsKey(e.getPlayerUUID()) && user.isOnline()) {
            user.notify("parkour.session-ended");
        }
        parkourRunManager.clear(e.getPlayerUUID());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Game over
        parkourRunManager.clear(e.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Game over
        parkourRunManager.clear(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorFall(EntityDamageEvent e) {
        // Check if visitor
        if (!(e.getEntity() instanceof Player player)
                || !addon.inWorld(player.getWorld())
                || !e.getCause().equals(DamageCause.VOID)
                || !parkourRunManager.timers().containsKey(e.getEntity().getUniqueId())) {
            return;
        }
        // Put player back to last checkpoint. Do not cancel event so that player takes some damage
        player.playEffect(EntityEffect.ENTITY_POOF);
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0);
        player.teleport(parkourRunManager.checkpoints().get(player.getUniqueId()));

    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        boolean shouldStopRun = switch (e.getCause()) {
        case ENDER_PEARL, CHORUS_FRUIT, DISMOUNT, EXIT_BED -> false;
        case COMMAND, PLUGIN, NETHER_PORTAL, END_PORTAL, SPECTATE, END_GATEWAY, UNKNOWN -> true;
        };
        if (shouldStopRun && parkourRunManager.timers().containsKey(e.getPlayer().getUniqueId())) {
            User user = User.getInstance(e.getPlayer().getUniqueId());
            if (parkourRunManager.checkpoints().containsKey(e.getPlayer().getUniqueId()) && user.isOnline()) {
                user.notify("parkour.session-ended");
            }
            parkourRunManager.clear(e.getPlayer().getUniqueId());
        }
        // Check world - only apply flag actions to Parkour world and only if player is not actively running the course
        if (e.getTo() == null // To can sometimes be null...
                || !addon.inWorld(e.getTo())
                || parkourRunManager.timers().containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        // Handle flag action for players who are not running
        Optional<Island> fromIsland = addon.getIslands().getIslandAt(e.getFrom());
        Optional<Island> toIsland = addon.getIslands().getIslandAt(e.getTo());

        if (fromIsland.isPresent() && toIsland.isPresent() && fromIsland.get().equals(toIsland.get())) {
            // same island teleport
            Island island = fromIsland.get();
            User user = User.getInstance(e.getPlayer());
            if (island.getFlag(addon.PARKOUR_CREATIVE) <= island.getRank(user)) {
                user.setGameMode(GameMode.CREATIVE);
            } else {
                user.setGameMode(GameMode.SURVIVAL);
            }
        }

    }


    /**
     * Prevent players from issuing commands during a run
     *
     * @param e PlayerCommandPreprocessEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorCommand(PlayerCommandPreprocessEvent e) {
        if (!parkourRunManager.timers().containsKey(e.getPlayer().getUniqueId())
                || !e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                || e.getPlayer().hasPermission("parkour.mod.bypasscommandban")) {
            return;
        }

        String command = e.getMessage();
        for (String parkourAllowedCommand : addon.getSettings().getParkourAllowedCommands()) {
            if (command.startsWith(parkourAllowedCommand)) {
                return;
            }
        }
        // Always allow using /<base command> quit
        if (addon.getPlayerCommand().isPresent()) {
            CompositeCommand cmd = addon.getPlayerCommand().get();
            List<String> commands = cmd.getAliases();
            commands.add(cmd.getLabel());
            for (String alias : commands) {
                if (command.startsWith("/" + alias + " quit")) {
                    return;
                }
            }
        }

        User user = User.getInstance(e.getPlayer());
        user.notify("protection.protected", TextVariables.DESCRIPTION,
                user.getTranslation("protection.command-is-banned"));
        e.setCancelled(true);
    }

    /**
     * Start or end the run
     *
     * @param e PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onStartEndSet(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL)
                || e.getClickedBlock() == null
                || !e.getClickedBlock().getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)
                || !addon.inWorld(e.getPlayer().getLocation())) {
            return;
        }

        Location l = e.getClickedBlock().getLocation();
        User user = User.getInstance(e.getPlayer());
        addon.getIslands().getProtectedIslandAt(l).ifPresent(island -> {
            Optional<Location> start = addon.getParkourManager().getStart(island);
            Optional<Location> end = addon.getParkourManager().getEnd(island);

            // Check if start and end is set
            if (start.filter(startLoc -> isLocEquals(l, startLoc)).isPresent()) {
                // End is not set
                if (end.isEmpty()) {
                    user.sendMessage("parkour.set-the-end");
                    return;
                }
                // Start the race!
                if (!parkourRunManager.timers().containsKey(e.getPlayer().getUniqueId())) {
                    parkourStart(user, l);
                }
            } else if (end.filter(endLoc -> isLocEquals(l, endLoc)).isPresent()
                    && parkourRunManager.timers().containsKey(e.getPlayer().getUniqueId())) {
                // End the race!
                parkourEnd(user, island, l);
            }
        });
    }

    void parkourStart(User user, Location l) {
        user.sendMessage("parkour.start");
        user.getPlayer().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
        parkourRunManager.timers().put(user.getUniqueId(), System.currentTimeMillis());
        parkourRunManager.checkpoints().put(user.getUniqueId(), user.getLocation());
        user.setGameMode(GameMode.SURVIVAL);

    }

    void parkourEnd(User user, Island island, Location l) {
        long duration = (System.currentTimeMillis() - Objects.requireNonNull(parkourRunManager.timers().get(user.getUniqueId())));
        user.notify("parkour.end");
        user.getPlayer().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
        user.notify("parkour.you-took", TextVariables.NUMBER, getDuration(user, duration));
        parkourRunManager.clear(user.getUniqueId());

        // Comment on time
        long previous = addon.getParkourManager().getTime(island, user.getUniqueId());
        if (duration < previous || previous == 0L) {
            user.sendMessage("parkour.top.beat-previous-time");
            // Store
            addon.getParkourManager().addScore(island, user, duration);
        } else {
            user.sendMessage("parkour.top.did-not-beat-previous-time");
        }
        // Say rank
        user.sendMessage("parkour.top.your-rank", TextVariables.NUMBER,
                String.valueOf(addon.getParkourManager().getRank(island, user.getUniqueId())));

        // set creative
        if (island.getFlag(addon.PARKOUR_CREATIVE) <= island.getRank(user)) {
            user.setGameMode(GameMode.CREATIVE);
        }

    }

    /**
     * Handle checkpoints.
     *
     * @param e PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCheckpoint(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL) || e.getClickedBlock() == null
                || !e.getClickedBlock().getType().equals(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE)
                || !addon.inWorld(e.getPlayer().getLocation())
                || !parkourRunManager.timers().containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        Location l = e.getClickedBlock().getLocation();
        User user = User.getInstance(e.getPlayer());
        Vector checkPoint = parkourRunManager.checkpoints().get(e.getPlayer().getUniqueId()).toVector();

        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && !l.toVector().equals(checkPoint)) {
            user.notify("parkour.checkpoint");
            e.getPlayer().playSound(l, Sound.BLOCK_BELL_USE, 1F, 1F);
            parkourRunManager.checkpoints().put(user.getUniqueId(), e.getPlayer().getLocation());
        }
    }
}
