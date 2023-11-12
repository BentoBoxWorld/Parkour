package world.bentobox.parkour.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.parkour.AbstractParkourTest;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.Settings;
import world.bentobox.parkour.gui.RankingsUI;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class TopCommandTest extends AbstractParkourTest {
	@Mock
	private CompositeCommand ac;
	@Mock
	private LocalesManager lm;
	private UUID uuid;
	@Mock
	private World world;
	@Mock
	private ParkourManager parkourManager;

	private TopCommand cmd;
	@Mock
	private @NonNull Location location;
	@Mock
	private RankingsUI rankings;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
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
		uuid = UUID.randomUUID();
		when(user.getUniqueId()).thenReturn(uuid);
		when(user.getPlayer()).thenReturn(p);
		when(user.getName()).thenReturn("tastybento");
		when(user.getLocation()).thenReturn(location);
		when(user.getTranslation(any())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
		User.setPlugin(plugin);

		// Parent command has no aliases
		when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
		when(ac.getWorld()).thenReturn(world);
		when(ac.getAddon()).thenReturn(addon);
		when(addon.getRankings()).thenReturn(rankings);

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
		when(addon.getParkourManager()).thenReturn(parkourManager);

		// IWM
		when(plugin.getIWM()).thenReturn(iwm);
		when(iwm.getPermissionPrefix(any())).thenReturn("parkour.");
		when(iwm.inWorld(world)).thenReturn(true);

		// Settings
		Settings settings = new Settings();
		when(addon.getSettings()).thenReturn(settings);

		// DUT
		cmd = new TopCommand(ac);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.TopCommand#TopCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
	 */
	@Test
	public void testTopCommand() {
		assertNotNull(cmd);
	}

	/**
	 * Test method for {@link world.bentobox.parkour.commands.TopCommand#setup()}.
	 */
	@Test
	public void testSetup() {
		assertEquals("top", cmd.getPermission());
		assertEquals("parkour.commands.parkour.top.description", cmd.getDescription());
		assertTrue(cmd.isOnlyPlayer());
	}

	/**
     * Test method for {@link world.bentobox.parkour.commands.TopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteFailNotOnIsland() {
        // Not on island
        when(im.getIslandAt(location)).thenReturn(Optional.empty());
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("parkour.errors.not-on-island");
    }

	/**
     * Test method for {@link world.bentobox.parkour.commands.TopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteFailWrongWorld() {
        when(iwm.inWorld(world)).thenReturn(false);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.wrong-world");
    }

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.TopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	public void testCanExecutePass() {
		assertTrue(cmd.canExecute(user, "", List.of()));
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.TopCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	public void testExecuteUserStringListOfString() {
		testCanExecutePass();
		assertTrue(cmd.execute(user, "", List.of()));
		verify(rankings).getGUI(island, user);
	}

}
