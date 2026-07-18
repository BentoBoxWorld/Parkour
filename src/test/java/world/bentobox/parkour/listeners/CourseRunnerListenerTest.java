package world.bentobox.parkour.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Creeper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.CommonTestSetup;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.ParkourRunRecord;
import world.bentobox.parkour.Settings;

/**
 * @author tastybento
 */
class CourseRunnerListenerTest extends CommonTestSetup {
    @Mock
    private ParkourManager parkourManager;

    private CourseRunnerListener crl;
    @Mock
    private @NonNull Location testLocation;
    // Not mock
    private ParkourRunRecord prm;
    @Mock
    private CompositeCommand cc;
    @Mock
    private Block block;
    @Mock
    private User u;
    private Settings settings;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        // Player setup (already done in CommonTestSetup)
        when(mockPlayer.getWorld()).thenReturn(world);
        uuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        when(mockPlayer.getName()).thenReturn("tastybento");
        when(mockPlayer.getLocation()).thenReturn(location);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.hasPermission(anyString())).thenReturn(false);
        when(mockPlayer.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(mockPlayer.getServer()).thenReturn(server);
        when(mockPlayer.spigot()).thenReturn(spigot);
        User.setPlugin(plugin);

        // Mock user u
        when(u.getUniqueId()).thenReturn(uuid);
        when(u.getPlayer()).thenReturn(mockPlayer);
        when(u.getTranslationOrNothing(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class));

        // Islands
        when(plugin.getIslands()).thenReturn(im);
        when(addon.getIslands()).thenReturn(im);
        when(im.getIsland(world, User.getInstance(mockPlayer))).thenReturn(island);
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.of(island));
        when(im.hasIsland(world, User.getInstance(mockPlayer))).thenReturn(true);
        when(im.inTeam(world, uuid)).thenReturn(true);
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.OWNER_RANK);
        when(island.getRank(any(User.class))).thenReturn(RanksManager.MEMBER_RANK);
        when(island.getWorld()).thenReturn(world);
        when(im.userIsOnIsland(any(), any())).thenReturn(true);

        // Parkour Manager
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        when(addon.getParkourManager()).thenReturn(parkourManager);

        // Notifier (already in CommonTestSetup)
        when(plugin.getNotifier()).thenReturn(notifier);

        // Command
        List<String> al = new ArrayList<>();
        al.add("parkour");
        al.add("pk");
        when(cc.getAliases()).thenReturn(al);
        when(addon.getPlayerCommand()).thenReturn(Optional.of(cc));

        // Settings
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // Test Location
        when(testLocation.getWorld()).thenReturn(world);
        when(testLocation.toVector()).thenReturn(new Vector(0, 0, 0));
        when(testLocation.clone()).thenReturn(testLocation);
        when(testLocation.add(0.5, 0, 0.5)).thenReturn(testLocation);

        // Run Manager and ParkourManager
        prm = new ParkourRunRecord(new HashMap<>(), new HashMap<>(), new ArrayList<>());
        when(addon.getParkourRunRecord()).thenReturn(prm);
        when(addon.inWorld(location)).thenReturn(true);
        when(addon.inWorld(world)).thenReturn(true);
        when(addon.getParkourManager()).thenReturn(parkourManager);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));

        // Block
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(block.getLocation()).thenReturn(location);

        // DUT
        crl = new CourseRunnerListener(addon);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#CourseRunnerListener(world.bentobox.parkour.Parkour)}.
     */
    @Test
    void testCourseRunnerListener() {
        assertNotNull(crl);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorArrive(world.bentobox.bentobox.api.events.island.IslandEnterEvent)}.
     */
    @Test
    void testOnVisitorArrive() {
        IslandEnterEvent e = new IslandEnterEvent(island, uuid, false, location, island, null);
        crl.onVisitorArrive(e);
        verify(notifier).notify(any(), eq("parkour.to-start"));
        verify(mockPlayer).setGameMode(GameMode.CREATIVE);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorArrive(world.bentobox.bentobox.api.events.island.IslandEnterEvent)}.
     */
    @Test
    void testOnVisitorArriveOtherGame() {
        when(addon.inWorld(world)).thenReturn(false);
        IslandEnterEvent e = new IslandEnterEvent(island, uuid, false, location, island, null);
        crl.onVisitorArrive(e);
        verify(notifier, never()).notify(any(), eq("parkour.to-start"));
        verify(mockPlayer, never()).setGameMode(GameMode.CREATIVE);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorArrive(world.bentobox.bentobox.api.events.island.IslandEnterEvent)}.
     */
    @Test
    void testOnVisitorArriveInRace() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        prm.checkpoints().put(uuid, location);
        IslandEnterEvent e = new IslandEnterEvent(island, uuid, false, location, island, null);
        crl.onVisitorArrive(e);
        verify(notifier, never()).notify(any(), eq("parkour.to-start"));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    void testOnVisitorLeave() {
        prm.checkpoints().put(uuid, location);
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    void testOnVisitorLeaveOffline() {
        when(mockPlayer.isOnline()).thenReturn(false);
        prm.checkpoints().put(uuid, location);
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier, never()).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    void testOnVisitorLeaveNotRuning() {
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier, never()).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent)}.
     */
    @Test
    void testOnPlayerDeath() {
        PlayerDeathEvent e = new PlayerDeathEvent(mockPlayer, mock(DamageSource.class), List.of(), 0,
                net.kyori.adventure.text.Component.empty(), false);
        crl.onPlayerDeath(e);
        assertFalse(prm.timers().containsKey(uuid));
        assertFalse(prm.checkpoints().containsKey(uuid));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent)}.
     */
    @Test
    void testOnPlayerQuit() {
        PlayerQuitEvent e = new PlayerQuitEvent(mockPlayer, net.kyori.adventure.text.Component.empty(),
                PlayerQuitEvent.QuitReason.DISCONNECTED);
        crl.onPlayerQuit(e);
        assertFalse(prm.timers().containsKey(uuid));
        assertFalse(prm.checkpoints().containsKey(uuid));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    void testOnVisitorFall() {
        // Stub the static method using mockedUtil
        mockedUtil.when(() -> Util.teleportAsync(any(), any(), any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(true));

        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        prm.checkpoints().put(uuid, location);
        EntityDamageEvent e = new EntityDamageEvent(mockPlayer, DamageCause.VOID, null, 0);
        crl.onVisitorFall(e);
        // prevent-void-death defaults to true, so the player is saved from dying
        assertTrue(e.isCancelled());
        verify(world).spawnParticle(eq(Particle.POOF), eq(location), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(mockPlayer).setVelocity(new Vector(0, 0, 0));
        verify(mockPlayer).setFallDistance(0);
        // Verify static call
        mockedUtil.verify(() -> Util.teleportAsync(eq(mockPlayer), eq(location), eq(PlayerTeleportEvent.TeleportCause.PLUGIN)));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    void testOnVisitorFallPreventVoidDeathDisabled() {
        // Stub the static method using mockedUtil
        mockedUtil.when(() -> Util.teleportAsync(any(), any(), any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(true));

        settings.setPreventVoidDeath(false);
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        prm.checkpoints().put(uuid, location);
        EntityDamageEvent e = new EntityDamageEvent(mockPlayer, DamageCause.VOID, null, 0);
        crl.onVisitorFall(e);
        // prevent-void-death is off, so the player still takes damage (event not cancelled)
        assertFalse(e.isCancelled());
        verify(world).spawnParticle(eq(Particle.POOF), eq(location), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(mockPlayer).setVelocity(new Vector(0, 0, 0));
        verify(mockPlayer).setFallDistance(0);
        // Verify static call
        mockedUtil.verify(() -> Util.teleportAsync(eq(mockPlayer), eq(location), eq(PlayerTeleportEvent.TeleportCause.PLUGIN)));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    void testOnVisitorFallNotVoid() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        prm.checkpoints().put(uuid, location);
        EntityDamageEvent e = new EntityDamageEvent(mockPlayer, DamageCause.BLOCK_EXPLOSION, null, 0);
        crl.onVisitorFall(e);
        verify(world, never()).spawnParticle(eq(Particle.POOF), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(mockPlayer, never()).setVelocity(new Vector(0, 0, 0));
        verify(mockPlayer, never()).setFallDistance(0);
        verify(mockPlayer, never()).teleport(location);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    void testOnVisitorFallNotRunning() {
        EntityDamageEvent e = new EntityDamageEvent(mockPlayer, DamageCause.VOID, null, 1D);
        crl.onVisitorFall(e);
        verify(world, never()).spawnParticle(eq(Particle.POOF), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(mockPlayer, never()).setVelocity(new Vector(0, 0, 0));
        verify(mockPlayer, never()).setFallDistance(0);
        verify(mockPlayer, never()).teleport(location);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    void testOnVisitorFallNotPlayer() {
        Creeper creeper = mock(Creeper.class);
        EntityDamageEvent e = new EntityDamageEvent(creeper, DamageCause.VOID, null, 1D);
        crl.onVisitorFall(e);
        verify(world, never()).spawnParticle(eq(Particle.POOF), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    void testOnVisitorCommand() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(mockPlayer, "/island");
        crl.onVisitorCommand(e);
        assertTrue(e.isCancelled());
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    void testOnVisitorCommandNotRunning() {
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(mockPlayer, "/island");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    void testOnVisitorCommandQuitting() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(mockPlayer, "/pk quit");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    void testOnVisitorCommandQuittingParkour() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(mockPlayer, "/parkour quit");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onStartEndSet(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    void testOnStartEndSet() {
        PlayerInteractEvent e = new PlayerInteractEvent(mockPlayer, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onStartEndSet(e);
        checkSpigotMessage("parkour.start");
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onStartEndSet(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    void testOnStartEndSetNoEnd() {
        when(this.parkourManager.getEnd(island)).thenReturn(Optional.empty());
        PlayerInteractEvent e = new PlayerInteractEvent(mockPlayer, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onStartEndSet(e);
        checkSpigotMessage("parkour.set-the-end");
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onStartEndSet(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    void testOnStartEndSetRaceOver() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(world);
        when(l.getBlockX()).thenReturn(20);
        when(this.parkourManager.getStart(island)).thenReturn(Optional.of(l));
        PlayerInteractEvent e = new PlayerInteractEvent(mockPlayer, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onStartEndSet(e);
        verify(mockPlayer).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#parkourStart(User, Location)}.
     */
    @Test
    void testParkourStart() {
        crl.parkourStart(u, location);
        verify(u).sendMessage("parkour.start");
        verify(mockPlayer).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
        verify(u).setGameMode(GameMode.SURVIVAL);

        assertTrue(prm.checkpoints().containsKey(uuid));
        assertTrue(prm.timers().containsKey(uuid));

    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#parkourEnd(User, Island, Location)}.
     */
    @Test
    void testParkourEnd() {
        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago

        crl.parkourEnd(u, island, location);
        verify(u).notify("parkour.end");
        verify(mockPlayer).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 1F);
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
    void testParkourEndLongerTime() {
        when(this.parkourManager.getTime(island, uuid)).thenReturn(1L);

        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago

        crl.parkourEnd(u, island, location);
        verify(u).sendMessage("parkour.top.did-not-beat-previous-time");
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#parkourEnd(User, Island, Location)}.
     */
    @Test
    void testParkourEndNoCreative() {
        when(island.getFlag(addon.PARKOUR_CREATIVE)).thenReturn(RanksManager.ADMIN_RANK);

        prm.timers().put(uuid, System.currentTimeMillis() - 20000); // ~ 20 seconds ago

        crl.parkourEnd(u, island, location);
        verify(u, never()).setGameMode(GameMode.CREATIVE);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onCheckpoint(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    void testOnCheckpointNotPhysical() {
        PlayerInteractEvent e = new PlayerInteractEvent(mockPlayer, Action.LEFT_CLICK_AIR, null, block, BlockFace.DOWN);
        crl.onCheckpoint(e);
        verify(block, never()).getLocation();
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onCheckpoint(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Test
    void testOnCheckpointInitialChecks() {
        Location l = mock(Location.class);
        when(l.toVector()).thenReturn(new Vector(100, 0, 20)); // Different to location
        prm.checkpoints().put(uuid, l);

        when(block.getType()).thenReturn(Material.STONE);

        when(iwm.inWorld(location)).thenReturn(false);

        PlayerInteractEvent e = new PlayerInteractEvent(mockPlayer, Action.PHYSICAL, null, block, BlockFace.DOWN);
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
        verify(mockPlayer).playSound(location, Sound.BLOCK_BELL_USE, 1F, 1F);

    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    void testOnTeleport() {
        // Player is running
        for (TeleportCause cause : TeleportCause.values()) {
            // Reset the maps
            prm.checkpoints().clear();
            prm.timers().clear();
            prm.checkpoints().put(uuid, location);
            prm.timers().put(uuid, 20L);
            // Make the event
            PlayerTeleportEvent e = new PlayerTeleportEvent(mockPlayer, location, location, cause);
            // Fire event
            crl.onTeleport(e);
        }
        // Should fire 5 times: COMMAND, PLUGIN, SPECTATE, END_GATEWAY, UNKNOWN
        verify(notifier, times(5)).notify(any(), eq("parkour.session-ended"));
        // Should happen just 3 times: COMMAND, PLUGIN, UNKNOWN
        verify(mockPlayer, times(3)).setGameMode(GameMode.CREATIVE);
        verify(mockPlayer, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    void testOnTeleportNoFlagActionNullTo() {
        // Make the event
        PlayerTeleportEvent e = new PlayerTeleportEvent(mockPlayer, location, null, TeleportCause.ENDER_PEARL);
        // Fire event
        crl.onTeleport(e);
        verify(mockPlayer, never()).setGameMode(GameMode.CREATIVE);
        verify(mockPlayer, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    void testOnTeleportToNoFlagActionNotInParkourWorld() {
        // Make the event
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(mock(World.class));
        PlayerTeleportEvent e = new PlayerTeleportEvent(mockPlayer, location, l, TeleportCause.PLUGIN);
        // Fire event
        crl.onTeleport(e);
        verify(mockPlayer, never()).setGameMode(GameMode.CREATIVE);
        verify(mockPlayer, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    void testOnTeleportToNoFlagActionDifferentIsland() {
        // Make the event
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(world);
        Island i = mock(Island.class);
        when(im.getIslandAt(l)).thenReturn(Optional.of(i));
        PlayerTeleportEvent e = new PlayerTeleportEvent(mockPlayer, location, l, TeleportCause.PLUGIN);
        // Fire event
        crl.onTeleport(e);
        verify(mockPlayer, never()).setGameMode(GameMode.CREATIVE);
        verify(mockPlayer, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    void testOnTeleportToFlagActionVisitors() {
        when(island.getFlag(any())).thenReturn(RanksManager.MEMBER_RANK);
        when(island.getRank(any(User.class))).thenReturn(RanksManager.VISITOR_RANK);

        // Make the event
        PlayerTeleportEvent e = new PlayerTeleportEvent(mockPlayer, location, location, TeleportCause.PLUGIN);
        // Fire event
        crl.onTeleport(e);
        verify(mockPlayer, never()).setGameMode(GameMode.CREATIVE);
        // Visitors should be set to survival when they teleport to the island.
        verify(mockPlayer).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onTeleport(org.bukkit.event.player.PlayerTeleportEvent)}.
     */
    @Test
    void testOnTeleportToFlagActionVisitorsChorusFruit() {
        when(island.getFlag(any())).thenReturn(RanksManager.MEMBER_RANK);
        when(island.getRank(any(User.class))).thenReturn(RanksManager.VISITOR_RANK);

        // Make the event
        PlayerTeleportEvent e = new PlayerTeleportEvent(mockPlayer, location, location, TeleportCause.CONSUMABLE_EFFECT);
        // Fire event
        crl.onTeleport(e);
        // Never alter the game mode
        verify(mockPlayer, never()).setGameMode(GameMode.CREATIVE);
        verify(mockPlayer, never()).setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Check that spigot sent the message
     * @param message - message to check
     */
    void checkSpigotMessage(String expectedMessage) {
        checkSpigotMessage(expectedMessage, 1);
    }

    void checkSpigotMessage(String expectedMessage, int expectedOccurrences) {
        // BentoBox 3.14 routes User.sendMessage through Adventure: CommandSender.sendMessage(Component)
        ArgumentCaptor<net.kyori.adventure.text.Component> captor = ArgumentCaptor
                .forClass(net.kyori.adventure.text.Component.class);

        // Capture any Adventure components sent to the player
        verify(mockPlayer, atLeast(0)).sendMessage(captor.capture());

        // Count the number of occurrences of the expectedMessage in the captured messages
        long actualOccurrences = captor.getAllValues().stream()
                .map(component -> net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(component))
                .filter(messageText -> messageText.contains(expectedMessage))
                .count();

        // Assert that the number of occurrences matches the expectedOccurrences
        assertEquals(expectedOccurrences, actualOccurrences,
                "Expected message occurrence mismatch: " + expectedMessage);
    }

}
