package world.bentobox.parkour.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
import org.bukkit.util.Vector;

import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.Parkour;

/**
 * Handles the mechanics of the race.
 * @author tastybento
 *
 */
public class CourseRunnerListener extends AbstractListener {

    private final Map<UUID, Location> checkpoints = new HashMap<>();
    private final Map<UUID, Long> timers = new HashMap<>();

    /**
     * @param addon Parkour addon
     */
    public CourseRunnerListener(Parkour addon) {
        super(addon);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorArrive(IslandEnterEvent e) {
        // Check if visitor
        User user = User.getInstance(e.getPlayerUUID());
        if (user == null || !user.isOnline() || !addon.inWorld(e.getLocation())) {
            return;
        }
        Island island = e.getIsland();
        Optional<Location> start = addon.getPm().getStart(island);
        Optional<Location> end = addon.getPm().getEnd(island);
        if (start.isEmpty()) {
            user.notify("parkour.no-start-yet");
        } else if (end.isEmpty()) {
            user.notify("parkour.no-end-yet");
        } else if (!timers.containsKey(e.getPlayerUUID())){
            user.notify("parkour.to-start");
            user.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorLeave(IslandExitEvent e) {
        User user = User.getInstance(e.getPlayerUUID());
        if (checkpoints.containsKey(e.getPlayerUUID()) && user != null && user.isOnline()) {
            user.notify("parkour.session-ended");
        }
        clear(e.getPlayerUUID());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Game over
        clear(e.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Game over
        clear(e.getPlayer().getUniqueId());
    }

    private void clear(UUID uuid) {
        checkpoints.remove(uuid);
        timers.remove(uuid);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorFall(EntityDamageEvent e) {
        // Check if visitor
        if (!e.getCause().equals(DamageCause.VOID) || !timers.containsKey(e.getEntity().getUniqueId())) {
            return;
        }
        if (e.getEntity() instanceof Player player) {
            // Put player back to last checkpoint. Do not cancel event so that player takes some damage
            player.playEffect(EntityEffect.ENTITY_POOF);
            player.setVelocity(new Vector(0,0,0));
            player.setFallDistance(0);
            player.teleport(checkpoints.get(player.getUniqueId()));
        }
    }

    /**
     * Prevent players from issuing commands during a run
     * @param e PlayerCommandPreprocessEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onVisitorCommand(PlayerCommandPreprocessEvent e) {
        if (!timers.containsKey(e.getPlayer().getUniqueId()) || !e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                || e.getPlayer().hasPermission("parkour.mod.bypasscommandban")) {
            return;
        }

        String command = e.getMessage();
        for (String parkourAllowedCommand : addon.getSettings().getParkourAllowedCommands()) {
            if (command.startsWith(parkourAllowedCommand)) {
                return;
            }
        }


        User user = User.getInstance(e.getPlayer());
        user.notify("protection.protected", TextVariables.DESCRIPTION, user.getTranslation("protection.command-is-banned"));
        e.setCancelled(true);
    }

    /**
     * Start or end the run
     * @param e PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onStartEndSet(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL)) return;
        if (e.getClickedBlock() == null || !e.getClickedBlock().getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE) || !addon.inWorld(e.getPlayer().getLocation())) {
            return;
        }

        Location l = e.getClickedBlock().getLocation();
        User user = Objects.requireNonNull(User.getInstance(e.getPlayer()));
        if (addon.getIslands().getProtectedIslandAt(l).isPresent()) {
            Island island = addon.getIslands().getProtectedIslandAt(l).get();

            Optional<Location> start = addon.getPm().getStart(island);
            Optional<Location> end = addon.getPm().getEnd(island);

            // Check if start and end is set
            if (start.filter(mdv -> isLocEquals(l, mdv)).isPresent()) {
                if (end.isEmpty()) {
                    user.sendMessage("parkour.set-the-end");
                    return;
                }
                if (!timers.containsKey(e.getPlayer().getUniqueId())) {
                    user.sendMessage("parkour.start");
                    e.getPlayer().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
                    timers.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
                    checkpoints.put(user.getUniqueId(), e.getPlayer().getLocation());
                    user.setGameMode(GameMode.SURVIVAL);
                }
            } else if (end.filter(mdv -> isLocEquals(l, mdv)).isPresent()) {
                if (timers.containsKey(e.getPlayer().getUniqueId())) {

                    long duration = (System.currentTimeMillis() - timers.get(e.getPlayer().getUniqueId()));
                    user.notify("parkour.end");
                    e.getPlayer().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
                    user.notify("parkour.you-took", TextVariables.NUMBER, AbstractListener.getDuration(user, duration));
                    clear(e.getPlayer().getUniqueId());

                    // Comment on time
                    long previous = addon.getPm().getTime(island, e.getPlayer().getUniqueId());
                    if (duration < previous || previous == 0L) {
                        user.sendMessage("parkour.top.beat-previous-time");
                        // Store
                        addon.getPm().addScore(island, user, duration);
                    } else {
                        user.sendMessage("parkour.top.did-not-beat-previous-time");
                    }
                    // Say rank
                    user.sendMessage("parkour.top.your-rank", TextVariables.NUMBER, String.valueOf(addon.getPm().getRank(island, e.getPlayer().getUniqueId())));

                    // set creative
                    if (addon.getIslands().userIsOnIsland(addon.getOverWorld(), user)) {
                        user.setGameMode(GameMode.CREATIVE);
                    }
                }
            }
        }
    }

    /**
     * Handle checkpoints.
     * @param e PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCheckpoint(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.PHYSICAL)) return;
        if (e.getClickedBlock() == null || !e.getClickedBlock().getType().equals(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE)
                || !addon.inWorld(e.getPlayer().getLocation()) || !timers.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        Location l = e.getClickedBlock().getLocation();
        User user = Objects.requireNonNull(User.getInstance(e.getPlayer()));
        Vector checkPoint = checkpoints.get(e.getPlayer().getUniqueId()).toVector();
        if (addon.getIslands().getProtectedIslandAt(l).isPresent() && !l.toVector().equals(checkPoint)) {
            user.notify("parkour.checkpoint");
            e.getPlayer().playSound(l, Sound.BLOCK_BELL_USE, 1F, 1F);
            checkpoints.put(user.getUniqueId(), e.getPlayer().getLocation());
        }
    }
}
