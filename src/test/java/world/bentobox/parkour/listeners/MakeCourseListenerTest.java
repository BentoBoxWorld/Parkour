package world.bentobox.parkour.listeners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.parkour.CommonTestSetup;
import world.bentobox.parkour.ParkourManager;

/**
 * Tests {@link MakeCourseListener} — handles block place/break events for course
 * creation (start/end plates, checkpoints and warp spots).
 *
 * @author tastybento
 */
class MakeCourseListenerTest extends CommonTestSetup {

    @Mock
    private ParkourManager parkourManager;
    @Mock
    private Block block;

    private MakeCourseListener listener;
    private User user;

    /**
     * Build a BlockPlaceEvent for the mocked block/player.
     */
    private BlockPlaceEvent placeEvent() {
        BlockState state = mock(BlockState.class);
        Block replacedBlock = mock(Block.class);
        ItemStack item = mock(ItemStack.class);
        return new BlockPlaceEvent(block, state, replacedBlock, item, mockPlayer, true, EquipmentSlot.HAND);
    }

    /**
     * A location in the same world but at different block coordinates, so it is not
     * {@code isLocEquals} to the shared {@code location} mock (which sits at 0,0,0).
     */
    private Location otherLocation(int xyz) {
        Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(world);
        when(l.getBlockX()).thenReturn(xyz);
        when(l.getBlockY()).thenReturn(xyz);
        when(l.getBlockZ()).thenReturn(xyz);
        return l;
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(addon.getParkourManager()).thenReturn(parkourManager);
        when(addon.inWorld(location)).thenReturn(true);
        when(addon.inWorld(world)).thenReturn(true);

        when(block.getLocation()).thenReturn(location);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(Material.WARPED_PRESSURE_PLATE);

        user = User.getInstance(mockPlayer);
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.of(island));
        when(im.userIsOnIsland(world, user)).thenReturn(true);

        // Defaults: start and end set, no warp spot
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());

        listener = new MakeCourseListener(addon);
    }

    @Test
    void testConstructor() {
        assertNotNull(listener);
    }

    // ========== onWarpSet ==========

    @Test
    void testOnWarpSetHappy() {
        listener.onWarpSet(placeEvent());
        verify(parkourManager).setWarpSpot(island, location);
    }

    @Test
    void testOnWarpSetReplaced() {
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(location));
        listener.onWarpSet(placeEvent());
        verify(parkourManager).setWarpSpot(island, location);
    }

    @Test
    void testOnWarpSetNoStart() {
        when(parkourManager.getStart(island)).thenReturn(Optional.empty());
        listener.onWarpSet(placeEvent());
        verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    @Test
    void testOnWarpSetNoEnd() {
        when(parkourManager.getEnd(island)).thenReturn(Optional.empty());
        listener.onWarpSet(placeEvent());
        verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    @Test
    void testOnWarpSetWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        listener.onWarpSet(placeEvent());
        verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    @Test
    void testOnWarpSetNotInWorld() {
        when(addon.inWorld(location)).thenReturn(false);
        listener.onWarpSet(placeEvent());
        verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    @Test
    void testOnWarpSetNoIsland() {
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        listener.onWarpSet(placeEvent());
        verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    @Test
    void testOnWarpSetUserNotOnIsland() {
        when(im.userIsOnIsland(world, user)).thenReturn(false);
        listener.onWarpSet(placeEvent());
        verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    // ========== onStartEndSet ==========

    @Test
    void testOnStartEndSetStart() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(parkourManager.getStart(island)).thenReturn(Optional.empty());
        listener.onStartEndSet(placeEvent());
        verify(parkourManager).setStart(island, location);
    }

    @Test
    void testOnStartEndSetEnd() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        when(parkourManager.getEnd(island)).thenReturn(Optional.empty());
        listener.onStartEndSet(placeEvent());
        verify(parkourManager).setEnd(island, location);
    }

    @Test
    void testOnStartEndSetAlreadySet() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        listener.onStartEndSet(placeEvent());
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }

    @Test
    void testOnStartEndSetWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        listener.onStartEndSet(placeEvent());
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }

    @Test
    void testOnStartEndSetNotInWorld() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(addon.inWorld(location)).thenReturn(false);
        listener.onStartEndSet(placeEvent());
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }

    @Test
    void testOnStartEndSetNoIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        listener.onStartEndSet(placeEvent());
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }

    @Test
    void testOnStartEndSetUserNotOnIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(im.userIsOnIsland(world, user)).thenReturn(false);
        listener.onStartEndSet(placeEvent());
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }

    // ========== onCheckPointPlaced ==========

    @Test
    void testOnCheckPointPlacedHappy() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        listener.onCheckPointPlaced(placeEvent());
        // Passed all guards and reached the user check
        verify(im).userIsOnIsland(eq(world), any(User.class));
    }

    @Test
    void testOnCheckPointPlacedWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        listener.onCheckPointPlaced(placeEvent());
        verify(im, never()).getProtectedIslandAt(any());
    }

    @Test
    void testOnCheckPointPlacedNotInWorld() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        when(addon.inWorld(location)).thenReturn(false);
        listener.onCheckPointPlaced(placeEvent());
        verify(im, never()).getProtectedIslandAt(any());
    }

    @Test
    void testOnCheckPointPlacedNoIsland() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        listener.onCheckPointPlaced(placeEvent());
        verify(im, never()).userIsOnIsland(any(), any());
    }

    @Test
    void testOnCheckPointPlacedUserNotOnIsland() {
        when(block.getType()).thenReturn(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        when(im.userIsOnIsland(world, user)).thenReturn(false);
        listener.onCheckPointPlaced(placeEvent());
        verify(im).userIsOnIsland(eq(world), any(User.class));
    }

    // ========== onBreak ==========

    @Test
    void testOnBreakStart() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(location));
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager).setStart(island, null);
    }

    @Test
    void testOnBreakEnd() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        // Start is elsewhere so the broken block matches the end, not the start
        Location start = otherLocation(20);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(start));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(location));
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager).setEnd(island, null);
    }

    @Test
    void testOnBreakWarpSpot() {
        when(block.getType()).thenReturn(Material.WARPED_PRESSURE_PLATE);
        Location start = otherLocation(20);
        Location end = otherLocation(30);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(start));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(end));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.of(location));
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager).setWarpSpot(island, null);
    }

    @Test
    void testOnBreakStartEndResetBoth() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        // Neither the start nor end match the broken plate -> reset both
        Location start = otherLocation(20);
        Location end = otherLocation(30);
        when(parkourManager.getStart(island)).thenReturn(Optional.of(start));
        when(parkourManager.getEnd(island)).thenReturn(Optional.of(end));
        when(parkourManager.getWarpSpot(island)).thenReturn(Optional.empty());
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager).setStart(island, null);
        verify(parkourManager).setEnd(island, null);
    }

    @Test
    void testOnBreakWrongBlockType() {
        when(block.getType()).thenReturn(Material.STONE);
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
        verify(parkourManager, never()).setWarpSpot(any(), any());
    }

    @Test
    void testOnBreakNotInWorld() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(addon.inWorld(location)).thenReturn(false);
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }

    @Test
    void testOnBreakNoIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(im.getProtectedIslandAt(location)).thenReturn(Optional.empty());
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }

    @Test
    void testOnBreakUserNotOnIsland() {
        when(block.getType()).thenReturn(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        when(im.userIsOnIsland(world, user)).thenReturn(false);
        listener.onBreak(new BlockBreakEvent(block, mockPlayer));
        verify(parkourManager, never()).setStart(any(), any());
        verify(parkourManager, never()).setEnd(any(), any());
    }
}
