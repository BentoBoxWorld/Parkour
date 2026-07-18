package world.bentobox.parkour.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.parkour.CommonTestSetup;
import world.bentobox.parkour.ParkourManager;
import world.bentobox.parkour.ParkourRunRecord;
import world.bentobox.parkour.Settings;

/**
 * @author tastybento
 */
class QuitCommandTest extends CommonTestSetup {
    @Mock
    private CompositeCommand ac;
    @Mock
    private User user;
    @Mock
    private ParkourManager parkourManager;

    private QuitCommand cmd;
    @Mock
    private @NonNull Location loc;
    // No mock
    private ParkourRunRecord prm;

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

        // Parent command has no aliases
        when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
        when(ac.getWorld()).thenReturn(world);
        when(ac.getAddon()).thenReturn(addon);

        prm = new ParkourRunRecord(new HashMap<>(), new HashMap<>(), new ArrayList<>());
        prm.timers().put(uuid, 20L);
        when(addon.getParkourRunRecord()).thenReturn(prm);

        // Islands
        when(addon.getIslands()).thenReturn(im);
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

        // DUT
        cmd = new QuitCommand(ac);
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.commands.QuitCommand#QuitCommand(world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    void testQuitCommand() {
        assertNotNull(cmd);
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.QuitCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertEquals("quit", cmd.getPermission());
        assertEquals("parkour.commands.parkour.quit.description", cmd.getDescription());
        assertTrue(cmd.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.QuitCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteWrongWorld() {
        when(iwm.inWorld(world)).thenReturn(false);
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.wrong-world");
    }

    /**
     * Test method for {@link world.bentobox.parkour.commands.QuitCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNotOnIsland() {
        // Not on island
        when(im.getIslandAt(loc)).thenReturn(Optional.empty());
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("parkour.errors.not-on-island");
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.commands.QuitCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNotInRun() {
        prm.timers().clear();
        assertFalse(cmd.canExecute(user, "", List.of()));
        verify(user).sendMessage("parkour.errors.not-in-run");
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.commands.QuitCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteSuccess() {
        assertTrue(cmd.canExecute(user, "", List.of()));
    }

    /**
     * Test method for
     * {@link world.bentobox.parkour.commands.QuitCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
        assertTrue(cmd.execute(user, "", List.of()));
        verify(user).sendMessage("parkour.quit.success");
        assertTrue(prm.timers().isEmpty());
        assertTrue(prm.checkpoints().isEmpty());
    }

}
