package world.bentobox.parkour.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.Notifier;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.ParkourRunRecord;
import world.bentobox.parkour.Settings;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class, Util.class})
public class CourseRunnerListenerTest {

    @Mock
    private BentoBox plugin;
    private User user;
    @Mock
    private LocalesManager lm;
    @Mock
    private Parkour addon;
    private UUID uuid;
    @Mock
    private World world;
    @Mock
    private IslandsManager im;
    @Mock
    private @Nullable Island island;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private ParkourManager parkourManager;

    private CourseRunnerListener crl;
    @Mock
    private @NonNull Location location;
    // Not mock
    private ParkourRunRecord prm;
    @Mock
    private Player player;
    @Mock
    private PlaceholdersManager phm;
    @Mock
    private Notifier notifier;
    @Mock
    private Server server;
    @Mock
    private CompositeCommand cc;
    @Mock
    private Block block;
    @Mock
    private User u;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        // Player
        when(player.getWorld()).thenReturn(world);
        uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("tastybento");
        when(player.getLocation()).thenReturn(location);
        when(player.isOnline()).thenReturn(true);
        when(player.hasPermission(anyString())).thenReturn(false);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.getServer()).thenReturn(server);
        User.setPlugin(plugin);
        user = User.getInstance(player);

        // Mock user u
        when(u.getUniqueId()).thenReturn(uuid);
        when(u.getPlayer()).thenReturn(player);
        when(u.getTranslationOrNothing(anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Islands
        when(plugin.getIslands()).thenReturn(im);
        when(addon.getIslands()).thenReturn(im);
        when(im.getIsland(world, user)).thenReturn(island);
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.of(island));
        when(im.hasIsland(world, user)).thenReturn(true);
        when(im.inTeam(world, uuid)).thenReturn(true);
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.OWNER_RANK);
        when(island.getRank(user)).thenReturn(RanksManager.MEMBER_RANK);
        when(island.getWorld()).thenReturn(world);
        when(im.userIsOnIsland(any(), any())).thenReturn(true);

        // Parkour Manager
        // No warp spot
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        when(addon.getParkourManager()).thenReturn(parkourManager);

        // Notifier
        when(plugin.getNotifier()).thenReturn(notifier);

        // Locales Manager
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
        when(phm.replacePlaceholders(any(), any())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        when(plugin.getPlaceholdersManager()).thenReturn(phm);

        // Command
        List<String> al = new ArrayList<>();
        al.add("parkour");
        al.add("pk");
        when(cc.getAliases()).thenReturn(al);
        when(addon.getPlayerCommand()).thenReturn(Optional.of(cc));

        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("parkour.");
        when(iwm.inWorld(world)).thenReturn(true); // Always in world

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // Location
        when(location.getWorld()).thenReturn(world);
        when(location.toVector()).thenReturn(new Vector(0, 0, 0));
        when(location.clone()).thenReturn(location);
        when(location.add(0.5, 0, 0.5)).thenReturn(location);

        // Run Manager and ParkourManager
        prm = new ParkourRunRecord(new HashMap<>(), new HashMap<>(), new ArrayList<>());
        when(addon.getParkourRunRecord()).thenReturn(prm);
        when(addon.inWorld(location)).thenReturn(true);
        when(addon.inWorld(world)).thenReturn(true);
        when(addon.getParkourManager()).thenReturn(parkourManager);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));

        // RanksManager
        RanksManager rm = new RanksManager();
        when(plugin.getRanksManager()).thenReturn(rm);

        // Block
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(block.getLocation()).thenReturn(location);

        // DUT
        crl = new CourseRunnerListener(addon);
    }

    @After
    public void tearDown() {
        User.clearUsers();
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#CourseRunnerListener(world.bentobox.parkour.Parkour)}.
     */
    @Test
    public void testCourseRunnerListener() {
        assertNotNull(crl);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorArrive(world.bentobox.bentobox.api.events.island.IslandEnterEvent)}.
     */
    @Test
    public void testOnVisitorArrive() {
        IslandEnterEvent e = new IslandEnterEvent(island, uuid, false, location, island, null);
        crl.onVisitorArrive(e);
        verify(notifier).notify(any(), eq("parkour.to-start"));
        verify(player).setGameMode(GameMode.CREATIVE);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorArrive(world.bentobox.bentobox.api.events.island.IslandEnterEvent)}.
     */
    @Test
    public void testOnVisitorArriveOtherGame() {
        when(addon.inWorld(world)).thenReturn(false);
        IslandEnterEvent e = new IslandEnterEvent(island, uuid, false, location, island, null);
        crl.onVisitorArrive(e);
        verify(notifier, never()).notify(any(), eq("parkour.to-start"));
        verify(player, never()).setGameMode(GameMode.CREATIVE);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorArrive(world.bentobox.bentobox.api.events.island.IslandEnterEvent)}.
     */
    @Test
    public void testOnVisitorArriveInRace() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        prm.checkpoints().put(uuid, location);
        IslandEnterEvent e = new IslandEnterEvent(island, uuid, false, location, island, null);
        crl.onVisitorArrive(e);
        verify(notifier, never()).notify(any(), eq("parkour.to-start"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnVisitorLeave() {
        prm.checkpoints().put(uuid, location);
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnVisitorLeaveOffline() {
        when(player.isOnline()).thenReturn(false);
        prm.checkpoints().put(uuid, location);
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier, never()).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnVisitorLeaveNotRuning() {
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier, never()).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent)}.
     */
    @Test
    public void testOnPlayerDeath() {
        PlayerDeathEvent e = new PlayerDeathEvent(player, new ArrayList<>(), 0, 0, 0, 0, "");
        crl.onPlayerDeath(e);
        assertFalse(prm.timers().containsKey(uuid));
        assertFalse(prm.checkpoints().containsKey(uuid));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent)}.
     */
    @Test
    public void testOnPlayerQuit() {
        PlayerQuitEvent e = new PlayerQuitEvent(player, "");
        crl.onPlayerQuit(e);
        assertFalse(prm.timers().containsKey(uuid));
        assertFalse(prm.checkpoints().containsKey(uuid));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    public void testOnVisitorFall() {
        PowerMockito.mockStatic(Util.class, RETURNS_MOCKS);
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        prm.checkpoints().put(uuid, location);
        EntityDamageEvent e = new EntityDamageEvent(player, DamageCause.VOID, 1D);
        crl.onVisitorFall(e);
        verify(player).playEffect(EntityEffect.ENTITY_POOF);
        verify(player).setVelocity(new Vector(0, 0, 0));
        verify(player).setFallDistance(0);
        PowerMockito.verifyStatic(Util.class);
        Util.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    public void testOnVisitorFallNotVoid() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        prm.checkpoints().put(uuid, location);
        EntityDamageEvent e = new EntityDamageEvent(player, DamageCause.BLOCK_EXPLOSION, 1D);
        crl.onVisitorFall(e);
        verify(player, never()).playEffect(EntityEffect.ENTITY_POOF);
        verify(player, never()).setVelocity(new Vector(0, 0, 0));
        verify(player, never()).setFallDistance(0);
        verify(player, never()).teleport(location);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    public void testOnVisitorFallNotRunning() {
        EntityDamageEvent e = new EntityDamageEvent(player, DamageCause.VOID, 1D);
        crl.onVisitorFall(e);
        verify(player, never()).playEffect(EntityEffect.ENTITY_POOF);
        verify(player, never()).setVelocity(new Vector(0, 0, 0));
        verify(player, never()).setFallDistance(0);
        verify(player, never()).teleport(location);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    public void testOnVisitorFallNotPlayer() {
        Creeper creeper = mock(Creeper.class);
        EntityDamageEvent e = new EntityDamageEvent(creeper, DamageCause.VOID, 1D);
        crl.onVisitorFall(e);
        verify(creeper, never()).playEffect(EntityEffect.ENTITY_POOF);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    public void testOnVisitorCommand() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(player, "/island");
        crl.onVisitorCommand(e);
        assertTrue(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    public void testOnVisitorCommandNotRunning() {
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(player, "/island");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    public void testOnVisitorCommandQuitting() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(player, "/pk quit");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    public void testOnVisitorCommandQuittingParkour() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(player, "/parkour quit");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onStartEndSet(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    public void testOnStartEndSet() {
        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onStartEndSet(e);
        verify(player).sendMessage("parkour.start");
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onStartEndSet(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    public void testOnStartEndSetNoEnd() {
        when(this.parkourManager.getEnd(island)).thenReturn(Optional.empty());
        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onStartEndSet(e);
        verify(player).sendMessage("parkour.set-the-end");
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onStartEndSet(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    public void testOnStartEndSetRaceOver() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(world);
        when(l.getBlockX()).thenReturn(20);
        when(this.parkourManager.getStart(island)).thenReturn(Optional.of(l));
        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onStartEndSet(e);
        verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#parkourStart(User, Location)}.
     */
    @Test
    public void testParkourStart() {
        crl.parkourStart(u, location);
        verify(u).sendMessage("parkour.start");
        verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
        verify(u).setGameMode(GameMode.SURVIVAL);

        assertTrue(prm.checkpoints().containsKey(uuid));
        assertTrue(prm.timers().containsKey(uuid));

    }


    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#parkourEnd(User, Island, Location)}.
     */
    @Test
    public void testParkourEnd() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago

        crl.parkourEnd(u, island, location);
        verify(u).notify("parkour.end");
        verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
        verify(u).notify(eq("parkour.you-took"), eq(TextVariables.NUMBER), contains("parkour.seconds"));
        verify(u).sendMessage("parkour.top.beat-previous-time");
        verify(parkourManager).addScore(eq(island), eq(u), anyLong());
        verify(u).sendMessage("parkour.top.your-rank", TextVariables.NUMBER, "0");
        verify(u).setGameMode(GameMode.CREATIVE);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#parkourEnd(User, Island, Location)}.
     */
    @Test
    public void testParkourEndLongerTime() {
        when(this.parkourManager.getTime(island, uuid)).thenReturn(1L);

        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago

        crl.parkourEnd(u, island, location);
        verify(u).sendMessage("parkour.top.did-not-beat-previous-time");
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#parkourEnd(User, Island, Location)}.
     */
    @Test
    public void testParkourEndNoCreative() {
        when(island.getFlag(addon.PARKOUR_CREATIVE)).thenReturn(RanksManager.ADMIN_RANK);

        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago

        crl.parkourEnd(u, island, location);
        verify(u, never()).setGameMode(GameMode.CREATIVE);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onCheckpoint(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    public void testOnCheckpointNotPhysical() {
        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, null, block, BlockFace.DOWN);
        crl.onCheckpoint(e);
        verify(block, never()).getLocation();
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onCheckpoint(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    public void testOnCheckpointInitialChecks() {
        Location l = mock(Location.class);
        when(l.toVector()).thenReturn(new Vector(100, 0, 20)); // Different to location
        prm.checkpoints().put(uuid, l);

        when(block.getType()).thenReturn(Material.STONE);

        when(iwm.inWorld(location)).thenReturn(false);


        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onCheckpoint(e);
        verify(block, never()).getLocation();

        when(iwm.inWorld(location)).thenReturn(true);
        crl.onCheckpoint(e);
        verify(block, never()).getLocation();

        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        crl.onCheckpoint(e);
        verify(block, never()).getLocation();

        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        crl.onCheckpoint(e);
        verify(block).getLocation();

        // Checkpoint reached!
        verify(player).playSound(location, Sound.BLOCK_BELL_USE, 1F, 1F);

    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    public void testOnTeleport() {
        // Player is running
        for (TeleportCause cause : TeleportCause.values()) {
            // Reset the maps
            prm.checkpoints().clear();
            prm.timers().clear();
            prm.checkpoints().put(uuid, location);
            prm.timers().put(uuid, 20L);
            // Make the event
            PlayerTeleportEvent e = new PlayerTeleportEvent(player, location, location, cause);
            // Fire event
            crl.onTeleport(e);
        }
        // Should fire 5 times: COMMAND, PLUGIN, SPECTATE, END_GATEWAY, UNKNOWN
        verify(notifier, times(5)).notify(any(), eq("parkour.session-ended"));
        // Should happen just 3 times: COMMAND, PLUGIN, UNKNOWN
        verify(player, times(3)).setGameMode(GameMode.CREATIVE);
        verify(player, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    public void testOnTeleportNoFlagActionNullTo() {
        // Make the event
        PlayerTeleportEvent e = new PlayerTeleportEvent(player, location, null, TeleportCause.ENDER_PEARL);
        // Fire event
        crl.onTeleport(e);
        verify(player, never()).setGameMode(GameMode.CREATIVE);
        verify(player, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    public void testOnTeleportToNoFlagActionNotInParkourWorld() {
        // Make the event
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(mock(World.class));
        PlayerTeleportEvent e = new PlayerTeleportEvent(player, location, l, TeleportCause.PLUGIN);
        // Fire event
        crl.onTeleport(e);
        verify(player, never()).setGameMode(GameMode.CREATIVE);
        verify(player, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    public void testOnTeleportToNoFlagActionDifferentIsland() {
        // Make the event
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(world);
        Island i = mock(Island.class);
        when(im.getIslandAt(l)).thenReturn(Optional.of(i));
        PlayerTeleportEvent e = new PlayerTeleportEvent(player, location, l, TeleportCause.PLUGIN);
        // Fire event
        crl.onTeleport(e);
        verify(player, never()).setGameMode(GameMode.CREATIVE);
        verify(player, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    public void testOnTeleportToFlagActionVisitors() {
        when(island.getFlag(any())).thenReturn(RanksManager.MEMBER_RANK);
        when(island.getRank(any(User.class))).thenReturn(RanksManager.VISITOR_RANK);

        // Make the event
        PlayerTeleportEvent e = new PlayerTeleportEvent(player, location, location, TeleportCause.PLUGIN);
        // Fire event
        crl.onTeleport(e);
        verify(player, never()).setGameMode(GameMode.CREATIVE);
        // Visitors should be set to survival when they teleport to the island.
        verify(player).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    public void testOnTeleportToFlagActionVisitorsChorusFruit() {
        when(island.getFlag(any())).thenReturn(RanksManager.MEMBER_RANK);
        when(island.getRank(any(User.class))).thenReturn(RanksManager.VISITOR_RANK);

        // Make the event
        PlayerTeleportEvent e = new PlayerTeleportEvent(player, location, location, TeleportCause.CHORUS_FRUIT);
        // Fire event
        crl.onTeleport(e);
        // Never alter the game mode
        verify(player, never()).setGameMode(GameMode.CREATIVE);
        verify(player, never()).setGameMode(GameMode.SURVIVAL);
    }


}
