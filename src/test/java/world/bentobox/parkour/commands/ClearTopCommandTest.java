package world.bentobox.parkour.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.CommonTestSetup;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.Settings;
import world.bentobox.parkour.WhiteBox;
import world.bentobox.parkour.gui.RankingsUI;

/**
 * @author tastybento
 */
class ClearTopCommandTest extends CommonTestSetup {

    @Mock
    private CompositeCommand ac;
    @Mock
    private User user;
    @Mock
    private ParkourManager parkourManager;

    private ClearTopCommand cmd;
    @Mock
    private @NonNull Location loc;
    @Mock
    private RankingsUI rankings;
    @Mock
    private PlayersManager pm;
    @Mock
    private RanksManager rm;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        Player p = mock(Player.class);
        when(user.isOp()).thenReturn(false);
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(4);
        when(user.getWorld()).thenReturn(world);
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getLocation()).thenReturn(loc);
        when(user.getTranslation(any())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        User.setPlugin(plugin);

        // Util static (Bukkit and Util are already statically mocked by CommonTestSetup)
        mockedUtil.when(() -> Util.getUUID("tastybento")).thenReturn(uuid);

        // Parent command has no aliases
        when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
        when(ac.getWorld()).thenReturn(world);
        when(ac.getAddon()).thenReturn(addon);
        when(addon.getRankings()).thenReturn(rankings);

        // Islands
        when(im.getIsland(world, user)).thenReturn(island);
        when(im.getIslandAt(loc)).thenReturn(Optional.of(island));
        when(im.hasIsland(world, user)).thenReturn(true);
        when(im.inTeam(world, uuid)).thenReturn(true);
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.OWNER_RANK);
        when(island.getRank(user)).thenReturn(RanksManager.MEMBER_RANK);
        when(im.userIsOnIsland(any(), any())).thenReturn(true);

        // Parkour Manager
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        when(addon.getParkourManager()).thenReturn(parkourManager);

        // IWM
        when(iwm.getPermissionPrefix(any())).thenReturn("parkour.");
        when(iwm.inWorld(world)).thenReturn(true);

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // RanksManager singleton
        WhiteBox.setInternalState(RanksManager.class, "instance", rm);

        // Players Manager
        when(addon.getPlayers()).thenReturn(pm);
        when(pm.getName(uuid)).thenReturn("tastybento");

        // DUT
        cmd = new ClearTopCommand(ac);
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        // Reset the RanksManager singleton so other test classes get a fresh instance
        WhiteBox.setInternalState(RanksManager.class, "instance", null);
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#ClearTopCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    void testClearTopCommand() {
        assertNotNull(cmd);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertEquals("cleartop", cmd.getPermission());
        assertEquals("parkour.commands.parkour.cleartop.description", cmd.getDescription());
        assertEquals("parkour.commands.parkour.cleartop.parameters", cmd.getParameters());
        assertTrue(cmd.isConfigurableRankCommand());
        assertTrue(cmd.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteShowHelp() {
        assertFalse(cmd.canExecute(user, "", List.of("too", "many", "args")));
        verify(user).sendMessage("commands.help.header", "[label]", "Parkour");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoIsland() {
        when(im.hasIsland(world, user)).thenReturn(false);
        when(im.inTeam(world, uuid)).thenReturn(false);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteInsufficientRank() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.ADMIN_RANK);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.insufficient-rank", TextVariables.RANK, null);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteUnknownPlayer() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.VISITOR_RANK);
        assertFalse(cmd.canExecute(user, "", List.of("lspvicky")));
        verify(user).sendMessage("general.errors.unknown-player", TextVariables.NAME, "lspvicky");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecute() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.VISITOR_RANK);
        assertTrue(cmd.canExecute(user, "", List.of("tastybento")));
        assertTrue(cmd.canExecute(user, "", List.of()));
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
        assertTrue(cmd.execute(user, "", List.of("tastybento")));
        verify(user).sendMessage("commands.confirmation.confirm", "[seconds]", "10");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#confirmed(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    void testConfirmed() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.VISITOR_RANK);
        assertTrue(cmd.canExecute(user, "", List.of("tastybento")));
        cmd.confirmed(user);
        verify(parkourManager).removeScore(eq(island), any(User.class));
        assertTrue(cmd.canExecute(user, "", List.of()));
        cmd.confirmed(user);
        verify(user, times(2)).sendMessage("general.success");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#confirmed(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    void testConfirmedNoIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        cmd.confirmed(user);
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testTabCompleteUserStringListOfStringSuccess() {
        Map<UUID, Long> map = Map.of(uuid, 20L);
        when(parkourManager.getRankings(island, 10)).thenReturn(map);
        Optional<List<String>> opList = cmd.tabComplete(user, "", List.of(""));
        assertFalse(opList.isEmpty());
        assertEquals("tastybento", opList.get().get(0));
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testTabCompleteUserStringListOfStringNotOnIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        Optional<List<String>> opList = cmd.tabComplete(user, "", List.of(""));
        assertTrue(opList.isEmpty());
    }

}
