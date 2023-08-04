package world.bentobox.parkour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.objects.ParkourData;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, DatabaseSetup.class, Util.class})
public class ParkourManagerTest {

    @Mock
    private Parkour addon;
    // Class under test
    private ParkourManager parkourManager;

    private static AbstractDatabaseHandler<Object> h;
    @Mock
    private BentoBox plugin;
    @Mock
    private Settings pluginSettings;
    @Mock
    private User user;
    @Mock
    private World world;

    private Island island;
    @Mock
    private IslandsManager im;
    private String uniqueId;
    @Mock
    private Location location;
    private world.bentobox.parkour.Settings settings;
    private UUID uuid = UUID.randomUUID();
    @Mock
    private PlayersManager pm;


    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void beforeClass() throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        // This has to be done beforeClass otherwise the tests will interfere with each other
        h = mock(AbstractDatabaseHandler.class);
        // Database
        PowerMockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        when(DatabaseSetup.getDatabase()).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
    }

    @Before
    public void setUp() {
        when(addon.getPlugin()).thenReturn(plugin);
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(pluginSettings);
        when(pluginSettings.getDatabaseType()).thenReturn(value);
        // Island manager
        when(addon.getIslands()).thenReturn(im);
        uniqueId = UUID.randomUUID().toString();
        // Island
        island = new Island();
        island.setUniqueId(uniqueId);
        island.setWorld(world);
        island.setOwner(uuid);

        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);
        island.setCenter(location);
        when(im.getIsland(eq(world), eq(user))).thenReturn(island);

        when(im.getIslandById(anyString())).thenReturn(Optional.of(island));

        PowerMockito.mockStatic(Util.class);
        when(Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, World.class));

        // Addon settings
        settings = new world.bentobox.parkour.Settings();
        when(addon.getSettings()).thenReturn(settings);

        // Addon
        when(addon.inWorld(world)).thenReturn(true);

        // User
        when(user.getUniqueId()).thenReturn(uuid);

        // Players Manager
        when(addon.getPlayers()).thenReturn(pm);
        when(pm.getName(uuid)).thenReturn("tastybento");

        // DUT
        parkourManager = new ParkourManager(addon);
    }

    /**
     * @throws java.lang.Exception - exception
     */
    @After
    public void tearDown() throws Exception {
        deleteAll(new File("database"));
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }

    private static void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#ParkourManager(world.bentobox.parkour.Parkour)}.
     */
    @Test
    public void testParkourManager() {
        assertNotNull(parkourManager);
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#addScore(world.bentobox.bentobox.database.objects.Island, world.bentobox.bentobox.api.user.User, long)}.
     */
    @Test
    public void testAddScore() {
        parkourManager.addScore(island, user, 10L);
        Map<UUID, Long> ranks = parkourManager.getRankings(island, 10L);
        assertEquals(1, ranks.size());
        assertTrue(ranks.get(uuid) == 10L);
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#clearScores(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testClearScores() {
        testAddScore();
        parkourManager.clearScores(island);
        assertTrue(parkourManager.getRankings(island, 10L).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#removeScore(world.bentobox.bentobox.database.objects.Island, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testRemoveScore() {
        testAddScore();
        parkourManager.removeScore(island, user);
        assertTrue(parkourManager.getRankings(island, 10L).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getRankings(world.bentobox.bentobox.database.objects.Island, long)}.
     */
    @Test
    public void testGetRankings() {
        Map<UUID, Long> ranks = parkourManager.getRankings(island, 10L);
        assertTrue(ranks.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getRank(world.bentobox.bentobox.database.objects.Island, java.util.UUID)}.
     */
    @Test
    public void testGetRank() {
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
    public void testGetTime() {
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
    public void testGetParkourData() {
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
    public void testGetStart() {
        Optional<Location> start = parkourManager.getStart(island);
        assertTrue(start.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getEnd(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testGetEnd() {
        Optional<Location> end = parkourManager.getEnd(island);
        assertTrue(end.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getWarpSpot(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testGetWarpSpot() {
        Optional<Location> warpSpot = parkourManager.getWarpSpot(island);
        assertTrue(warpSpot.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#setStart(world.bentobox.bentobox.database.objects.Island, org.bukkit.Location)}.
     */
    @Test
    public void testSetStart() {
        parkourManager.setStart(island, location);
        Optional<Location> start = parkourManager.getStart(island);
        assertFalse(start.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#setEnd(world.bentobox.bentobox.database.objects.Island, org.bukkit.Location)}.
     */
    @Test
    public void testSetEnd() {
        parkourManager.setEnd(island, location);
        Optional<Location> end = parkourManager.getEnd(island);
        assertTrue(end.isPresent());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#setWarpSpot(world.bentobox.bentobox.database.objects.Island, org.bukkit.Location)}.
     */
    @Test
    public void testSetWarpSpot() {
        parkourManager.setWarpSpot(island, location);
        Optional<Location> warpSpot = parkourManager.getWarpSpot(island);
        assertTrue(warpSpot.isPresent());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getWarps()}.
     */
    @Test
    public void testGetWarps() {
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
    public void testGetWarpsNoIslandOwner() {
        island.setOwner(null);
        parkourManager.setWarpSpot(island, location);
        Map<String, Location> map = parkourManager.getWarps();
        assertTrue(map.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.parkour.ParkourManager#getWarps()}.
     */
    @Test
    public void testGetWarpsNoWarpSpot() {
        parkourManager.setWarpSpot(island, null);
        Map<String, Location> map = parkourManager.getWarps();
        assertTrue(map.isEmpty());
    }

}
