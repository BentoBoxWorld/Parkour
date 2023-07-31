package world.bentobox.parkour.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.Settings;
import world.bentobox.parkour.gui.RankingsUI;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class, Util.class})
public class ClearTopCommandTest {

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
    @Mock
    private ParkourManager parkourManager;

    private ClearTopCommand cmd;
    @Mock
    private @NonNull Location location;
    @Mock
    private RankingsUI rankings;
    @Mock
    private PlayersManager pm;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        world.bentobox.bentobox.Settings s = new world.bentobox.bentobox.Settings();
        when(plugin.getSettings()).thenReturn(s);

        // Bukkit
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);

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

        // Util
        PowerMockito.mockStatic(Util.class, Mockito.RETURNS_MOCKS);
        when(Util.getUUID("tastybento")).thenReturn(uuid);

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
        when(addon.getPm()).thenReturn(parkourManager);

        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getPermissionPrefix(any())).thenReturn("parkour.");
        when(iwm.inWorld(world)).thenReturn(true);

        // Settings
        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        // RanksManager
        RanksManager rm = new RanksManager();
        when(plugin.getRanksManager()).thenReturn(rm);

        // Players Manager
        when(addon.getPlayers()).thenReturn(pm);
        when(pm.getName(uuid)).thenReturn("tastybento");

        // DUT
        cmd = new ClearTopCommand(ac);
    }
    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#ClearTopCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    public void testClearTopCommand() {
        assertNonNull(cmd);
    }

    private void assertNonNull(ClearTopCommand cmd2) {
        // TODO Auto-generated method stub

    }
    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("parkour.cleartop", cmd.getPermission());
        assertEquals("parkour.commands.parkour.cleartop.description", cmd.getDescription());
        assertEquals("parkour.commands.parkour.cleartop.parameters", cmd.getParameters());
        assertTrue(cmd.isConfigurableRankCommand());
        assertTrue(cmd.isOnlyPlayer());

    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteShowHelp() {
        assertFalse(cmd.canExecute(user, "", List.of("too","many","args")));
        verify(user).sendMessage("commands.help.header","[label]",null);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoIsland() {
        when(im.hasIsland(world, user)).thenReturn(false);
        when(im.inTeam(world, uuid)).thenReturn(false);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteInsufficientRank() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.ADMIN_RANK);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.insufficient-rank", TextVariables.RANK, RanksManager.MEMBER_RANK_REF);

    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteUnknownPlayer() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.VISITOR_RANK);
        assertFalse(cmd.canExecute(user, "", List.of("lspvicky")));
        verify(user).sendMessage("general.errors.unknown-player", TextVariables.NAME, "lspvicky");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecute() {
        when(island.getRankCommand(anyString())).thenReturn(RanksManager.VISITOR_RANK);
        assertTrue(cmd.canExecute(user, "", List.of("tastybento")));
        assertTrue(cmd.canExecute(user, "", List.of()));
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfString() {
        assertTrue(cmd.execute(user, "", List.of("tastybento")));
        verify(user).sendMessage("commands.confirmation.confirm", "[seconds]", "10");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#confirmed(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testConfirmed() {
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
    public void testConfirmedNoIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        cmd.confirmed(user);
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.ClearTopCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringSuccess() {
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
    public void testTabCompleteUserStringListOfStringNotOnIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        Optional<List<String>> opList = cmd.tabComplete(user, "", List.of(""));
        assertTrue(opList.isEmpty());
    }

}
