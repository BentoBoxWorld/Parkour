package world.bentobox.parkour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;

/**
 * Tests for {@link Parkour}.
 *
 * @author tastybento
 */
class ParkourTest extends CommonTestSetup {

    @Mock
    private AddonsManager am;

    private Parkour addon;
    private MockedStatic<DatabaseSetup> mockDb;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Database mock
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        // CommandsManager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // AddonsManager
        when(plugin.getAddonsManager()).thenReturn(am);
        when(am.getGameModeAddons()).thenReturn(Collections.emptyList());

        // FlagsManager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

        // Create the addon backed by a JAR containing config.yml
        addon = new Parkour();
        File jFile = new File("addon.jar");
        Path fromPath = Paths.get("src/main/resources/config.yml");
        Path path = Paths.get("config.yml");
        Files.copy(fromPath, path);
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jFile))) {
            add(path, jos);
        }

        File dataFolder = new File("addons/Parkour");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "parkour", "1.3").description("test")
                .authors("tasty").build();
        addon.setDescription(desc);
    }

    private void add(Path path, JarOutputStream jos) throws IOException {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            JarEntry entry = new JarEntry(path.toString());
            jos.putNextEntry(entry);
            while ((bytesRead = fis.read(buffer)) != -1) {
                jos.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
        new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
    }

    /**
     * Test method for {@link Parkour#onEnable()}.
     */
    @Test
    void testOnEnable() {
        testOnLoad();
        addon.setState(State.ENABLED);
        addon.onEnable();
        verify(plugin, never()).logError(anyString());
        assertNotEquals(State.DISABLED, addon.getState());
    }

    /**
     * Test method for {@link Parkour#onLoad()}.
     */
    @Test
    void testOnLoad() {
        addon.onLoad();
        // Check that config.yml file has been saved
        File check = new File("addons/Parkour", "config.yml");
        assertTrue(check.exists());
        assertTrue(addon.getPlayerCommand().isPresent());
        assertTrue(addon.getAdminCommand().isPresent());
    }

    /**
     * Test method for {@link Parkour#onReload()}.
     */
    @Test
    void testOnReload() {
        addon.onEnable();
        addon.onReload();
        // Check that config.yml file has been saved
        File check = new File("addons/Parkour", "config.yml");
        assertTrue(check.exists());
    }

    /**
     * Test method for {@link Parkour#createWorlds()}.
     */
    @Test
    void testCreateWorlds() {
        addon.onLoad();
        addon.createWorlds();
        verify(plugin).log("[parkour] Creating Parkour world ...");
        verify(plugin).log("[parkour] Creating Parkour's Nether...");
        verify(plugin).log("[parkour] Creating Parkour's End World...");
    }

    /**
     * Test method for {@link Parkour#getSettings()}.
     */
    @Test
    void testGetSettings() {
        assertNull(addon.getSettings());
        addon.onLoad();
        assertNotNull(addon.getSettings());
    }

    /**
     * Test method for {@link Parkour#getWorldSettings()}.
     */
    @Test
    void testGetWorldSettings() {
        addon.onLoad();
        assertEquals(addon.getSettings(), addon.getWorldSettings());
    }
}
