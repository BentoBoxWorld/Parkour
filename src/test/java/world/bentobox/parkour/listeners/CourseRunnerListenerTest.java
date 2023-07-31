package world.bentobox.parkour.listeners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
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
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.events.island.IslandExitEvent;
import world.bentobox.bentobox.api.user.Notifier;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.ParkourRunManager;
import world.bentobox.parkour.Settings;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class })
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
    @Mock
    private ParkourRunManager prm;
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

        // Islands
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(world, user)).thenReturn(island);
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));
        when(im.hasIsland(world, user)).thenReturn(true);
        when(im.inTeam(world, uuid)).thenReturn(true);
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.OWNER_RANK);
        when(island.getRank(user)).thenReturn(RanksManager.MEMBER_RANK);
        when(im.userIsOnIsland(any(), any())).thenReturn(true);

        // Parkour Manager
        // No warp spot
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        when(addon.getPm()).thenReturn(parkourManager);

        // Notifier
        when(plugin.getNotifier()).thenReturn(notifier);

        // Locales Manager
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
        when(phm.replacePlaceholders(any(), any())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        when(plugin.getPlaceholdersManager()).thenReturn(phm);

        // Command
        when(cc.getAliases()).thenReturn(List.of("parkour","pk"));
        when(addon.getPlayerCommand()).thenReturn(Optional.of(cc));

        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("parkour.");
        when(iwm.inWorld(world)).thenReturn(true);

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // Run Manager and ParkourManager
        when(addon.getParkourRunManager()).thenReturn(prm);
        when(addon.inWorld(location)).thenReturn(true);
        when(addon.getPm()).thenReturn(parkourManager);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        when(prm.getTimers()).thenReturn(Map.of()); // No runners yet

        // RanksManager
        RanksManager rm = new RanksManager();
        when(plugin.getRanksManager()).thenReturn(rm);

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
    public void testOnVisitorArriveInRace() {
        when(prm.getTimers()).thenReturn(Map.of(uuid, 20L));
        IslandEnterEvent e = new IslandEnterEvent(island, uuid, false, location, island, null);
        crl.onVisitorArrive(e);
        verify(notifier, never()).notify(any(), eq("parkour.to-start"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnVisitorLeave() {
        when(prm.getCheckpoints()).thenReturn(Map.of(uuid, location));
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
        when(prm.getCheckpoints()).thenReturn(Map.of(uuid, location));
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier, never()).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorLeave(world.bentobox.bentobox.api.events.island.IslandExitEvent)}.
     */
    @Test
    public void testOnVisitorLeaveNotRuning() {
        when(prm.getCheckpoints()).thenReturn(Map.of());
        IslandExitEvent e = new IslandExitEvent(island, uuid, false, location, island, null);
        crl.onVisitorLeave(e);
        verify(notifier, never()).notify(any(), eq("parkour.session-ended"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent)}.
     */
    @Test
    public void testOnPlayerDeath() {
        PlayerDeathEvent e = new PlayerDeathEvent(player, List.of(), 0, 0, 0, 0, "");
        crl.onPlayerDeath(e);
        verify(prm).clear(uuid);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent)}.
     */
    @Test
    public void testOnPlayerQuit() {
        PlayerQuitEvent e = new PlayerQuitEvent(player, "");
        crl.onPlayerQuit(e);
        verify(prm).clear(uuid);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    public void testOnVisitorFall() {
        when(prm.getTimers()).thenReturn(Map.of(uuid, 20L));
        when(prm.getCheckpoints()).thenReturn(Map.of(uuid, location));
        EntityDamageEvent e = new EntityDamageEvent(player, DamageCause.VOID, 1D);
        crl.onVisitorFall(e);
        verify(player).playEffect(EntityEffect.ENTITY_POOF);
        verify(player).setVelocity(new Vector(0, 0, 0));
        verify(player).setFallDistance(0);
        verify(player).teleport(location);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorFall(org.bukkit.event.entity.EntityDamageEvent)}.
     */
    @Test
    public void testOnVisitorFallNotVoid() {
        when(prm.getTimers()).thenReturn(Map.of(uuid, 20L));
        when(prm.getCheckpoints()).thenReturn(Map.of(uuid, location));
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
        when(prm.getTimers()).thenReturn(Map.of());
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
        when(prm.getTimers()).thenReturn(Map.of(uuid, 20L));
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
        when(prm.getTimers()).thenReturn(Map.of(uuid, 20L));
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(player, "/pk quit");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onVisitorCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent)}.
     */
    @Test
    public void testOnVisitorCommandQuittingParkour() {
        when(prm.getTimers()).thenReturn(Map.of(uuid, 20L));
        PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(player, "/parkour quit");
        crl.onVisitorCommand(e);
        assertFalse(e.isCancelled());
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onStartEndSet(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Ignore("WIP")
    @Test
    public void testOnStartEndSet() {
        Block block = mock(Block.class);
        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.PHYSICAL, null, block, BlockFace.DOWN);
        crl.onStartEndSet(e);
    }

    /**
     * Test method for {@link world.bentobox.parkour.listeners.CourseRunnerListener#onCheckpoint(org.bukkit.event.player.PlayerInteractEvent)}.
     */
    @Ignore("WIP")
    @Test
    public void testOnCheckpoint() {
        fail("Not yet implemented"); // TODO
    }

}
