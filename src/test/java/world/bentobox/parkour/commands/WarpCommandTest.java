package world.bentobox.parkour.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.CommonTestSetup;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.Settings;

/**
 * @author tastybento
 *
 */
class WarpCommandTest extends CommonTestSetup {

	@Mock
	private CompositeCommand ac;
	@Mock
	private User user;
	private WarpCommand cmd;
	@Mock
	private ParkourManager parkourManager;
	@Mock
	private @NonNull Player p;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeEach
	protected void setUp() throws Exception {
		super.setUp();

		// Command manager
		CommandsManager cm = mock(CommandsManager.class);
		when(plugin.getCommandsManager()).thenReturn(cm);

		// User
		when(user.isOp()).thenReturn(false);
		when(user.getPermissionValue(anyString(), anyInt())).thenReturn(4);
		when(user.getWorld()).thenReturn(world);
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

		// Settings
		Settings settings = new Settings();
		when(addon.getSettings()).thenReturn(settings);

		// Stub Util methods
		mockedUtil.when(() -> Util.tabLimit(any(), any())).thenCallRealMethod();
		mockedUtil.when(() -> Util.teleportAsync(any(), any(), any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(true));

		// DUT
		cmd = new WarpCommand(ac);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.WarpCommand#WarpCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
	 */
	@Test
	void testWarpCommand() {
		assertNotNull(cmd);
	}

	/**
	 * Test method for {@link world.bentobox.parkour.commands.WarpCommand#setup()}.
	 */
	@Test
	void testSetup() {
		assertEquals("warp", cmd.getPermission());
		assertEquals("parkour.commands.parkour.warp.description", cmd.getDescription());
		assertEquals("parkour.commands.parkour.warp.parameters", cmd.getParameters());
		assertTrue(cmd.isOnlyPlayer());

	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testCanExecuteHelp() {
		assertFalse(cmd.canExecute(user, "", List.of("more", "than", "one")));
		verify(user).sendMessage("commands.help.header", "[label]", "Parkour");
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testCanExecuteNoArgNotOnParkourIsland() {
		// Not on any island
		when(im.getIslandAt(location)).thenReturn(Optional.empty());
		assertFalse(cmd.canExecute(user, "", List.of()));
		// On island, but not in world
		when(im.getIslandAt(location)).thenReturn(Optional.of(island));
		when(addon.inWorld(world)).thenReturn(false);
		assertFalse(cmd.canExecute(user, "", List.of()));
		verify(user, times(2)).sendMessage("parkour.errors.not-on-island");
		verify(user, times(2)).sendMessage("commands.help.header", "[label]", "Parkour");
	}

	/**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoArgOnIslandNoWarp() {
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
    void testCanExecuteNoArgSuccess() {
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(location));
        when(im.getIslandAt(location)).thenReturn(Optional.of(island));
        assertTrue(cmd.canExecute(user, "", List.of()));
        verify(user, never()).sendMessage(any());
    }

	/**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteArgNoWarps() {
        when(parkourManager.getWarps()).thenReturn(new HashMap<>());
        assertFalse(cmd.canExecute(user, "", List.of("tastybento")));
        verify(user).sendMessage("parkour.warp.unknown-course");
    }

	/**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteArgDifferentPlayer() {
        when(parkourManager.getWarps()).thenReturn(Map.of("Bill", location));
        assertFalse(cmd.canExecute(user, "", List.of("tastybento")));
        verify(user).sendMessage("parkour.warp.unknown-course");
    }

	/**
     * Test method for {@link world.bentobox.parkour.commands.WarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteArgMixedCase() {
        when(parkourManager.getWarps()).thenReturn(Map.of("tAsTyBeNtO", location));
        assertTrue(cmd.canExecute(user, "", List.of("tastybento")));
        verify(user, never()).sendMessage(any());
    }

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.WarpCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testExecuteUserStringListOfString() {
		// Set warpspot
		testCanExecuteArgMixedCase();
		// Run test
		assertTrue(cmd.execute(user, "", List.of()));
		verify(user).sendMessage("parkour.warp.warping");
		// Teleport user
		verify(p, times(2)).playSound(location, Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
		mockedUtil.verify(() -> Util.teleportAsync(p, location, TeleportCause.COMMAND));

	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.WarpCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testTabCompleteUserStringListOfString() {
		assertTrue(cmd.tabComplete(user, "", List.of("ta")).get().isEmpty());
		when(parkourManager.getWarps()).thenReturn(Map.of("tAsTyBeNtO", location));
		List<String> list = cmd.tabComplete(user, "", List.of("ta")).get();
		assertEquals("tAsTyBeNtO", list.get(0));
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.WarpCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testTabCompleteUserStringListOfStringEmpty() {
		assertTrue(cmd.tabComplete(user, "", List.of()).get().isEmpty());
		when(parkourManager.getWarps()).thenReturn(Map.of("tAsTyBeNtO", location));
		List<String> list = cmd.tabComplete(user, "", List.of("ta")).get();
		assertEquals("tAsTyBeNtO", list.get(0));
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.WarpCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testTabCompleteUserStringListOfString10OptionsEmpty() {
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
