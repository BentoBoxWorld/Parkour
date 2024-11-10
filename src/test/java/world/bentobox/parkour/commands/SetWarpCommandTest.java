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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.parkour.AbstractParkourTest;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.Settings;

/**
 * Set warp command testd
 * 
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RanksManager.class)
public class SetWarpCommandTest extends AbstractParkourTest {
	@Mock
	private LocalesManager lm;
	private UUID uuid;
	@Mock
	private World world;
	private SetWarpCommand cmd;
	@Mock
	private ParkourManager parkourManager;
	@Mock
	private Location location;
	@Mock
	private CompositeCommand ac;
    @Mock
    private RanksManager rm;

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

		// Settings
		Settings settings = new Settings();
		when(addon.getSettings()).thenReturn(settings);

		// DUT
		cmd = new SetWarpCommand(ac);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.SetWarpCommand#SetWarpCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
	 */
	@Test
	public void testSetWarpCommand() {
		assertNotNull(cmd);
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.SetWarpCommand#setup()}.
	 */
	@Test
	public void testSetup() {
		assertEquals("setwarp", cmd.getPermission());
		assertEquals("parkour.commands.parkour.setwarp.description", cmd.getDescription());
		assertTrue(cmd.isConfigurableRankCommand());
		assertTrue(cmd.isOnlyPlayer());
	}

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.SetWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	public void testCanExecuteFailHelp() {
		// Help
		assertFalse(cmd.canExecute(user, "", List.of("something")));
		verify(user).sendMessage("commands.help.header", "[label]", null);
	}

	/**
     * Test method for {@link world.bentobox.parkour.commands.SetWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteFailNoIsland() {
        // Not on island
        when(im.userIsOnIsland(any(), any())).thenReturn(false);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("parkour.errors.not-on-island");
    }

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.SetWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	public void testCanExecuteFailNoRank() {
		// Insufficient rank
		assertFalse(cmd.canExecute(user, "", List.of()));
		verify(user).sendMessage("general.errors.insufficient-rank", TextVariables.RANK, RanksManager.MEMBER_RANK_REF);

	}

	/**
     * Test method for {@link world.bentobox.parkour.commands.SetWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoStartEndPlates() {
        // Has rank
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.MEMBER_RANK);
        // No start plate
        when(parkourManager.getStart(island)).thenReturn(Optional.empty());
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).notify("parkour.no-start-yet");
        // Start, but no end plate
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.empty());
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).notify("parkour.no-end-yet");
    }

	/**
     * Test method for {@link world.bentobox.parkour.commands.SetWarpCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecutePass() {
        // Has rank
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.MEMBER_RANK);
        assertTrue(cmd.canExecute(user, "", List.of()));
        verify(user, never()).sendMessage(any());
    }

	/**
	 * Test method for
	 * {@link world.bentobox.parkour.commands.SetWarpCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
	 */
	@Test
	public void testExecuteUserStringListOfString() {
		assertTrue(cmd.execute(user, "", List.of()));
		verify(user).sendMessage("parkour.warp.set");
		verify(parkourManager).setWarpSpot(island, null);

	}

	/**
     * Test method for {@link world.bentobox.parkour.commands.SetWarpCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringWarpReplace() {
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(mock(Location.class)));
        assertTrue(cmd.execute(user, "", List.of()));
        verify(user).sendMessage("parkour.warp.replaced");
        verify(parkourManager).setWarpSpot(island, null);
    }

}
