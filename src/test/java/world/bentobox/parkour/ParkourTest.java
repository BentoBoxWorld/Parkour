package world.bentobox.parkour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class ParkourTest extends AbstractParkourTest {

	protected Parkour addonJar;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// Create an Addon
		addonJar = new Parkour();
		File jFile = new File("addon.jar");
		try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {

			// Copy over config file from src folder
			Path fromPath = Paths.get("src/main/resources/config.yml");
			Path path = Paths.get("config.yml");
			Files.copy(fromPath, path);

			// Add the new files to the jar.
			add(path, tempJarOutputStream);

		}

		File dataFolder = new File("addons/Parkour");
		addonJar.setDataFolder(dataFolder);
		addonJar.setFile(jFile);
		AddonDescription desc = new AddonDescription.Builder("bentobox", "parkour", "1.3").description("test")
				.authors("tasty").build();
		addonJar.setDescription(desc);

	}

	private void add(Path path, JarOutputStream tempJarOutputStream) throws FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(path.toFile())) {
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			JarEntry entry = new JarEntry(path.toString());
			tempJarOutputStream.putNextEntry(entry);
			while ((bytesRead = fis.read(buffer)) != -1) {
				tempJarOutputStream.write(buffer, 0, bytesRead);
			}
		}

	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#onEnable()}.
	 */
	@Test
	public void testOnEnable() {
		testOnLoad();
		addonJar.setState(State.ENABLED);
		addonJar.onEnable();
		verify(plugin, never()).logError(anyString());
		assertNotEquals(State.DISABLED, addonJar.getState());
	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#onLoad()}.
	 */
	@Test
	public void testOnLoad() {
		addonJar.onLoad();
		// Check that config.yml file has been saved
		File check = new File("addons/Parkour", "config.yml");
		assertTrue(check.exists());
		assertTrue(addonJar.getPlayerCommand().isPresent());
		assertTrue(addonJar.getAdminCommand().isPresent());

	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#onReload()}.
	 */
	@Test
	public void testOnReload() {
		addonJar.onEnable();
		addonJar.onReload();
		// Check that config.yml file has been saved
		File check = new File("addons/Parkour", "config.yml");
		assertTrue(check.exists());
	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#createWorlds()}.
	 */
	@Test
	public void testCreateWorlds() {
		addonJar.onLoad();
		addonJar.createWorlds();
		verify(plugin).log("[parkour] Creating Parkour world ...");
		verify(plugin).log("[parkour] Creating Parkour's Nether...");
		verify(plugin).log("[parkour] Creating Parkour's End World...");

	}

	/**
	 * Test method for {@link world.bentobox.aoneblock.AOneBlock#getSettings()}.
	 */
	@Test
	public void testGetSettings() {
		addonJar.onLoad();
		assertNotNull(addonJar.getSettings());

	}

	/**
	 * Test method for
	 * {@link world.bentobox.aoneblock.AOneBlock#getWorldSettings()}.
	 */
	@Test
	public void testGetWorldSettings() {
		addonJar.onLoad();
		assertEquals(addonJar.getSettings(), addonJar.getWorldSettings());
	}
}
