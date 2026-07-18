package world.bentobox.parkour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.parkour.objects.ParkourData;

/**
 * @author tastybento
 *
 */
class ParkourManagerTest extends CommonTestSetup {

    @Mock
    private Parkour addon;
    @Mock
    private PlayersManager pm;

    // Class under test
    private ParkourManager parkourManager;

    private MockedStatic<DatabaseSetup> mockDb;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        // Database setup - mock static like in ParkourTest
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
        when(h.loadObjects()).thenReturn(java.util.Collections.emptyList());
        when(h.objectExists(anyString())).thenReturn(false);

        // Addon configuration
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getIslands()).thenReturn(im);
        when(addon.getSettings()).thenReturn(new Settings());
        when(addon.inWorld(world)).thenReturn(true);
        when(addon.getPlayers()).thenReturn(pm);
        when(pm.getName(any(UUID.class))).thenReturn("tastybento");

        // Island setup
        String uniqueId = UUID.randomUUID().toString();
        when(island.getUniqueId()).thenReturn(uniqueId);
        when(island.getWorld()).thenReturn(world);
        island.setUniqueId(uniqueId);
        island.setWorld(world);
        island.setOwner(uuid);
        island.setCenter(location);

        when(im.getIsland(world, User.getInstance(mockPlayer))).thenReturn(island);
        when(im.getIslandById(anyString())).thenReturn(Optional.of(island));

        // DUT (Device Under Test)
        parkourManager = new ParkourManager(addon);
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#ParkourManager(world.bentobox.parkour.Parkour)}.
     */
    @Test
    void testParkourManager() {
        assertNotNull(parkourManager);
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#addScore(world.bentobox.bentobox.database.objects.Island, world.bentobox.bentobox.api.user.User, long)}.
     */
    @Test
    void testAddScore() {
        User user = User.getInstance(mockPlayer);
        parkourManager.addScore(island, user, 10L);
        Map<UUID, Long> ranks = parkourManager.getRankings(island, 10L);
        assertEquals(1, ranks.size());
        assertEquals(Long.valueOf(10), ranks.get(uuid));
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#clearScores(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    void testClearScores() {
        testAddScore();
        parkourManager.clearScores(island);
        assertTrue(parkourManager.getRankings(island, 10L).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#removeScore(world.bentobox.bentobox.database.objects.Island, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    void testRemoveScore() {
        testAddScore();
        User user = User.getInstance(mockPlayer);
        parkourManager.removeScore(island, user);
        assertTrue(parkourManager.getRankings(island, 10L).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getRankings(world.bentobox.bentobox.database.objects.Island, long)}.
     */
    @Test
    void testGetRankings() {
        Map<UUID, Long> ranks = parkourManager.getRankings(island, 10L);
        assertTrue(ranks.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getRank(world.bentobox.bentobox.database.objects.Island, java.util.UUID)}.
     */
    @Test
    void testGetRank() {
        int rank = parkourManager.getRank(island, uuid);
        assertEquals(1, rank);
        testAddScore();
        rank = parkourManager.getRank(island, uuid);
        assertEquals(1, rank);
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getTime(world.bentobox.bentobox.database.objects.Island, java.util.UUID)}.
     */
    @Test
    void testGetTime() {
        long time = parkourManager.getTime(island, uuid);
        assertEquals(0, time);
        testAddScore();
        time = parkourManager.getTime(island, uuid);
        assertEquals(10L, time);
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getParkourData()}.
     */
    @Test
    void testGetParkourData() {
        Collection<ParkourData> data = parkourManager.getParkourData();
        assertTrue(data.isEmpty());
        testAddScore();
        data = parkourManager.getParkourData();
        assertFalse(data.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getStart(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    void testGetStart() {
        Optional<Location> start = parkourManager.getStart(island);
        assertTrue(start.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getEnd(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    void testGetEnd() {
        Optional<Location> end = parkourManager.getEnd(island);
        assertTrue(end.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getWarpSpot(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    void testGetWarpSpot() {
        Optional<Location> warpSpot = parkourManager.getWarpSpot(island);
        assertTrue(warpSpot.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#setStart(world.bentobox.bentobox.database.objects.Island, org.bukkit.Location)}.
     */
    @Test
    void testSetStart() {
        parkourManager.setStart(island, location);
        Optional<Location> start = parkourManager.getStart(island);
        assertFalse(start.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#setEnd(world.bentobox.bentobox.database.objects.Island, org.bukkit.Location)}.
     */
    @Test
    void testSetEnd() {
        parkourManager.setEnd(island, location);
        Optional<Location> end = parkourManager.getEnd(island);
        assertTrue(end.isPresent());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#setWarpSpot(world.bentobox.bentobox.database.objects.Island, org.bukkit.Location)}.
     */
    @Test
    void testSetWarpSpot() {
        parkourManager.setWarpSpot(island, location);
        Optional<Location> warpSpot = parkourManager.getWarpSpot(island);
        assertTrue(warpSpot.isPresent());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getWarps()}.
     */
    @Test
    void testGetWarps() {
        Map<String, Location> map = parkourManager.getWarps();
        assertTrue(map.isEmpty());

        parkourManager.setWarpSpot(island, location);
        map = parkourManager.getWarps();
        assertFalse(map.isEmpty());
        assertEquals(location, map.get("tastybento"));
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getWarps()}.
     */
    @Test
    void testGetWarpsNoIslandOwner() {
        // Mock the island to have no owner
        when(island.getOwner()).thenReturn(null);
        parkourManager.setWarpSpot(island, location);
        Map<String, Location> map = parkourManager.getWarps();
        assertTrue(map.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getWarps()}.
     */
    @Test
    void testGetWarpsNoWarpSpot() {
        parkourManager.setWarpSpot(island, null);
        Map<String, Location> map = parkourManager.getWarps();
        assertTrue(map.isEmpty());
    }

}
