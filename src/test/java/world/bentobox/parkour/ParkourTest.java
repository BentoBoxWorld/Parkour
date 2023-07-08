package world.bentobox.parkour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class, Config.class })
public class ParkourTest {

    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;

    private Parkour addon;
    @Mock
    private BentoBox plugin;
    @Mock
    private FlagsManager fm;
    @Mock
    private Settings settings;
    @Mock
    private PlaceholdersManager phm;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());

        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(settings);
        when(settings.getDatabaseType()).thenReturn(value);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        // Placeholders
        when(phm.replacePlaceholders(any(), anyString())).thenAnswer(a -> (String)a.getArgument(1, String.class));


        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        Player p = mock(Player.class);
        // Sometimes use Mockito.withSettings().verboseLogging()
        when(user.isOp()).thenReturn(false);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(plugin.getIWM()).thenReturn(iwm);


        // Player has island to begin with
        island = mock(Island.class);
        when(im.getIsland(Mockito.any(), Mockito.any(UUID.class))).thenReturn(island);
        when(plugin.getIslands()).thenReturn(im);

        // Locales
        // Return the reference (USE THIS IN THE FUTURE)
        when(user.getTranslation(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Server
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        Server server = mock(Server.class);
        when(Bukkit.getServer()).thenReturn(server);
        when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(Bukkit.getPluginManager()).thenReturn(mock(PluginManager.class));

        // Create an Addon
        addon = new Parkour();
        File jFile = new File("addon.jar");
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {

            // Copy over config file from src folder
            Path fromPath = Paths.get("src/main/resources/config.yml");
            Path path = Paths.get("config.yml");
            Files.copy(fromPath, path);

            //Add the new files to the jar.
            add(path, tempJarOutputStream);

        }

        File dataFolder = new File("addons/Parkour");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "parkour", "1.3").description("test").authors("tasty").build();
        addon.setDescription(desc);
        // Addons manager
        AddonsManager am = mock(AddonsManager.class);
        when(plugin.getAddonsManager()).thenReturn(am);

        // Flags manager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

    }

    private void add(Path path, JarOutputStream tempJarOutputStream) throws FileNotFoundException, IOException {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            JarEntry entry = new JarEntry(path.toString());
            tempJarOutputStream.putNextEntry(entry);
            while((bytesRead = fis.read(buffer)) != -1) {
                tempJarOutputStream.write(buffer, 0, bytesRead);
            }
        }

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        //new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
    }

    private void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }

    }


    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#onEnable()}.
     */
    @Test
    public void testOnEnable() {
        testOnLoad();
        addon.setState(State.ENABLED);
        addon.onEnable();
        verify(plugin, never()).logError(anyString());
        assertNotEquals(State.DISABLED, addon.getState());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#onLoad()}.
     */
    @Test
    public void testOnLoad() {
        addon.onLoad();
        // Check that config.yml file has been saved
        File check = new File("addons/parkour","config.yml");
        assertTrue(check.exists());
        assertTrue(addon.getPlayerCommand().isPresent());
        assertTrue(addon.getAdminCommand().isPresent());

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#onReload()}.
     */
    @Test
    public void testOnReload() {
        addon.onEnable();
        addon.onReload();
        // Check that config.yml file has been saved
        File check = new File("addons/parkour","config.yml");
        assertTrue(check.exists());
    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#createWorlds()}.
     */
    @Test
    public void testCreateWorlds() {
        addon.onLoad();
        addon.createWorlds();
        verify(plugin).log("[parkour] Creating Parkour world ...");
        verify(plugin).log("[parkour] Creating Parkour's Nether...");
        verify(plugin).log("[parkour] Creating Parkour's End World...");

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#getSettings()}.
     */
    @Test
    public void testGetSettings() {
        addon.onLoad();
        assertNotNull(addon.getSettings());

    }

    /**
     * Test method for {@link world.bentobox.aoneblock.AOneBlock#getWorldSettings()}.
     */
    @Test
    public void testGetWorldSettings() {
        addon.onLoad();
        assertEquals(addon.getSettings(), addon.getWorldSettings());
    }
}
