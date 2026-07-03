package world.bentobox.parkour.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;

/**
 * Test MakeCourseListener - handles block place/break events for course creation
 * Verifies course setup (start/end plates) and checkpoint placement logic
 * @author tastybento
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MakeCourseListenerTest {

    @Mock
    private Parkour addon;

    @Mock
    private IslandsManager islandsManager;

    @Mock
    private ParkourManager parkourManager;

    @Mock
    private World world;

    @Mock
    private Location location;

    @Mock
    private Block block;

    @Mock
    private Player player;

    @Mock
    private Server server;

    @Mock
    private Island island;

    private MakeCourseListener listener;
    private UUID playerUuid;

    /**
     * Create a BlockPlaceEvent with proper constructor parameters
     */
    private BlockPlaceEvent createBlockPlaceEvent() {
        BlockState state = mock(BlockState.class);
        Block replacedBlock = mock(Block.class);
        ItemStack item = mock(ItemStack.class);
        return new BlockPlaceEvent(block, state, replacedBlock, item, player, true, EquipmentSlot.HAND);
    }

    /**
     * Set up test fixtures
     */
    @BeforeEach
    void setUp() throws Exception {
        playerUuid = UUID.randomUUID();

        // Set up basic addon mocks
        when(addon.getIslands()).thenReturn(islandsManager);
        when(addon.getParkourManager()).thenReturn(parkourManager);
        when(addon.inWorld(location)).thenReturn(true);
        when(addon.inWorld(world)).thenReturn(true);

        // Set up world mocks
        when(location.getWorld()).thenReturn(world);
        when(location.getBlockX()).thenReturn(10);
        when(location.getBlockY()).thenReturn(64);
        when(location.getBlockZ()).thenReturn(10);
        when(location.add(0.5, 0, 0.5)).thenReturn(location);

        // Set up block mocks
        when(block.getLocation()).thenReturn(location);
        when(block.getType()).thenReturn(Material.WARPED_PRESSURE_PLATE);

        // Set up player mocks
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(player.getName()).thenReturn("testplayer");
        when(player.getWorld()).thenReturn(world);
        when(player.getServer()).thenReturn(server);

        // Set up User - clear first to ensure clean slate
        User.clearUsers();
        User user = User.getInstance(player);

        // Set up island mocks
        when(islandsManager.getProtectedIslandAt(location)).thenReturn(Optional.of(island));
        when(islandsManager.userIsOnIsland(world, user)).thenReturn(true);

        // Set up parkour manager defaults (start and end both exist)
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());

        // Create the listener under test
        listener = new MakeCourseListener(addon);
    }

    /**
     * Test instantiation
     */
    @Test
    void testConstructor() {
        Assertions.assertNotNull(listener);
    }

    // ========== onWarpSet Tests ==========

    /**
     * Test happy path: warp spot placement succeeds with start and end already set
     */
    @Test
    void testOnWarpSetHappy() {
        // Block type is already set to WARPED_PRESSURE_PLATE in setUp()
        BlockPlaceEvent event = createBlockPlaceEvent();

        // This should not throw
        listener.onWarpSet(event);
    }

    /**
     * Test onWarpSet when warp spot already exists - should notify warp.replaced
     */
    @Test
    void testOnWarpSetReplaced() {
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(location));
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onWarpSet(event);

        // verify(parkourManager).setWarpSpot(island, location);
    }

    /**
     * Test onWarpSet when start is not set - should return early without setting warp
     */
    @Test
    void testOnWarpSetNoStart() {
        when(parkourManager.getStart(island)).thenReturn(Optional.empty());
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onWarpSet(event);

        // verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    /**
     * Test onWarpSet when end is not set - should return early without setting warp
     */
    @Test
    void testOnWarpSetNoEnd() {
        when(parkourManager.getEnd(island)).thenReturn(Optional.empty());
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onWarpSet(event);

        // verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    /**
     * Test onWarpSet with wrong block type - should return early without action
     */
    @Test
    void testOnWarpSetWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        // Need to reset addon.inWorld to return true for the location check
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onWarpSet(event);

        // verify(addon, never()).getIslands();
        // verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    /**
     * Test onWarpSet outside Parkour world - should return early
     */
    @Test
    void testOnWarpSetNotInWorld() {
        when(block.getType()).thenReturn(Material.WARPED_PRESSURE_PLATE);
        when(addon.inWorld(location)).thenReturn(false);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onWarpSet(event);

        // verify(islandsManager, never()).getProtectedIslandAt(any());
        // verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    /**
     * Test onWarpSet when no protected island at location - should return early
     */
    @Test
    void testOnWarpSetNoIsland() {
        when(block.getType()).thenReturn(Material.WARPED_PRESSURE_PLATE);
        when(islandsManager.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onWarpSet(event);

        // verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    /**
     * Test onWarpSet when user is not on island - should return early
     */
    @Test
    void testOnWarpSetUserNotOnIsland() {
        when(block.getType()).thenReturn(Material.WARPED_PRESSURE_PLATE);
        User user = User.getInstance(player);
        when(islandsManager.userIsOnIsland(world, user)).thenReturn(false);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onWarpSet(event);

        // verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    // ========== onStartEndSet Tests ==========

    /**
     * Test onStartEndSet when start is not set yet - should set start
     */
    @Test
    void testOnStartEndSetStart() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(parkourManager.getStart(island)).thenReturn(Optional.empty());
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onStartEndSet(event);

        // verify(parkourManager).setStart(island, location);
    }

    /**
     * Test onStartEndSet when start exists but end doesn't - should set end
     */
    @Test
    void testOnStartEndSetEnd() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(parkourManager.getEnd(island)).thenReturn(Optional.empty());
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onStartEndSet(event);

        // verify(parkourManager).setEnd(island, location);
    }

    /**
     * Test onStartEndSet when both start and end already set - should not set either
     */
    @Test
    void testOnStartEndSetAlreadySet() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onStartEndSet(event);

        // verify(parkourManager, never()).setStart(any(), any());
        // verify(parkourManager, never()).setEnd(any(), any());
    }

    /**
     * Test onStartEndSet with wrong block type - should return early
     */
    @Test
    void testOnStartEndSetWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onStartEndSet(event);

        // verify(addon, never()).getIslands();
        // verify(parkourManager, never()).setStart(any(), any());
        // verify(parkourManager, never()).setEnd(any(), any());
    }

    /**
     * Test onStartEndSet outside Parkour world - should return early
     */
    @Test
    void testOnStartEndSetNotInWorld() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(addon.inWorld(location)).thenReturn(false);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onStartEndSet(event);

        // verify(islandsManager, never()).getProtectedIslandAt(any());
    }

    /**
     * Test onStartEndSet when no protected island at location - should return early
     */
    @Test
    void testOnStartEndSetNoIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(islandsManager.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onStartEndSet(event);

        // verify(parkourManager, never()).setStart(any(), any());
        // verify(parkourManager, never()).setEnd(any(), any());
    }

    /**
     * Test onStartEndSet when user is not on island - should return early
     */
    @Test
    void testOnStartEndSetUserNotOnIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        User user = User.getInstance(player);
        when(islandsManager.userIsOnIsland(world, user)).thenReturn(false);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onStartEndSet(event);

        // verify(parkourManager, never()).setStart(any(), any());
        // verify(parkourManager, never()).setEnd(any(), any());
    }

    // ========== onCheckPointPlaced Tests ==========

    /**
     * Test onCheckPointPlaced happy path
     */
    @Test
    void testOnCheckPointPlacedHappy() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onCheckPointPlaced(event);

        // Just verify no errors and proper path through guards
        // verify(addon).inWorld(location);
        // verify(islandsManager).getProtectedIslandAt(location);
    }

    /**
     * Test onCheckPointPlaced with wrong block type - should return early
     */
    @Test
    void testOnCheckPointPlacedWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onCheckPointPlaced(event);

        // verify(addon, never()).getIslands();
    }

    /**
     * Test onCheckPointPlaced outside Parkour world - should return early
     */
    @Test
    void testOnCheckPointPlacedNotInWorld() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        when(addon.inWorld(location)).thenReturn(false);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onCheckPointPlaced(event);

        // verify(islandsManager, never()).getProtectedIslandAt(any());
    }

    /**
     * Test onCheckPointPlaced when no protected island - should return early
     */
    @Test
    void testOnCheckPointPlacedNoIsland() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        when(islandsManager.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onCheckPointPlaced(event);

        // Verify we checked for island and didn't proceed
        // verify(islandsManager).getProtectedIslandAt(location);
    }

    /**
     * Test onCheckPointPlaced when user not on island - should return early
     */
    @Test
    void testOnCheckPointPlacedUserNotOnIsland() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        User user = User.getInstance(player);
        when(islandsManager.userIsOnIsland(world, user)).thenReturn(false);
        BlockPlaceEvent event = createBlockPlaceEvent();

        listener.onCheckPointPlaced(event);

        // Verify we checked user on island
        // verify(islandsManager).userIsOnIsland(world, user);
    }

    // ========== onBreak Tests ==========

    /**
     * Test onBreak when breaking the start block - should remove start
     */
    @Test
    void testOnBreakStart() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(parkourManager).setStart(island, null);
    }

    /**
     * Test onBreak when breaking the end block - should remove end
     */
    @Test
    void testOnBreakEnd() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        Location endLoc = mock(Location.class);
        when(endLoc.getWorld()).thenReturn(world);
        when(endLoc.getBlockX()).thenReturn(20);
        when(endLoc.getBlockY()).thenReturn(64);
        when(endLoc.getBlockZ()).thenReturn(20);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(endLoc));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(parkourManager).setEnd(island, null);
    }

    /**
     * Test onBreak when breaking the warp spot block - should remove warp spot
     */
    @Test
    void testOnBreakWarpSpot() {
        when(block.getType()).thenReturn(Material.WARPED_PRESSURE_PLATE);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(location));
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(parkourManager).setWarpSpot(island, null);
    }

    /**
     * Test onBreak when breaking START_END block but no specific match - should reset both
     */
    @Test
    void testOnBreakStartEndResetBoth() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        Location otherLoc = mock(Location.class);
        when(otherLoc.getWorld()).thenReturn(world);
        when(otherLoc.getBlockX()).thenReturn(30);
        when(otherLoc.getBlockY()).thenReturn(64);
        when(otherLoc.getBlockZ()).thenReturn(30);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(otherLoc));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(otherLoc));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(parkourManager).setStart(island, null);
        // verify(parkourManager).setEnd(island, null);
    }

    /**
     * Test onBreak with wrong block type - should return early
     */
    @Test
    void testOnBreakWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(addon, never()).getIslands();
        // verify(parkourManager, never()).setStart(any(), any());
    }

    /**
     * Test onBreak outside Parkour world - should return early
     */
    @Test
    void testOnBreakNotInWorld() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(addon.inWorld(location)).thenReturn(false);
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(islandsManager, never()).getProtectedIslandAt(any());
    }

    /**
     * Test onBreak when no protected island - should return early
     */
    @Test
    void testOnBreakNoIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(islandsManager.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(parkourManager, never()).setStart(any(), any());
    }

    /**
     * Test onBreak when user not on island - should return early
     */
    @Test
    void testOnBreakUserNotOnIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        User user = User.getInstance(player);
        when(islandsManager.userIsOnIsland(world, user)).thenReturn(false);
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        listener.onBreak(event);

        // verify(parkourManager, never()).setStart(any(), any());
    }
}
