package world.bentobox.parkour.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.Settings;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class, Util.class})
public class WarpCommandTest {

    @Mock
    private BentoBox plugin;
    @Mock
    private CompositeCommand ac;
    @Mock
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
    private WarpCommand cmd;
    @Mock
    private ParkourManager parkourManager;
    @Mock
    private Location location;
    @Mock
    private @NonNull Player p;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // User
        when(user.isOp()).thenReturn(false);
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(4);
        when(user.getWorld()).thenReturn(world);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getTranslation(any())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        when(user.getLocation()).thenReturn(location);
        User.setPlugin(plugin);

        // Parent command has no aliases
        when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
        when(ac.getWorld()).thenReturn(world);
        when(ac.getAddon()).thenReturn(addon);

        // Islands
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(world, user)).thenReturn(island);
        when(im.hasIsland(world, user)).thenReturn(true);
        when(im.inTeam(world, uuid)).thenReturn(true);
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.OWNER_RANK);
        when(island.getRank(user)).thenReturn(RanksManager.MEMBER_RANK);
        when(im.userIsOnIsland(any(), any())).thenReturn(true);

        // Parkour Manager
        // No warp spot
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        // Start and end plates are set
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        when(addon.getParkourManager()).thenReturn(parkourManager);

        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("parkour.");

        // World
        when(addon.inWorld(world)).thenReturn(true);

        // Location
        when(location.clone()).thenReturn(location);
        when(location.add(any(Vector.class))).thenReturn(location);

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // RanksManager
        RanksManager rm = new RanksManager();
        when(plugin.getRanksManager()).thenReturn(rm);

        // Static classes : Bukkit, Util
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        PowerMockito.mockStatic(Util.class, Mockito.RETURNS_MOCKS);
        when(Util.tabLimit(any(), any())).thenCallRealMethod();
        // DUT
        cmd = new WarpCommand(ac);
    }
    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#WarpCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    public void testWarpCommand() {
        assertNotNull(cmd);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("warp", cmd.getPermission());
        assertEquals("parkour.commands.parkour.warp.description", cmd.getDescription());
        assertEquals("parkour.commands.parkour.warp.parameters", cmd.getParameters());
        assertTrue(cmd.isOnlyPlayer());

    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteHelp() {
        assertFalse(cmd.canExecute(user, "", List.of("more","than","one")));
        verify(user).sendMessage("commands.help.header","[label]",null);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoArgNotOnParkourIsland() {
        assertFalse(cmd.canExecute(user, "", List.of()));
        // On island, but not in world
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));
        when(addon.inWorld(world)).thenReturn(false);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user, times(2)).sendMessage("parkour.errors.not-on-island");
        verify(user, times(2)).sendMessage("commands.help.header","[label]",null);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoArgOnIslandNoWarp() {
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("parkour.warp.no-warp");
        verify(user, never()).sendMessage("commands.help.header","[label]",null);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoArgSuccess() {
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(location));
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));
        assertTrue(cmd.canExecute(user, "", List.of()));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteArgNoWarps() {
        when(parkourManager.getWarps()).thenReturn(new HashMap<>());
        assertFalse(cmd.canExecute(user, "", List.of("tastybento")));
        verify(user).sendMessage("parkour.warp.unknown-course");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteArgDifferentPlayer() {
        when(parkourManager.getWarps()).thenReturn(Map.of("Bill", location));
        assertFalse(cmd.canExecute(user, "", List.of("tastybento")));
        verify(user).sendMessage("parkour.warp.unknown-course");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteArgMixedCase() {
        when(parkourManager.getWarps()).thenReturn(Map.of("tAsTyBeNtO", location));
        assertTrue(cmd.canExecute(user, "", List.of("tastybento")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfString() {
        // Set warpspot
        testCanExecuteArgMixedCase();
        // Run test
        assertTrue(cmd.execute(user, "", List.of()));
        verify(user).sendMessage("parkour.warp.warping");
        // Teleport user
        verify(p, times(2)).playSound(location, Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
        verify(location).add(any(Vector.class));
        PowerMockito.verifyStatic(Util.class);
        Util.teleportAsync(p, location, TeleportCause.COMMAND);

    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfString() {
        assertTrue(cmd.tabComplete(user, "", List.of("ta")).get().isEmpty());
        when(parkourManager.getWarps()).thenReturn(Map.of("tAsTyBeNtO", location));
        List<String> list = cmd.tabComplete(user, "", List.of("ta")).get();
        assertEquals("tAsTyBeNtO", list.get(0));
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringEmpty() {
        assertTrue(cmd.tabComplete(user, "", List.of()).get().isEmpty());
        when(parkourManager.getWarps()).thenReturn(Map.of("tAsTyBeNtO", location));
        List<String> list = cmd.tabComplete(user, "", List.of("ta")).get();
        assertEquals("tAsTyBeNtO", list.get(0));
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfString10OptionsEmpty() {
        assertTrue(cmd.tabComplete(user, "", List.of("ta")).get().isEmpty());
        Map<String, Location> map = new HashMap<>();
        map.put("tAsTyBeNtO1", location);
        map.put("tAsTyBeNtO2", location);
        map.put("tAsTyBeNtO3", location);
        map.put("tAsTyBeNtO4", location);
        map.put("tAsTyBeNtO5", location);
        map.put("tAsTyBeNtO6", location);
        map.put("tAsTyBeNtO7", location);
        map.put("tAsTyBeNtO8", location);
        map.put("tAsTyBeNtO9", location);
        map.put("tAsTyBeNtO10", location);
        when(parkourManager.getWarps()).thenReturn(map);
        Optional<List<String>> list = cmd.tabComplete(user, "", List.of());
        assertTrue(list.isEmpty());
        // Try again
        list = cmd.tabComplete(user, "", List.of("tA"));
        assertEquals(10, list.get().size());
        // Try again
        list = cmd.tabComplete(user, "", List.of("p"));
        assertTrue(list.get().isEmpty()); // Zero length list

    }

}
