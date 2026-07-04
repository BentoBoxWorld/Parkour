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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.parkour.CommonTestSetup;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.Settings;

/**
 * @author tastybento
 *
 */
class RemoveWarpCommandTest extends CommonTestSetup {
	@Mock
	private CompositeCommand ac;
	private RemoveWarpCommand cmd;
	@Mock
	private ParkourManager parkourManager;
	@Mock
	private User user;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();

		// Command manager
		CommandsManager cm = mock(CommandsManager.class);
		when(plugin.getCommandsManager()).thenReturn(cm);

		// Player
		Player p = mock(Player.class);
		// Sometimes use Mockito.withSettings().verboseLogging()
		when(user.isOp()).thenReturn(false);
		when(user.getPermissionValue(anyString(), anyInt())).thenReturn(4);
		when(user.getWorld()).thenReturn(world);
		when(user.getUniqueId()).thenReturn(uuid);
		when(user.getPlayer()).thenReturn(p);
		when(user.getName()).thenReturn("tastybento");
		when(user.getTranslation(any())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
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
		// Warp spot available by default
		when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(mock(Location.class)));
		when(addon.getParkourManager()).thenReturn(parkourManager);

		// IWM
		when(plugin.getIWM()).thenReturn(iwm);
		when(iwm.getPermissionPrefix(any())).thenReturn("parkour.");

		// Settings
		Settings settings = new Settings();
		when(addon.getSettings()).thenReturn(settings);

		// DUT
		cmd = new RemoveWarpCommand(ac);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.RemoveWarpCommand#RemoveWarpCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
	 */
	@Test
	void testRemoveWarpCommand() {
		assertNotNull(cmd);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.RemoveWarpCommand#setup()}.
	 */
	@Test
	void testSetup() {
		assertEquals("removewarp", cmd.getPermission());
		assertEquals("parkour.commands.parkour.removewarp.description", cmd.getDescription());
		assertTrue(cmd.isConfigurableRankCommand());
		assertTrue(cmd.isOnlyPlayer());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.RemoveWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testCanExecuteFailHelp() {
		// Help
		assertFalse(cmd.canExecute(user, "", List.of("something")));
		verify(user).sendMessage("commands.help.header", "[label]", "Parkour");
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.RemoveWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testCanExecuteFailNoRank() {
		// Insufficient rank
		assertFalse(cmd.canExecute(user, "", List.of()));
		verify(user).sendMessage("general.errors.insufficient-rank", TextVariables.RANK, RanksManager.MEMBER_RANK_REF);

	}

	/**
     * Test method for {@link world.bentobox.parkour.commands.RemoveWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteFailNoWarpSpot() {
        // Has rank
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.MEMBER_RANK);
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("parkour.errors.no-warp");

    }

	/**
     * Test method for {@link world.bentobox.parkour.commands.RemoveWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecutePass() {
        // Has rank
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.MEMBER_RANK);
        assertTrue(cmd.canExecute(user, "", List.of()));
        verify(user, never()).sendMessage(any());
    }

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.RemoveWarpCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	void testExecuteUserStringListOfString() {
		assertTrue(cmd.execute(user, "", List.of()));
		verify(user).sendMessage("parkour.warp.removed");
		verify(parkourManager).setWarpSpot(island, null);

	}

	/**
     * Test method for {@link world.bentobox.parkour.commands.RemoveWarpCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringWarpRemove() {
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(mock(Location.class)));
        assertTrue(cmd.execute(user, "", List.of()));
        verify(user).sendMessage("parkour.warp.removed");
        verify(parkourManager).setWarpSpot(island, null);
    }

}